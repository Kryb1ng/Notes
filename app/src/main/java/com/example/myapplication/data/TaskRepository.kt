package com.example.myapplication.data

import android.net.Uri
import com.example.myapplication.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<TasksSnapshot>
    suspend fun insert(task: Task)
    suspend fun update(task: Task)
    suspend fun delete(task: Task)

    /**
     * Загружает изображение в Storage.
     * @return пара (downloadUrl, storagePath).
     */
    suspend fun uploadReportImage(taskId: String, source: Uri): UploadedImage

    /** Удаляет файл из Storage по пути. Если файла нет — молча игнорируем. */
    suspend fun deleteReportImage(storagePath: String)
}

data class UploadedImage(
    val downloadUrl: String,
    val storagePath: String,
)