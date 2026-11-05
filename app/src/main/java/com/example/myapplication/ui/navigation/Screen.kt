package com.example.myapplication.ui.navigation

sealed class Screen(val route: String, val title: String) {
    data object Tasks : Screen("tasks", "Задачи")
    data object Calendar : Screen("calendar", "Календарь")
    data object Bikes : Screen("bikes", "Велосипеды")
}