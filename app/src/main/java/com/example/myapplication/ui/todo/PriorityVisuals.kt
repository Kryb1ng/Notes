package com.example.myapplication.ui.todo

import androidx.compose.ui.graphics.Color
import com.example.myapplication.model.Priority

fun Priority.label(): String = when (this) {
    Priority.LOW -> "Низкий"
    Priority.MEDIUM -> "Средний"
    Priority.HIGH -> "Высокий"
}

// Меняем цвета на более сочные и различимые
fun Priority.priorityColor(): Color = when (this) {
    Priority.LOW -> Color(0xFF4CAF50)    // Спокойный зеленый
    Priority.MEDIUM -> Color(0xFFFF9800) // Насыщенный оранжевый (вместо желтого)
    Priority.HIGH -> Color(0xFFF44336)   // Яркий красный
}

// Если ты где-то еще используешь dotColor, можешь оставить алиас:
fun Priority.dotColor(): Color = priorityColor()