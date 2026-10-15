package com.example.myapplication.data

import android.content.Context
import android.net.Uri
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
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class FirebaseTaskRepository(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
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
    // ImgBB: загрузка и удаление изображений
    // ---------------------------------------------------------------------

    override suspend fun uploadReportImage(taskId: String, source: Uri): UploadedImage =
        withContext(Dispatchers.IO) {
            // 1. Читаем байты изображения из content:// URI
            val bytes = context.contentResolver.openInputStream(source)?.use { it.readBytes() }
                ?: throw IllegalStateException("Не удалось прочитать изображение по URI: $source")

            // 2. Отправляем multipart/form-data запрос в ImgBB
            val boundary = "Boundary-${System.currentTimeMillis()}"
            val url = URL("${IMGBB_UPLOAD_URL}?key=$IMGBB_API_KEY&expiration=$IMGBB_EXPIRATION_SECONDS")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                connectTimeout = 30_000
                readTimeout = 30_000
            }

            DataOutputStream(connection.outputStream).use { output ->
                // Заголовок части с файлом
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
            val imageUrl = data.getString("url")
            val deleteUrl = data.getString("delete_url")

            UploadedImage(downloadUrl = imageUrl, storagePath = deleteUrl)
        }

    override suspend fun deleteReportImage(storagePath: String) {

    }

    private companion object {
        const val COLLECTION_NAME = "tasks"
        const val IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload"
        const val IMGBB_API_KEY = "7289cc4953ce53538018415e01a6e6b6"
        const val IMGBB_EXPIRATION_SECONDS = 24L * 60 * 60
    }
}