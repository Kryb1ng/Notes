package com.example.myapplication.ui.todo

import com.example.myapplication.model.Task
import com.example.myapplication.model.TaskStatus

enum class TaskFilter(val displayName: String) {
    ALL("Все"),
    ACTIVE("Активные"),
    IN_PROGRESS("В работе"),
    COMPLETED("Готово");

    fun matches(task: Task): Boolean = when (this) {
        ALL -> true
        ACTIVE -> task.status == TaskStatus.ACTIVE
        IN_PROGRESS -> task.status == TaskStatus.IN_PROGRESS
        COMPLETED -> task.status == TaskStatus.COMPLETED
    }
}