package com.example.myapplication.model

import java.util.Date
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isCompleted: Boolean = false,
    val createdDate: Long = System.currentTimeMillis(),
    val priority: Int = 2  // 1=низкий, 2=средний, 3=высокий
)

