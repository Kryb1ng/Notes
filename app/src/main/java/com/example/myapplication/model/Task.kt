package com.example.myapplication.model

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val priority: Priority = Priority.MEDIUM,
    val status: TaskStatus = TaskStatus.ACTIVE,

    val createdBy: String = "",

    val startedDate: Long? = null,
    val startedBy: String? = null,

    val completedDate: Long? = null,
    val completedBy: String? = null,

    // Отчёт о выполнении
    val reportComment: String? = null,
    val reportImageUrl: String? = null,
    val reportImageStoragePath: String? = null,       // нужен для удаления файла при revert
) {
    val isCompleted: Boolean
        get() = status == TaskStatus.COMPLETED
}