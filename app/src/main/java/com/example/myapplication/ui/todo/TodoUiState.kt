package com.example.myapplication.ui.todo

import android.net.Uri
import com.example.myapplication.model.Priority
import com.example.myapplication.model.Task

data class TodoUiState(
    val textInput: String = "",
    val selectedPriority: Priority = Priority.MEDIUM,
    val selectedFilter: TaskFilter = TaskFilter.ALL,
    val showAddDialog: Boolean = false,
    val showEmptyInputError: Boolean = false,
    val errorMessage: String? = null,
    val editDialog: EditDialogState? = null,
    val completeDialog: CompleteDialogState? = null,
)

data class EditDialogState(
    val task: Task,
    val editedText: String,
)

data class CompleteDialogState(
    val task: Task,
    val comment: String = "",
    val imageUri: Uri? = null,
    val isUploading: Boolean = false,
)