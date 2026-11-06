package com.example.myapplication.ui.todo

import androidx.compose.ui.graphics.Color
import com.example.myapplication.model.Priority
import com.example.myapplication.ui.theme.*

fun Priority.label(): String = when (this) {
    Priority.LOW -> "Низкий"
    Priority.MEDIUM -> "Средний"
    Priority.HIGH -> "Высокий"
}

fun Priority.priorityColor(): Color = when (this) {
    Priority.LOW -> PriorityLow
    Priority.MEDIUM -> PriorityMedium
    Priority.HIGH -> PriorityHigh
}

fun Priority.dotColor(): Color = priorityColor()
fun Priority.chipColor(): Color = priorityColor().copy(alpha = 0.7f)