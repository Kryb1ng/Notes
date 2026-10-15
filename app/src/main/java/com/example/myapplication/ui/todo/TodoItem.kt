package com.example.myapplication.ui.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Task
import com.example.myapplication.ui.todo.TodoUiState.Companion.PRIORITY_HIGH
import com.example.myapplication.ui.todo.TodoUiState.Companion.PRIORITY_LOW
import com.example.myapplication.ui.todo.TodoUiState.Companion.PRIORITY_MEDIUM
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodoItem(
    task: Task,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(task.priority.toColor()),
        )
        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.combinedClickable(
                    onClick = onToggleComplete,
                    onLongClick = onEdit,
                ),
                softWrap = true,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
            )
            Text(
                text = formatDate(task.createdDate),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

private fun Int.toColor(): Color = when (this) {
    PRIORITY_LOW -> Color.Green
    PRIORITY_MEDIUM -> Color.Yellow
    PRIORITY_HIGH -> Color.Red
    else -> Color.Gray
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))