package com.example.myapplication.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.data.FirebaseTaskRepository
import com.example.myapplication.data.TaskRepository
import com.example.myapplication.model.Priority
import com.example.myapplication.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TodoViewModel(
    private val repository: TaskRepository,
) : ViewModel() {

    /** Общая подписка на Firestore — одна на всех потребителей. */
    private val tasksSnapshot = repository.getAllTasks()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    /** Отсортированный список задач для UI. */
    val tasks: StateFlow<List<Task>> = tasksSnapshot
        .map { snapshot -> snapshot.tasks.sortedWith(TASK_COMPARATOR) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Флаг «оффлайн-режим». */
    val isOffline: StateFlow<Boolean> = tasksSnapshot
        .map { it.isOffline }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    // --- Ввод и приоритет ---

    fun onTextInputChange(newText: String) {
        _uiState.update { it.copy(textInput = newText, showEmptyInputError = false) }
    }

    fun onPrioritySelected(priority: Priority) {
        _uiState.update { it.copy(selectedPriority = priority) }
    }

    fun onAddTaskClick() {
        val state = _uiState.value
        if (state.textInput.isBlank()) {
            _uiState.update { it.copy(showEmptyInputError = true) }
            return
        }
        launchCatching("Не удалось добавить задачу") {
            repository.insert(Task(text = state.textInput, priority = state.selectedPriority))
        }
        _uiState.update { it.copy(textInput = "", showEmptyInputError = false) }
    }

    // --- Действия над задачей ---

    fun onToggleComplete(task: Task) {
        launchCatching("Не удалось обновить задачу") {
            repository.update(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun onDeleteTask(task: Task) {
        launchCatching("Не удалось удалить задачу") {
            repository.delete(task)
        }
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
            launchCatching("Не удалось сохранить изменения") {
                repository.update(dialog.task.copy(text = dialog.editedText))
            }
        }
        onDismissEditDialog()
    }

    fun onDismissEditDialog() {
        _uiState.update { it.copy(editDialog = null) }
    }

    // --- Ошибки ---

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // --- Утилиты ---

    private fun launchCatching(
        errorMessage: String,
        block: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = errorMessage) }
            }
        }
    }

    companion object {
        /**
         * Сортировка по умолчанию:
         * 1) невыполненные сверху,
         * 2) внутри группы — по убыванию приоритета (HIGH → MEDIUM → LOW),
         * 3) при равенстве приоритета — свежие сверху.
         */
        private val TASK_COMPARATOR: Comparator<Task> =
            compareBy<Task> { it.isCompleted }
                .thenByDescending { it.priority.weight }
                .thenByDescending { it.createdDate }

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TodoViewModel(repository = FirebaseTaskRepository())
            }
        }
    }
}