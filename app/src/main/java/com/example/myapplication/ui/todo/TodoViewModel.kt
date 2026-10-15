package com.example.myapplication.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.data.FirebaseTaskRepository
import com.example.myapplication.data.TaskRepository
import com.example.myapplication.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TodoViewModel(
    private val repository: TaskRepository,
) : ViewModel() {

    val tasks: Flow<List<Task>> = repository.getAllTasks()

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    // --- Ввод и приоритет ---

    fun onTextInputChange(newText: String) {
        _uiState.update { it.copy(textInput = newText, showEmptyInputError = false) }
    }

    fun onPrioritySelected(priority: Int) {
        _uiState.update { it.copy(selectedPriority = priority) }
    }

    fun onAddTaskClick() {
        val state = _uiState.value
        if (state.textInput.isBlank()) {
            _uiState.update { it.copy(showEmptyInputError = true) }
            return
        }
        viewModelScope.launch {
            repository.insert(Task(text = state.textInput, priority = state.selectedPriority))
        }
        _uiState.update { it.copy(textInput = "", showEmptyInputError = false) }
    }

    // --- Действия над задачей ---

    fun onToggleComplete(task: Task) {
        viewModelScope.launch {
            repository.update(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun onDeleteTask(task: Task) {
        viewModelScope.launch { repository.delete(task) }
    }

    // --- Диалог редактирования ---

    fun onEditTaskClick(task: Task) {
        _uiState.update { it.copy(editDialog = EditDialogState(task, task.text)) }
    }

    fun onEditTextChange(newText: String) {
        _uiState.update { state ->
            val dialog = state.editDialog ?: return@update state
            state.copy(editDialog = dialog.copy(editedText = newText))
        }
    }

    fun onSaveEditClick() {
        val dialog = _uiState.value.editDialog ?: return
        if (dialog.editedText.isNotBlank()) {
            viewModelScope.launch {
                repository.update(dialog.task.copy(text = dialog.editedText))
            }
        }
        onDismissEditDialog()
    }

    fun onDismissEditDialog() {
        _uiState.update { it.copy(editDialog = null) }
    }

    companion object {
        /** Единая точка сборки зависимостей ViewModel. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TodoViewModel(repository = FirebaseTaskRepository())
            }
        }
    }
}
