package com.example.myapplication.model

import com.google.firebase.firestore.PropertyName
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",

    @get:PropertyName("isCompleted")
    @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false,

    val createdDate: Long = System.currentTimeMillis(),
    val priority: Priority = Priority.MEDIUM,

    val createdBy: String = "",
    val completedBy: String? = null,
)

