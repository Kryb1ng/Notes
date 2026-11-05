package com.example.myapplication.ui.common

import com.example.myapplication.model.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Форматирование даты
fun Long.formatAsDate(): String {
    return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(this))
}

// Сборка текста для журнала событий
fun Task.getCreatedText(): String =
    "Добавил: ${createdBy.ifBlank { "—" }} · ${createdDate.formatAsDate()}"

fun Task.getStartedText(): String? =
    if (startedBy != null && startedDate != null) {
        "Взял в работу: $startedBy · ${startedDate.formatAsDate()}"
    } else null

fun Task.getCompletedText(): String? =
    if (completedBy != null && completedDate != null) {
        "Выполнил: $completedBy · ${completedDate.formatAsDate()}"
    } else null

// Текст про фото
val Task.photoRequirementText: String?
    get() = if (requiresPhoto) "📷 Требуется фотоотчёт" else null