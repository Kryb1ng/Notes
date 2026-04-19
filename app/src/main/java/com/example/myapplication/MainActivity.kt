package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.combinedClickable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                TodoScreen()
                }
            }
        }
    }
data class Task(
    val text: String,
    val isCopmleted: Boolean = false,
    val createdDate: Long = System.currentTimeMillis()
)

@Composable
fun TodoScreen() {
    var textInput by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var editText by remember { mutableStateOf("") }

    val tasks = remember { mutableStateListOf<Task>() }
    fun openEditDialog(task: Task) {
        editingTask = task
        editText = task.text
        showEditDialog = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it},
                modifier = Modifier.weight(1f),
                label = { Text("Что нужно сделать?")},
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
                        if (textInput.isNotBlank()) {
                            tasks.add(Task(text = textInput))
                            textInput = ""
                        }
                        }
                        ) {
                    Text("Добавить")
                    }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (tasks.isEmpty()) {
            Text("Пока нет задач. Добавьте первую!")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    TodoItem(
                        task = task,
                        onToggleComplete = {
                            val index = tasks.indexOf(task)
                            if (index != -1) {
                                tasks [index] = task.copy(isCopmleted = !task.isCopmleted)
                            }
                        },
                        onDelete = {
                            tasks.remove(task)
                        },
                        onEdit = { openEditDialog(task)}
                    )
                }
            }
        }
        if (showEditDialog && editingTask != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false},
                title = { Text("Редактировать задачу") },
                text = {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        singleLine = true,
                        label = {Text("Новый текст")}
                    )
                },
                confirmButton = {
                TextButton(
                    onClick = {
                        if (editText.isNotBlank()) {
                            val index = tasks.indexOf(editingTask)
                            if (index != -1) {
                                tasks[index] = editingTask!!.copy(text = editText)
                            }
                        }
                        showEditDialog = false
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = {showEditDialog = false}) {
                    Text("Отмена")
                }
            }
            )
        }
    }
}

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
        Text(
            text = task.text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    onClick = { },
                    onLongClick = onEdit
                ),
            softWrap = true,
            textDecoration = if (task.isCopmleted) {
                androidx.compose.ui.text.style.TextDecoration.LineThrough
            } else {
                null
            }
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onToggleComplete) {
                Text(if (task.isCopmleted) "Вернуть" else "Готово")
            }
            Button(onClick = onDelete) {
                Text("Удалить")
            }
        }
    }
}