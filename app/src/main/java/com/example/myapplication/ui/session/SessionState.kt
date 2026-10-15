package com.example.myapplication.ui.session

import com.example.myapplication.model.User

sealed interface SessionState {
    data object Loading : SessionState
    data object NoUser : SessionState
    data class LoggedIn(val user: User) : SessionState
}