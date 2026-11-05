package com.example.myapplication.ui.todo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.model.Task
import com.example.myapplication.model.TaskStatus
import com.example.myapplication.ui.common.* // Импортируем наши новые утилиты
import com.example.myapplication.ui.todo.components.ImageViewerDialog

@Composable
fun TodoItem(
    task: Task,
    isAdmin: Boolean,
    canEdit: Boolean,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onRevert: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    var showImageViewer by rememberSaveable { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (task.isCompleted) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (task.isCompleted) Color(0xFFA5D6A7) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Полоса приоритета
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(task.priority.priorityColor())
            )

            Column(modifier = Modifier.padding(12.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = task.text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .weight(1f)
                            .combinedClickable(
                                onClick = { },
                                onLongClick = if (canEdit) onEdit else null,
                            ),
                        softWrap = true,
                    )

                    Spacer(Modifier.width(8.dp))
                    StatusChip(status = task.status)
                }
                Spacer(Modifier.height(8.dp))

                // Передаем готовые строки в UI-компонент
                TaskTimeline(task)

                if (task.isCompleted) {
                    task.reportComment?.let { comment ->
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Отчёт: $comment",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    task.reportImageUrl?.let { url ->
                        Spacer(Modifier.height(8.dp))
                        AsyncImage(
                            model = url,
                            contentDescription = "Фото отчёта",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showImageViewer = true },
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (task.status) {
                        TaskStatus.ACTIVE -> Button(onClick = onStart) { Text("Выполнить") }
                        TaskStatus.IN_PROGRESS -> Button(
                            onClick = onComplete,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC8E6C9),
                                contentColor = Color(0xFF2E7D32)
                            )
                        ) { Text("Готово") }
                        TaskStatus.COMPLETED -> OutlinedButton(onClick = onRevert) { Text("Вернуть") }
                    }
                    if (canEdit) {
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit, // Потребуется иконка Edit
                                contentDescription = "Редактировать",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (isAdmin) {
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    if (showImageViewer && task.reportImageUrl != null) {
        ImageViewerDialog(
            imageUrl = task.reportImageUrl,
            onDismiss = { showImageViewer = false },
        )
    }
}

@Composable
private fun StatusChip(status: TaskStatus) {
    val (containerColor, contentColor) = when (status) {
        TaskStatus.ACTIVE -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        TaskStatus.IN_PROGRESS -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        TaskStatus.COMPLETED -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
    }
    Surface(color = containerColor, shape = RoundedCornerShape(12.dp)) {
        Text(
            text = status.displayName,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun TaskTimeline(task: Task) {
    val lineStyle = MaterialTheme.typography.bodySmall
    val mutedColor = Color.Gray

    // 1. Кто добавил
    Text(
        text = task.getCreatedText(),
        style = lineStyle,
        color = mutedColor,
    )

    // 2. Кто взял в работу (если взял)
    task.getStartedText()?.let {
        Text(
            text = it,
            style = lineStyle,
            color = mutedColor,
        )
    }

    // 3. Кто выполнил (если выполнил)
    task.getCompletedText()?.let {
        Text(
            text = it,
            style = lineStyle,
            color = mutedColor,
        )
    }

    // 4. Требуется ли фото
    task.photoRequirementText?.let {
        Text(
            text = it,
            style = lineStyle,
            color = Color(0xFF666666),
        )
    }

    // 5. Дедлайн (если есть)
    task.deadline?.let { deadline ->
        val isOverdue = deadline < System.currentTimeMillis() && !task.isCompleted
        val dateStr = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(deadline))

        Text(
            text = if (isOverdue) "Просрочено: $dateStr" else "Срок: $dateStr",
            style = lineStyle,
            color = if (isOverdue) Color(0xFFD32F2F) else Color(0xFF1976D2),
            fontWeight = if (isOverdue) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
        )
    }
}