package com.example.myapplication.ui.todo

import com.example.myapplication.model.Task
import com.example.myapplication.model.TaskStatus

enum class TaskFilter(val displayName: String) {
    // Порядок важен: первый элемент будет первым в списке чипов
    ACTIVE("Активные"),
    COMPLETED("Выполненные"),
    ALL("Все");

    fun matches(task: Task): Boolean = when (this) {
        // В "Активные" попадают и новые задачи, и те, что взяты в работу
        ACTIVE -> task.status == TaskStatus.ACTIVE || task.status == TaskStatus.IN_PROGRESS
        COMPLETED -> task.status == TaskStatus.COMPLETED
        ALL -> true
    }
}