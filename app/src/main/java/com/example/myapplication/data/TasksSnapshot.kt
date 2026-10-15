package com.example.myapplication.data

import com.example.myapplication.model.Task

/**
 * Снимок списка задач из репозитория.
 * @param tasks — сами задачи.
 * @param isOffline — true, если данные пришли из локального кеша (нет связи).
 */
data class TasksSnapshot(
    val tasks: List<Task>,
    val isOffline: Boolean,
)