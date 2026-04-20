package com.example.myapplication.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Task
import com.example.myapplication.model.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository(application)

    // Список задач из репозитория (StateFlow для Compose)
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    // UI-состояния (временные, не хранятся в БД)
    var textInput by mutableStateOf("")
        private set
    var selectedPriority by mutableStateOf(2)
        private set
    var showError by mutableStateOf(false)
        private set
    var showEditDialog by mutableStateOf(false)
        private set
    var editingTask by mutableStateOf<Task?>(null)
        private set
    var editText by mutableStateOf("")
        private set

    init {
        // Подписываемся на Flow из репозитория и обновляем StateFlow
        viewModelScope.launch {
            repository.allTasks.collect { taskList ->
                _tasks.value = taskList
            }
        }
    }

    fun updateTextInput(newText: String) {
        textInput = newText
        if (showError) showError = false
    }

    fun updateSelectedPriority(priority: Int) {
        selectedPriority = priority
    }

    fun addTask() {
        if (textInput.isBlank()) {
            showError = true
            return
        }
        val newTask = Task(
            text = textInput,
            priority = selectedPriority
        )
        viewModelScope.launch {
            repository.insert(newTask)
        }
        textInput = ""
        showError = false
    }

    fun toggleComplete(task: Task) {
        val updated = task.copy(isCompleted = !task.isCompleted)
        viewModelScope.launch {
            repository.update(updated)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }

    fun openEditDialog(task: Task) {
        editingTask = task
        editText = task.text
        showEditDialog = true
    }

    fun updateEditText(newText: String) {
        editText = newText
    }

    fun saveEdit() {
        val task = editingTask ?: return
        if (editText.isNotBlank()) {
            val updated = task.copy(text = editText)
            viewModelScope.launch {
                repository.update(updated)
            }
        }
        closeEditDialog()
    }

    fun closeEditDialog() {
        showEditDialog = false
        editingTask = null
        editText = ""
    }
}
