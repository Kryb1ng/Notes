package com.example.myapplication.model

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val priority: Priority = Priority.MEDIUM,
    val status: TaskStatus = TaskStatus.ACTIVE,

    // Кто и когда создал — заполняется при добавлении
    val createdBy: String = "",

    // Кто и когда взял в работу — заполняется при переходе в IN_PROGRESS
    val startedDate: Long? = null,
    val startedBy: String? = null,

    // Кто и когда завершил — заполняется при переходе в COMPLETED
    val completedDate: Long? = null,
    val completedBy: String? = null,
) {
    val isCompleted: Boolean
        get() = status == TaskStatus.COMPLETED
}

