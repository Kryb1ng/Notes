package com.example.myapplication.ui.todo.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Priority
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization

@Composable
fun AddTaskDialog(
    textValue: String,
    selectedPriority: Priority,
    requiresPhoto: Boolean,
    showError: Boolean,
    onTextChange: (String) -> Unit,
    onPrioritySelected: (Priority) -> Unit,
    onRequiresPhotoChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая задача", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Что нужно сделать?") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp), // Единый стиль
                    isError = showError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                if (showError) {
                    Text(
                        text = "Задача не может быть пустой",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                    )
                }

                Text(
                    text = "Приоритет",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                PrioritySelector(
                    selectedPriority = selectedPriority,
                    onPrioritySelected = onPrioritySelected,
                )

                // Чекбокс фото-отчёта
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Checkbox(
                        checked = requiresPhoto,
                        onCheckedChange = onRequiresPhotoChange,
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Требуется фото-отчёт",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "При завершении нужно будет приложить фото",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
}