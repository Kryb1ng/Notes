package com.example.myapplication.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.data.LocalSessionRepository
import com.example.myapplication.data.SessionRepository
import com.example.myapplication.model.User
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SessionViewModel(
    private val repository: SessionRepository,
) : ViewModel() {

    val state: StateFlow<SessionState> = repository.currentUser()
        .map { user ->
            if (user == null) SessionState.NoUser else SessionState.LoggedIn(user)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    fun login(user: User) {
        viewModelScope.launch { repository.saveUser(user) }
    }

    fun logout() {
        viewModelScope.launch { repository.clearUser() }
    }

    companion object {
        fun factory(context: android.content.Context): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    SessionViewModel(
                        repository = LocalSessionRepository(context.applicationContext),
                    )
                }
            }
    }
}