package com.example.myapplication.ui.todo.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Priority
import java.text.SimpleDateFormat
import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    textValue: String,
    selectedPriority: Priority,
    requiresPhoto: Boolean,
    selectedDeadline: Long?,
    showError: Boolean,
    onTextChange: (String) -> Unit,
    onPrioritySelected: (Priority) -> Unit,
    onRequiresPhotoChange: (Boolean) -> Unit,
    onDeadlineSelected: (Long?) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var tempDateMillis by remember { mutableStateOf<Long?>(null) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDeadline)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    tempDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                    if (tempDateMillis != null) {
                        showTimePicker = true // Сразу открываем выбор времени
                    }
                }) { Text("Далее") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Выберите время") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    tempDateMillis?.let { date ->
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = date
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                            set(Calendar.SECOND, 0)
                        }
                        onDeadlineSelected(calendar.timeInMillis)
                    }
                    showTimePicker = false
                }) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Отмена") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая задача", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Что нужно сделать?") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = showError,
                )
                if (showError) {
                    Text("Задача не может быть пустой", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Text("Приоритет", style = MaterialTheme.typography.titleSmall)
                PrioritySelector(selectedPriority = selectedPriority, onPrioritySelected = onPrioritySelected)

                // --- БЛОК ДЕДЛАЙНА ---
                Text("Дедлайн (срок сдачи):", style = MaterialTheme.typography.titleSmall)
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    val text = if (selectedDeadline != null) {
                        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(selectedDeadline))
                    } else {
                        "Выбрать дату и время"
                    }
                    Text(text)
                }
                // ---------------------

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = requiresPhoto, onCheckedChange = onRequiresPhotoChange)
                    Spacer(Modifier.width(8.dp))
                    Text("Требуется фото-отчёт", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text("Добавить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}