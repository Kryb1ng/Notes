package com.example.myapplication.ui.todo

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.Priority
import com.example.myapplication.model.Task
import com.example.myapplication.model.User
import com.example.myapplication.ui.todo.components.AddTaskDialog
import com.example.myapplication.ui.todo.components.CompleteTaskDialog
import com.example.myapplication.ui.todo.components.EditTaskDialog
import com.example.myapplication.ui.todo.components.TaskFilterRow

@Composable
fun TodoScreen(
    user: User,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current

    val viewModel: TodoViewModel = viewModel(
        key = "${user.name}_${user.role.name}",
        factory = TodoViewModel.factory(context, user),
    )

    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()

    // Передаем всё в наш "тупой" UI-компонент
    TodoContent(
        tasks = tasks,
        uiState = uiState,
        isOffline = isOffline,
        currentUserName = user.name,
        isAdmin = viewModel.isAdmin,
        onTextChange = viewModel::onTextInputChange,
        onPrioritySelected = viewModel::onPrioritySelected,
        onFilterSelected = viewModel::onFilterSelected,
        onOpenAddDialog = viewModel::onOpenAddDialog,
        onDismissAddDialog = viewModel::onDismissAddDialog,
        onAddClick = viewModel::onAddTaskClick,
        onStart = viewModel::onStartTask,
        onComplete = viewModel::onOpenCompleteDialog,
        onCommentChange = viewModel::onCompleteCommentChange,
        onImageSelected = viewModel::onCompleteImageSelected,
        onConfirmComplete = viewModel::onConfirmComplete,
        onRequiresPhotoChange = viewModel::onRequiresPhotoChange,
        onDismissCompleteDialog = viewModel::onDismissCompleteDialog,
        onRevert = viewModel::onRevertTask,
        onDelete = viewModel::onDeleteTask,
        onEdit = viewModel::onEditTaskClick,
        onEditTextChange = viewModel::onEditTextChange,
        onSaveEdit = viewModel::onSaveEditClick,
        onDismissEdit = viewModel::onDismissEditDialog,
        onErrorDismissed = viewModel::onErrorDismissed,
        onDeadlineSelected = viewModel::onDeadlineSelected,
    )
}

@Composable
private fun TodoContent(
    tasks: List<Task>,
    uiState: TodoUiState,
    isOffline: Boolean,
    isAdmin: Boolean,
    currentUserName: String,
    onTextChange: (String) -> Unit,
    onPrioritySelected: (Priority) -> Unit,
    onFilterSelected: (TaskFilter) -> Unit,
    onOpenAddDialog: () -> Unit,
    onDismissAddDialog: () -> Unit,
    onAddClick: () -> Unit,
    onStart: (Task) -> Unit,
    onComplete: (Task) -> Unit,
    onCommentChange: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onConfirmComplete: () -> Unit,
    onRequiresPhotoChange: (Boolean) -> Unit,
    onDismissCompleteDialog: () -> Unit,
    onRevert: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    onEditTextChange: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onDismissEdit: () -> Unit,
    onErrorDismissed: () -> Unit,
    onDeadlineSelected: (Long?) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onErrorDismissed()
    }

    // Используем Box вместо Scaffold
    Box(modifier = Modifier.fillMaxSize()) {

        // Основной контент
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Твой стандартный отступ
        ) {
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

            TodoList(
                tasks = tasks,
                isAdmin = isAdmin,
                onStart = onStart,
                onComplete = onComplete,
                onRevert = onRevert,
                onDelete = onDelete,
                onEdit = onEdit,
                currentUserName = currentUserName,
            )
        }

        // Кнопка добавления (FAB) — прижимаем к правому нижнему углу
        FloatingActionButton(
            onClick = onOpenAddDialog,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("+", style = MaterialTheme.typography.headlineMedium)
        }

        // Snackbar — прижимаем к нижнему центру
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    TodoDialogs(
        uiState = uiState,
        onTextChange = onTextChange,
        onPrioritySelected = onPrioritySelected,
        onRequiresPhotoChange = onRequiresPhotoChange,
        onAddClick = onAddClick,
        onDismissAddDialog = onDismissAddDialog,
        onEditTextChange = onEditTextChange,
        onSaveEdit = onSaveEdit,
        onDismissEdit = onDismissEdit,
        onCommentChange = onCommentChange,
        onImageSelected = onImageSelected,
        onConfirmComplete = onConfirmComplete,
        onDismissCompleteDialog = onDismissCompleteDialog,
        onDeadlineSelected = onDeadlineSelected,
    )
}

/**
 * Вынесенный список задач
 */
@Composable
private fun TodoList(
    tasks: List<Task>,
    currentUserName: String,
    isAdmin: Boolean,
    onStart: (Task) -> Unit,
    onComplete: (Task) -> Unit,
    onRevert: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onEdit: (Task) -> Unit,
) {
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Пока нет задач. Нажмите «+», чтобы добавить первую.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tasks, key = { it.id }) { task ->
                val canEdit = task.createdBy == currentUserName || isAdmin
                TodoItem(
                    task = task,
                    isAdmin = isAdmin,
                    canEdit = canEdit,
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

/**
 * Вынесенный контейнер для диалоговых окон
 */
@Composable
private fun TodoDialogs(
    uiState: TodoUiState,
    onTextChange: (String) -> Unit,
    onPrioritySelected: (Priority) -> Unit,
    onRequiresPhotoChange: (Boolean) -> Unit,
    onAddClick: () -> Unit,
    onDismissAddDialog: () -> Unit,
    onEditTextChange: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onDismissEdit: () -> Unit,
    onCommentChange: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onConfirmComplete: () -> Unit,
    onDismissCompleteDialog: () -> Unit,
    onDeadlineSelected: (Long?) -> Unit,
) {
    // Диалог добавления
    if (uiState.showAddDialog) {
        AddTaskDialog(
            textValue = uiState.textInput,
            selectedPriority = uiState.selectedPriority,
            requiresPhoto = uiState.requiresPhoto,
            showError = uiState.showEmptyInputError,
            onTextChange = onTextChange,
            onPrioritySelected = onPrioritySelected,
            onRequiresPhotoChange = onRequiresPhotoChange,
            onConfirm = onAddClick,
            onDismiss = onDismissAddDialog,
            selectedDeadline = uiState.selectedDeadline,
            onDeadlineSelected = onDeadlineSelected,
        )
    }

    // Диалог редактирования
    uiState.editDialog?.let { dialog ->
        EditTaskDialog(
            text = dialog.editedText,
            onTextChange = onEditTextChange,
            onSave = onSaveEdit,
            onDismiss = onDismissEdit,
        )
    }

    // Диалог завершения
    uiState.completeDialog?.let { dialog ->
        CompleteTaskDialog(
            comment = dialog.comment,
            imageUri = dialog.imageUri,
            requiresPhoto = dialog.task.requiresPhoto,
            isUploading = dialog.isUploading,
            isSubmitEnabled = dialog.isSubmitEnabled,
            onCommentChange = onCommentChange,
            onImageSelected = onImageSelected,
            onConfirm = onConfirmComplete,
            onDismiss = onDismissCompleteDialog,
        )
    }
}