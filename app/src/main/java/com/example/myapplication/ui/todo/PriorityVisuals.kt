package com.example.myapplication.ui.todo

import androidx.compose.ui.graphics.Color
import com.example.myapplication.model.Priority

fun Priority.dotColor(): Color = when (this) {
    Priority.LOW -> Color.Green
    Priority.MEDIUM -> Color.Yellow
    Priority.HIGH -> Color.Red
}