package com.example.myapplication.model

class TaskRepository {
    private val _tasks = mutableListOf<Task>()
    val tasks: List<Task> get() = _tasks.toList()

    fun addTask(task: Task) {
        _tasks.add(task)
    }

    fun updateTask(updatedTask: Task) {
        val index = _tasks.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) {
            _tasks[index] = updatedTask
        }
    }

    fun deleteTask(task: Task) {
        _tasks.removeAll { it.id == task.id }
    }

    fun getTasksSorted(): List<Task> {
        return _tasks.sortedByDescending { it.priority }
    }
}

