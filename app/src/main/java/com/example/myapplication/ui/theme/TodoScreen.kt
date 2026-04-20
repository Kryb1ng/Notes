package com.example.myapplication.ui.theme

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.model.Task
import com.example.myapplication.viewmodel.TodoViewModel

@Composable
fun TodoScreen() {
    val context = LocalContext.current
    val factory = remember {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return TodoViewModel(context.applicationContext as Application) as T
            }
        }
    }
    val viewModel: TodoViewModel = viewModel(factory = factory)

    val tasks by viewModel.tasks.collectAsState(initial = emptyList())

    // Локальное состояние для поля ввода
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Поле ввода и кнопка
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier.weight(1f),
                label = { Text("Что нужно сделать?") },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    disabledTextColor = Color.Black,
                    focusedLabelColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray
                )
            )
            Button(
                onClick = {
                    viewModel.updateTextInput(textInput)
                    viewModel.addTask()
                    textInput = "" // очищаем поле
                }
            ) {
                Text("Добавить")
            }
        }

        if (viewModel.showError) {
            Text("Задача не может быть пустой", color = Color.Red, fontSize = 12.sp)
        }

        // Выбор приоритета
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = viewModel.selectedPriority == 1,
                onClick = { viewModel.updateSelectedPriority(1) },
                label = { Text("Низкий", color = Color.Black) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.Green.copy(alpha = 0.7f)
                )
            )
            FilterChip(
                selected = viewModel.selectedPriority == 2,
                onClick = { viewModel.updateSelectedPriority(2) },
                label = { Text("Средний", color = Color.Black) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.Yellow.copy(alpha = 0.7f)
                )
            )
            FilterChip(
                selected = viewModel.selectedPriority == 3,
                onClick = { viewModel.updateSelectedPriority(3) },
                label = { Text("Высокий", color = Color.Black) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.Red.copy(alpha = 0.7f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Список задач
        if (tasks.isEmpty()) {
            Text("Пока нет задач. Добавьте первую!")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    TodoItem(
                        task = task,
                        onToggleComplete = { viewModel.toggleComplete(task) },
                        onDelete = { viewModel.deleteTask(task) },
                        onEdit = { viewModel.openEditDialog(task) }
                    )
                }
            }
        }

        // Диалог редактирования
        if (viewModel.showEditDialog && viewModel.editingTask != null) {
            AlertDialog(
                onDismissRequest = { viewModel.closeEditDialog() },
                title = { Text("Редактировать задачу") },
                text = {
                    OutlinedTextField(
                        value = viewModel.editText,
                        onValueChange = { viewModel.updateEditText(it) },
                        singleLine = true,
                        label = { Text("Новый текст") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.saveEdit() }) {
                        Text("Сохранить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeEditDialog() }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}
