package com.example.myapplication.model

import java.util.Date
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val isCompleted: Boolean = false,
    val createdDate: Long = System.currentTimeMillis(),
    val priority: Int = 2
)

