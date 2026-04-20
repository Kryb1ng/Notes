package com.example.myapplication.model

import android.content.Context
import kotlinx.coroutines.flow.Flow
class TaskRepository(context: Context) {
    private val taskDao = TaskDatabase.getDatabase(context).taskDao()

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun insert(task: Task) {
        taskDao.insert(task)
    }

    suspend fun update(task: Task) {
        taskDao.update(task)
    }
    suspend fun delete(task: Task) {
        taskDao.delete(task)
    }
}

