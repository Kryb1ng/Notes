package com.example.myapplication.ui.todo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Priority

@Composable
fun AddTaskDialog(
    textValue: String,
    selectedPriority: Priority,
    showError: Boolean,
    onTextChange: (String) -> Unit,
    onPrioritySelected: (Priority) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая задача") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Что нужно сделать?") },
                    singleLine = true,
                    isError = showError,
                )
                if (showError) {
                    Text(
                        text = "Задача не может быть пустой",
                        color = Color.Red,
                        fontSize = 12.sp,
                    )
                }

                Text(
                    text = "Приоритет",
                    style = MaterialTheme.typography.titleSmall,
                )
                PrioritySelector(
                    selectedPriority = selectedPriority,
                    onPrioritySelected = onPrioritySelected,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Добавить задачу")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отменить")
            }
        },
    )
}