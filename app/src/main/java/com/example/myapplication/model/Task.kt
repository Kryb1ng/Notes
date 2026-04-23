package com.example.myapplication.model

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",           // ← значение по умолчанию
    val isCompleted: Boolean = false,
    val createdDate: Long = System.currentTimeMillis(),
    val priority: Int = 2
)

