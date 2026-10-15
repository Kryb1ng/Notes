package com.example.myapplication.model

import androidx.annotation.Keep
import java.util.UUID
import com.google.firebase.firestore.PropertyName

@Keep
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    @get:PropertyName("isCompleted")
    @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false,
    val createdDate: Long = System.currentTimeMillis(),
    val priority: Int = 2
)

