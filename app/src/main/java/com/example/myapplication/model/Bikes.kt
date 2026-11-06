package com.example.myapplication.model

import java.util.UUID

enum class BikeStatus(val displayName: String) {
    IN_SHOP("В Лавке"),
    IN_REPAIR("В Ремонте")
}

data class Bike(
    val id: String = UUID.randomUUID().toString(),
    val number: String = "", // Например, YY001K
    val status: BikeStatus = BikeStatus.IN_SHOP,
    val hasRequest: Boolean = false,
    val comment: String = "",
    val lastCourier: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)