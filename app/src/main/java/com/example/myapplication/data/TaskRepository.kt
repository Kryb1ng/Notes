package com.example.myapplication.data

import com.example.myapplication.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Абстракция над хранилищем задач.
 * ViewModel зависит от этого интерфейса, а не от Firebase.
 */
interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    suspend fun insert(task: Task)
    suspend fun update(task: Task)
    suspend fun delete(task: Task)
}