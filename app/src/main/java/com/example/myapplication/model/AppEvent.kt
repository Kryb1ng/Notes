package com.example.myapplication.model

import java.util.UUID

data class AppEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val isMonthlyRecurring: Boolean = false, // Повторять каждый месяц?
    val createdBy: String = "",
)