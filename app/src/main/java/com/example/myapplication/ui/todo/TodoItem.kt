package com.example.myapplication.ui.todo

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
import com.example.myapplication.model.TaskStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodoItem(
    task: Task,
    isAdmin: Boolean,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onRevert: () -> Unit,
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
                .background(task.priority.dotColor()),
        )
        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.combinedClickable(
                    onClick = when (task.status) {
                        TaskStatus.ACTIVE -> onStart
                        TaskStatus.IN_PROGRESS -> onComplete
                        TaskStatus.COMPLETED -> onRevert
                    },
                    onLongClick = onEdit,
                ),
                softWrap = true,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
            )

            Spacer(Modifier.height(4.dp))

            // Журнал событий задачи — показываем только то, что произошло
            TaskTimeline(task)

            Spacer(Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (task.status) {
                    TaskStatus.ACTIVE -> Button(onClick = onStart) { Text("Выполнить") }
                    TaskStatus.IN_PROGRESS -> Button(onClick = onComplete) { Text("Готово") }
                    TaskStatus.COMPLETED -> OutlinedButton(onClick = onRevert) { Text("Вернуть") }
                }
                if (isAdmin) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text("Удалить")
                    }
                }
            }
        }
    }
}

/**
 * Список событий: создание, взятие в работу, завершение.
 * Каждая строка появляется только если событие реально произошло.
 */
@Composable
private fun TaskTimeline(task: Task) {
    val lineStyle = MaterialTheme.typography.bodySmall
    val mutedColor = Color.Gray

    // Создание — всегда есть
    Text(
        text = "Добавил: ${task.createdBy.ifBlank { "—" }} · ${formatDate(task.createdDate)}",
        style = lineStyle,
        color = mutedColor,
    )

    // Взятие в работу — только если задача хоть раз была в IN_PROGRESS
    if (task.startedBy != null && task.startedDate != null) {
        Text(
            text = "Взял в работу: ${task.startedBy} · ${formatDate(task.startedDate)}",
            style = lineStyle,
            color = mutedColor,
        )
    }

    // Завершение — только если задача завершена
    if (task.completedBy != null && task.completedDate != null) {
        Text(
            text = "Выполнил: ${task.completedBy} · ${formatDate(task.completedDate)}",
            style = lineStyle,
            color = mutedColor,
        )
    }

    // Текущий статус — отдельной строкой, чтобы был виден и для ACTIVE/IN_PROGRESS
    Text(
        text = "Статус: ${task.status.displayName}",
        style = lineStyle,
        color = mutedColor,
    )
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))