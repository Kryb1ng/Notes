package com.example.myapplication.data
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.myapplication.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL


class FirebaseTaskRepository(
    private val context: Context,
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : TaskRepository {

    private val tasksCollection = firestore.collection(COLLECTION_NAME)

    override fun getAllTasks(): Flow<TasksSnapshot> = callbackFlow {
        val listener = tasksCollection.addSnapshotListener(
            MetadataChanges.INCLUDE,
        ) { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val tasks = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Task::class.java)?.copy(id = doc.id)
                }
                val isOffline = snapshot.metadata.isFromCache
                trySend(TasksSnapshot(tasks, isOffline))
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun insert(task: Task) {
        tasksCollection.document(task.id).set(task).await()
    }

    override suspend fun update(task: Task) {
        tasksCollection.document(task.id).set(task).await()
    }

    override suspend fun delete(task: Task) {
        tasksCollection.document(task.id).delete().await()
    }

    // ---------------------------------------------------------------------
    // ImgBB: загрузка изображений
    // ---------------------------------------------------------------------

    override suspend fun uploadReportImage(taskId: String, source: Uri): UploadedImage =
        withContext(Dispatchers.IO) {
            // 1. Читаем, сжимаем и получаем байты
            val bytes = readAndCompress(source)

            Log.d(TAG, "Размер после сжатия: ${bytes.size / 1024} КБ")

            if (bytes.size > MAX_IMAGE_BYTES) {
                throw IllegalStateException(
                    "Фото слишком большое: ${bytes.size / 1024 / 1024} МБ (максимум 32 МБ)"
                )
            }

            // 2. Отправляем multipart/form-data запрос в ImgBB
            val boundary = "Boundary-${System.currentTimeMillis()}"
            val url = URL("$IMGBB_UPLOAD_URL?key=$IMGBB_API_KEY&expiration=$IMGBB_EXPIRATION_SECONDS")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                connectTimeout = 120_000
                readTimeout = 120_000
            }

            DataOutputStream(connection.outputStream).use { output ->
                output.writeBytes("--$boundary\r\n")
                output.writeBytes(
                    "Content-Disposition: form-data; name=\"image\"; filename=\"report.jpg\"\r\n"
                )
                output.writeBytes("Content-Type: image/jpeg\r\n\r\n")
                output.write(bytes)
                output.writeBytes("\r\n")
                output.writeBytes("--$boundary--\r\n")
                output.flush()
            }

            // 3. Разбираем ответ
            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw IllegalStateException("ImgBB upload failed: HTTP $responseCode, $error")
            }
            connection.disconnect()

            val data = JSONObject(responseText).getJSONObject("data")
            UploadedImage(
                downloadUrl = data.getString("url"),
                storagePath = data.getString("delete_url"),
            )
        }

    /**
     * Читает изображение по [source], сжимает до [MAX_IMAGE_SIDE] по большей стороне
     * и кодирует в JPEG с качеством [JPEG_QUALITY].
     */
    private fun readAndCompress(source: Uri): ByteArray {
        // 1. Читаем сырые байты один раз
        val rawBytes = context.contentResolver.openInputStream(source)?.use { it.readBytes() }
            ?: throw IllegalStateException("Не удалось прочитать изображение по URI: $source")

        // 2. Читаем EXIF-ориентацию
        val orientation = androidx.exifinterface.media.ExifInterface(
            java.io.ByteArrayInputStream(rawBytes)
        ).getAttributeInt(
            androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
            androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL,
        )

        // 3. Декодируем Bitmap из тех же байтов
        val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
            ?: throw IllegalStateException("Не удалось декодировать изображение")

        // 4. Поворачиваем в соответствии с EXIF
        val oriented = applyExifOrientation(decoded, orientation)

        // 5. Масштабируем
        val scale = minOf(
            MAX_IMAGE_SIDE.toFloat() / oriented.width,
            MAX_IMAGE_SIDE.toFloat() / oriented.height,
            1f,
        )
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                oriented,
                (oriented.width * scale).toInt(),
                (oriented.height * scale).toInt(),
                true,
            )
        } else {
            oriented
        }

        // 6. Освобождаем промежуточные битмапы
        if (scaled !== oriented) oriented.recycle()
        if (oriented !== decoded) decoded.recycle()

        // 7. Кодируем в JPEG
        return ByteArrayOutputStream().use { output ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            scaled.recycle()
            output.toByteArray()
        }
    }

    private fun applyExifOrientation(
        bitmap: Bitmap,
        orientation: Int,
    ): Bitmap {
        val matrix = android.graphics.Matrix()
        when (orientation) {
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap  // ORIENTATION_NORMAL или неизвестно — не трогаем
        }
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true,
        )
    }

    override suspend fun deleteReportImage(storagePath: String) {
        // ImgBB не предоставляет публичного API для удаления файлов.
        // Вместо удаления используем параметр expiration при загрузке —
        // ImgBB сам удалит изображение через IMGBB_EXPIRATION_SECONDS.
    }

    private companion object {
        const val TAG = "ImgBB"
        const val COLLECTION_NAME = "tasks"
        const val IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload"
        const val IMGBB_API_KEY = "7289cc4953ce53538018415e01a6e6b6"
        const val IMGBB_EXPIRATION_SECONDS = 24L * 60 * 60 // 24 часа
        const val MAX_IMAGE_BYTES = 32 * 1024 * 1024       // лимит ImgBB
        const val MAX_IMAGE_SIDE = 1280                    // Макс. сторона после сжатия
        const val JPEG_QUALITY = 80                        // % качества JPEG
    }
}