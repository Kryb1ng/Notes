package com.example.myapplication.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.Task
import com.example.myapplication.model.TaskRepository

class TodoViewModel : ViewModel() {
    private val repository = TaskRepository()

    // Состояния UI, которые будут наблюдаться Compose
    private val _tasks = mutableStateListOf<Task>()   // реактивный список
    val tasks: List<Task> get() = _tasks

    var textInput by mutableStateOf("")
        private set
    var selectedPriority by mutableStateOf(2)
        private set
    var showError by mutableStateOf(false)
        private set

    // Для диалога редактирования
    var showEditDialog by mutableStateOf(false)
        private set
    var editingTask by mutableStateOf<Task?>(null)
        private set
    var editText by mutableStateOf("")
        private set

    fun updateEditText(newText: String) {
        editText = newText
    }

    init {
        // Загружаем задачи из репозитория (пока пусто)
        refreshTasks()
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
        repository.addTask(newTask)
        textInput = ""
        showError = false
        refreshTasks()
    }

    fun toggleComplete(task: Task) {
        val updated = task.copy(isCompleted = !task.isCompleted)
        repository.updateTask(updated)
        refreshTasks()
    }

    fun deleteTask(task: Task) {
        repository.deleteTask(task)
        refreshTasks()
    }

    fun openEditDialog(task: Task) {
        editingTask = task
        editText = task.text
        showEditDialog = true
    }

    fun saveEdit() {
        val task = editingTask ?: return
        if (editText.isNotBlank()) {
            val updated = task.copy(text = editText)
            repository.updateTask(updated)
        }
        closeEditDialog()
        refreshTasks()
    }

    fun closeEditDialog() {
        showEditDialog = false
        editingTask = null
        editText = ""
    }

    private fun refreshTasks() {
        _tasks.clear()
        _tasks.addAll(repository.getTasksSorted())
    }
}

