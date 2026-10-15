package com.example.myapplication.ui.todo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.Task
import com.example.myapplication.ui.todo.components.AddTaskRow
import com.example.myapplication.ui.todo.components.EditTaskDialog
import com.example.myapplication.ui.todo.components.PrioritySelector

/**
 * Stateful-обёртка: знает про ViewModel, отдаёт всё «вниз» в stateless TodoContent.
 */
@Composable
fun TodoScreen(
    viewModel: TodoViewModel = viewModel(factory = TodoViewModel.Factory),
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle(initialValue = emptyList())
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TodoContent(
        tasks = tasks,
        uiState = uiState,
        onTextChange = viewModel::onTextInputChange,
        onPrioritySelected = viewModel::onPrioritySelected,
        onAddClick = viewModel::onAddTaskClick,
        onToggleComplete = viewModel::onToggleComplete,
        onDelete = viewModel::onDeleteTask,
        onEdit = viewModel::onEditTaskClick,
        onEditTextChange = viewModel::onEditTextChange,
        onSaveEdit = viewModel::onSaveEditClick,
        onDismissEdit = viewModel::onDismissEditDialog,
    )
}

/**
 * Stateless-контент: легко превьюить и тестировать.
 */
@Composable
private fun TodoContent(
    tasks: List<Task>,
    uiState: TodoUiState,
    onTextChange: (String) -> Unit,
    onPrioritySelected: (Int) -> Unit,
    onAddClick: () -> Unit,
    onToggleComplete: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    onEditTextChange: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onDismissEdit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
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

        Spacer(Modifier.height(16.dp))

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

    uiState.editDialog?.let { dialog ->
        EditTaskDialog(
            text = dialog.editedText,
            onTextChange = onEditTextChange,
            onSave = onSaveEdit,
            onDismiss = onDismissEdit,
        )
    }
}