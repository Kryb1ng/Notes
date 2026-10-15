package com.example.myapplication.ui.todo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.Priority
import com.example.myapplication.model.Task
import com.example.myapplication.model.User
import com.example.myapplication.ui.todo.components.AddTaskRow
import com.example.myapplication.ui.todo.components.EditTaskDialog
import com.example.myapplication.ui.todo.components.PrioritySelector
import com.example.myapplication.ui.todo.components.TaskFilterRow
@Composable
fun TodoScreen(
    user: User,
    onLogout: () -> Unit,
) {
    val viewModel: TodoViewModel = viewModel(
        key = user.name,                       // ← пересоздать при смене пользователя
        factory = TodoViewModel.factory(user),
    )

    val tasks by viewModel.tasks.collectAsStateWithLifecycle(initialValue = emptyList())
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()

    TodoContent(
        tasks = tasks,
        uiState = uiState,
        isOffline = isOffline,
        userName = user.name,
        isAdmin = viewModel.isAdmin,
        onTextChange = viewModel::onTextInputChange,
        onPrioritySelected = viewModel::onPrioritySelected,
        onFilterSelected = viewModel::onFilterSelected,          // ← новое
        onAddClick = viewModel::onAddTaskClick,
        onStart = viewModel::onStartTask,                        // ← вместо onToggleComplete
        onComplete = viewModel::onCompleteTask,                  // ←
        onRevert = viewModel::onRevertTask,                      // ←
        onDelete = viewModel::onDeleteTask,
        onEdit = viewModel::onEditTaskClick,
        onEditTextChange = viewModel::onEditTextChange,
        onSaveEdit = viewModel::onSaveEditClick,
        onDismissEdit = viewModel::onDismissEditDialog,
        onErrorDismissed = viewModel::onErrorDismissed,
        onLogout = onLogout,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoContent(
    tasks: List<Task>,
    uiState: TodoUiState,
    isOffline: Boolean,
    userName: String,
    isAdmin: Boolean,
    onTextChange: (String) -> Unit,
    onPrioritySelected: (Priority) -> Unit,
    onFilterSelected: (TaskFilter) -> Unit,                      // ← новое
    onAddClick: () -> Unit,
    onStart: (Task) -> Unit,                                     // ← вместо onToggleComplete
    onComplete: (Task) -> Unit,                                  // ←
    onRevert: (Task) -> Unit,                                    // ←
    onDelete: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    onEditTextChange: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onDismissEdit: () -> Unit,
    onErrorDismissed: () -> Unit,
    onLogout: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onErrorDismissed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задачи — $userName") },
                actions = {
                    TextButton(onClick = onLogout) { Text("Выйти") }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
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

            Spacer(Modifier.height(8.dp))

            TaskFilterRow(
                selected = uiState.selectedFilter,
                onFilterSelected = onFilterSelected,
            )

            if (isOffline) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFFFE0B2),
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
            }

            Spacer(Modifier.height(16.dp))

            if (tasks.isEmpty()) {
                Text("Пока нет задач. Добавьте первую!")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tasks, key = { it.id }) { task ->
                        TodoItem(
                            task = task,
                            isAdmin = isAdmin,
                            onStart = { onStart(task) },
                            onComplete = { onComplete(task) },
                            onRevert = { onRevert(task) },
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