package com.example.myapplication.model

enum class TaskStatus(val displayName: String, val sortOrder: Int) {
    ACTIVE("Активное", 0),
    IN_PROGRESS("В работе", 1),
    COMPLETED("Выполнено", 2);

    companion object {
        /** Безопасное чтение из Firestore. */
        fun fromRaw(value: Any?): TaskStatus = when (value) {
            is String -> runCatching { valueOf(value) }.getOrDefault(ACTIVE)
            else -> ACTIVE
        }
    }
}