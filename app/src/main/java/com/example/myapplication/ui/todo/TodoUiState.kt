package com.example.myapplication.ui.todo

import com.example.myapplication.model.Task

data class TodoUiState(
    val textInput: String = "",
    val selectedPriority: Int = PRIORITY_MEDIUM,
    val showEmptyInputError: Boolean = false,
    val editDialog: EditDialogState? = null,
) {
    companion object {
        const val PRIORITY_LOW = 1
        const val PRIORITY_MEDIUM = 2
        const val PRIORITY_HIGH = 3
    }
}

data class EditDialogState(
    val task: Task,
    val editedText: String,
)