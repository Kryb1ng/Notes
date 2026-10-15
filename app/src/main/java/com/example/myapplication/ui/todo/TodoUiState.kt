package com.example.myapplication.ui.todo

import com.example.myapplication.model.Priority
import com.example.myapplication.model.Task

data class TodoUiState(
    val textInput: String = "",
    val selectedPriority: Priority = Priority.MEDIUM,
    val showEmptyInputError: Boolean = false,
    val errorMessage: String? = null,
    val editDialog: EditDialogState? = null,
)

data class EditDialogState(
    val task: Task,
    val editedText: String,
)