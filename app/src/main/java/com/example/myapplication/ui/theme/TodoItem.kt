package com.example.myapplication.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Task
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TodoItem(
    task: Task,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Индикатор приоритета
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(
                    when (task.priority) {
                        1 -> Color.Green
                        2 -> Color.Yellow
                        3 -> Color.Red
                        else -> Color.Gray
                    }
                )
        )
        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.combinedClickable(
                    onClick = { /* можно добавить быстрый toggle, но пока пусто */ },
                    onLongClick = onEdit
                ),
                softWrap = true,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
            )
            Text(
                text = formatDate(task.createdDate),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onToggleComplete) {
                    Text(if (task.isCompleted) "Вернуть" else "Готово")
                }
                Button(onClick = onDelete) {
                    Text("Удалить")
                }
            }
        }
    }
}

// Утилита форматирования даты (можно вынести в отдельный файл utils)
private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}