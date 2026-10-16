package com.example.myapplication.ui.todo

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.data.UploadedImage
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
                    requiresPhoto = state.requiresPhoto,   // ← передаём
                    createdBy = currentUser.name,
                ),
            )
        }
        _uiState.update {
            it.copy(
                textInput = "",
                selectedPriority = Priority.MEDIUM,
                requiresPhoto = false,
                showEmptyInputError = false,
                showAddDialog = false,
            )
        }
    }

    fun onRequiresPhotoChange(checked: Boolean) {
        _uiState.update { it.copy(requiresPhoto = checked) }
    }

    fun onDismissCompleteDialog() {
        _uiState.update { it.copy(completeDialog = null) }
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

    fun onOpenCompleteDialog(task: Task) {
        _uiState.update {
            it.copy(completeDialog = CompleteDialogState(task = task))
        }
    }

    fun onCompleteCommentChange(text: String) {
        _uiState.update { state ->
            val dialog = state.completeDialog ?: return@update state
            state.copy(completeDialog = dialog.copy(comment = text))
        }
    }

    fun onCompleteImageSelected(uri: Uri?) {
        _uiState.update { state ->
            val dialog = state.completeDialog ?: return@update state
            state.copy(completeDialog = dialog.copy(imageUri = uri))
        }
    }

    fun onConfirmComplete() {
        val dialog = _uiState.value.completeDialog ?: return
        val task = dialog.task

        // Если задача требует фото, а его нет — ошибка
        if (task.requiresPhoto && dialog.imageUri == null) {
            _uiState.update {
                it.copy(errorMessage = "Для этой задачи обязателен фото-отчёт")
            }
            return
        }

        _uiState.update {
            it.copy(completeDialog = it.completeDialog?.copy(isUploading = true))
        }

        launchCatching("Не удалось завершить задачу") {
            var uploadedImage: UploadedImage? = null

            dialog.imageUri?.let { uri ->
                uploadedImage = repository.uploadReportImage(task.id, uri)
            }

            repository.update(
                task.copy(
                    status = TaskStatus.COMPLETED,
                    completedDate = System.currentTimeMillis(),
                    completedBy = currentUser.name,
                    reportComment = dialog.comment.ifBlank { null },
                    reportImageUrl = uploadedImage?.downloadUrl,
                    reportImageStoragePath = uploadedImage?.storagePath,
                ),
            )
        }

        _uiState.update { it.copy(completeDialog = null) }
    }

    fun onRevertTask(task: Task) {
        launchCatching("Не удалось вернуть задачу") {
            // Удаляем файл из Storage, если он был
            task.reportImageStoragePath?.let { path ->
                runCatching { repository.deleteReportImage(path) }
            }

            repository.update(
                task.copy(
                    status = TaskStatus.ACTIVE,
                    startedDate = null,
                    startedBy = null,
                    completedDate = null,
                    completedBy = null,
                    reportComment = null,
                    reportImageUrl = null,
                    reportImageStoragePath = null,
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
                android.util.Log.e("TodoViewModel", "❌ $errorMessage", e)
                _uiState.update {
                    it.copy(errorMessage = "$errorMessage: ${e.message ?: e.javaClass.simpleName}")
                }
            }
        }
    }

    fun onOpenAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                textInput = "",
                selectedPriority = Priority.MEDIUM,
                requiresPhoto = false,
                showEmptyInputError = false,
            )
        }
    }

    fun onDismissAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = false,
                textInput = "",
                requiresPhoto = false,
                showEmptyInputError = false,
            )
        }
    }

    companion object {
        private val TASK_COMPARATOR: Comparator<Task> =
            compareBy<Task> { it.status.sortOrder }
                .thenByDescending { it.priority.weight }
                .thenByDescending { it.createdDate }

        fun factory(context: android.content.Context, user: User): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    TodoViewModel(
                        repository = FirebaseTaskRepository(context.applicationContext),
                        currentUser = user,
                    )
                }
            }
    }
}