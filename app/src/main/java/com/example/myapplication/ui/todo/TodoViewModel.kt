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
import com.example.myapplication.model.User
import com.example.myapplication.model.UserRole
import com.example.myapplication.model.TaskStatus
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
import kotlinx.coroutines.flow.combine

class TodoViewModel(
    private val repository: TaskRepository,
    val currentUser: User,
) : ViewModel() {

    val isAdmin: Boolean = currentUser.role == UserRole.ADMIN

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    private val tasksSnapshot = repository.getAllTasks()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val tasks: StateFlow<List<Task>> = combine(
        tasksSnapshot,
        _uiState.map { it.selectedFilter }.distinctUntilChanged(),
    ) { snapshot, filter ->
        snapshot.tasks
            .filter { filter.matches(it) }
            .sortedWith(TASK_COMPARATOR)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isOffline: StateFlow<Boolean> = tasksSnapshot
        .map { it.isOffline }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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
            repository.insert(
                Task(
                    text = state.textInput,
                    priority = state.selectedPriority,
                    createdBy = currentUser.name,
                ),
            )
        }
        _uiState.update { it.copy(textInput = "", showEmptyInputError = false) }
    }

    fun onStartTask(task: Task) {
        launchCatching("Не удалось перевести задачу в работу") {
            repository.update(
                task.copy(
                    status = TaskStatus.IN_PROGRESS,
                    startedDate = System.currentTimeMillis(),
                    startedBy = currentUser.name,
                ),
            )
        }
    }

    fun onCompleteTask(task: Task) {
        launchCatching("Не удалось завершить задачу") {
            repository.update(
                task.copy(
                    status = TaskStatus.COMPLETED,
                    completedDate = System.currentTimeMillis(),
                    completedBy = currentUser.name,
                ),
            )
        }
    }

    fun onRevertTask(task: Task) {
        launchCatching("Не удалось вернуть задачу") {
            repository.update(
                task.copy(
                    status = TaskStatus.ACTIVE,
                    startedDate = null,
                    startedBy = null,
                    completedDate = null,
                    completedBy = null,
                ),
            )
        }
    }

    fun onFilterSelected(filter: TaskFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun onDeleteTask(task: Task) {
        if (!isAdmin) {
            _uiState.update { it.copy(errorMessage = "Удалять задачи может только администратор") }
            return
        }
        launchCatching("Не удалось удалить задачу") {
            repository.delete(task)
        }
    }

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

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

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
        private val TASK_COMPARATOR: Comparator<Task> =
            compareBy<Task> { it.status.sortOrder }
                .thenByDescending { it.priority.weight }
                .thenByDescending { it.createdDate }

        /** Фабрика: собирает ViewModel с текущим пользователем. */
        fun factory(user: User): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TodoViewModel(
                    repository = FirebaseTaskRepository(),
                    currentUser = user,
                )
            }
        }
    }
}