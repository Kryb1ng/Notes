package com.example.myapplication.ui.todo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.myapplication.ui.common.*
import com.example.myapplication.ui.theme.* // Импорт наших цветов
import com.example.myapplication.ui.todo.components.ImageViewerDialog
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState

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

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart && isAdmin) {
                onDelete() // Вызываем удаление только если свайпнули влево и если это админ
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false, // Запрещаем свайп слева направо
        enableDismissFromEndToStart = isAdmin, // Свайп справа налево только для админа
        backgroundContent = {
            // Красный фон с корзиной, который появляется при свайпе
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                // ИСПОЛЬЗУЕМ КОНСТАНТУ
                containerColor = if (task.isCompleted) TaskCompletedBg else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                // ИСПОЛЬЗУЕМ КОНСТАНТУ
                if (task.isCompleted) TaskCompletedBorder else MaterialTheme.colorScheme.outlineVariant
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
                            modifier = Modifier.weight(1f), // Оставили только weight
                            softWrap = true,
                        )

                        Spacer(Modifier.width(8.dp))
                        StatusChip(status = task.status)
                    }
                    Spacer(Modifier.height(8.dp))

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
                                // ИСПОЛЬЗУЕМ КОНСТАНТЫ
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ButtonCompleteBg,
                                    contentColor = ButtonCompleteText
                                )
                            ) { Text("Готово") }

                            TaskStatus.COMPLETED -> OutlinedButton(onClick = onRevert) { Text("Вернуть") }
                        }
                        if (canEdit) {
                            Spacer(Modifier.width(4.dp))
                            IconButton(onClick = onEdit) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Редактировать",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
}

    @Composable
    private fun StatusChip(status: TaskStatus) {
        // ИСПОЛЬЗУЕМ КОНСТАНТЫ
        val (containerColor, contentColor) = when (status) {
            TaskStatus.ACTIVE -> StatusActiveBg to StatusActiveText
            TaskStatus.IN_PROGRESS -> StatusInProgressBg to StatusInProgressText
            TaskStatus.COMPLETED -> StatusCompletedBg to StatusCompletedText
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

        Text(
            text = task.getCreatedText(),
            style = lineStyle,
            color = mutedColor,
        )

        task.getStartedText()?.let {
            Text(
                text = it,
                style = lineStyle,
                color = mutedColor,
            )
        }

        task.getCompletedText()?.let {
            Text(
                text = it,
                style = lineStyle,
                color = mutedColor,
            )
        }

        task.photoRequirementText?.let {
            Text(
                text = it,
                style = lineStyle,
                color = mutedColor,
            )
        }

        task.deadline?.let { deadline ->
            val isOverdue = deadline < System.currentTimeMillis() && !task.isCompleted
            val dateStr =
                java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date(deadline))

            Text(
                text = if (isOverdue) "Просрочено: $dateStr" else "Срок: $dateStr",
                style = lineStyle,
                // ИСПОЛЬЗУЕМ КОНСТАНТЫ
                color = if (isOverdue) BikeAlertRedBar else StatusActiveText,
                fontWeight = if (isOverdue) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
            )
        }
    }