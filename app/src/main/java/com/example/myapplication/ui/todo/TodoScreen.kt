package com.example.myapplication.ui.todo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.Priority
import com.example.myapplication.model.Task
import com.example.myapplication.ui.todo.components.AddTaskRow
import com.example.myapplication.ui.todo.components.EditTaskDialog
import com.example.myapplication.ui.todo.components.PrioritySelector
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Surface

@Composable
fun TodoScreen(
    viewModel: TodoViewModel = viewModel(factory = TodoViewModel.Factory),
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle(initialValue = emptyList())
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()

    TodoContent(
        tasks = tasks,
        uiState = uiState,
        isOffline = isOffline,
        onTextChange = viewModel::onTextInputChange,
        onPrioritySelected = viewModel::onPrioritySelected,
        onAddClick = viewModel::onAddTaskClick,
        onToggleComplete = viewModel::onToggleComplete,
        onDelete = viewModel::onDeleteTask,
        onEdit = viewModel::onEditTaskClick,
        onEditTextChange = viewModel::onEditTextChange,
        onSaveEdit = viewModel::onSaveEditClick,
        onDismissEdit = viewModel::onDismissEditDialog,
        onErrorDismissed = viewModel::onErrorDismissed,   // ← новое
    )
}

@Composable
private fun TodoContent(
    tasks: List<Task>,
    uiState: TodoUiState,
    isOffline: Boolean,
    onTextChange: (String) -> Unit,
    onPrioritySelected: (Priority) -> Unit,
    onAddClick: () -> Unit,
    onToggleComplete: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    onEditTextChange: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onDismissEdit: () -> Unit,
    onErrorDismissed: () -> Unit,                          // ← новое
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Показываем Snackbar каждый раз, когда в состоянии появилось новое сообщение об ошибке
    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onErrorDismissed()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)      // ← отступы от Scaffold (заменяют systemBarsPadding)
                .padding(16.dp),            // ← контентный отступ
        ) {
            AddTaskRow(
                value = uiState.textInput,
                onValueChange = onTextChange,
                onAddClick = onAddClick,
                showError = uiState.showEmptyInputError,
            )

            Spacer(Modifier.height(8.dp))

            PrioritySelector(
                selectedPriority = uiState.selectedPriority,
                onPrioritySelected = onPrioritySelected,
            )

            Spacer(Modifier.height(16.dp))

            if (isOffline) {
                Surface(
                    color = Color(0xFFFFE0B2),                 // светлый оранжевый
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Оффлайн-режим. Изменения синхронизируются позже",
                        color = Color(0xFF8A4B00),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            if (tasks.isEmpty()) {
                Text("Пока нет задач. Добавьте первую!")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tasks, key = { it.id }) { task ->
                        TodoItem(
                            task = task,
                            onToggleComplete = { onToggleComplete(task) },
                            onDelete = { onDelete(task) },
                            onEdit = { onEdit(task) },
                        )
                    }
                }
            }
        }
    }

    uiState.editDialog?.let { dialog ->
        EditTaskDialog(
            text = dialog.editedText,
            onTextChange = onEditTextChange,
            onSave = onSaveEdit,
            onDismiss = onDismissEdit,
        )
    }
}