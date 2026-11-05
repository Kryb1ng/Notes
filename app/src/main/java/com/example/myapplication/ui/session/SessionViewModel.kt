package com.example.myapplication.ui.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.data.LocalSessionRepository
import com.example.myapplication.data.SessionRepository
import com.example.myapplication.model.AdminPassword
import com.example.myapplication.model.User
import com.example.myapplication.model.UserRole
import com.example.myapplication.ui.todo.capitalizeFirst
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SessionViewModel(
    private val repository: SessionRepository,
) : ViewModel() {

    val state: StateFlow<SessionState> = repository.currentUser()
        .map { user -> if (user == null) SessionState.NoUser else SessionState.LoggedIn(user) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Старый login(user: User) мы удалили

    fun login(name: String, role: UserRole, password: String) {
        if (name.isBlank()) {
            errorMessage = "Пожалуйста, введите имя"
            return
        }
        if (role == UserRole.ADMIN && password != AdminPassword.VALUE) {
            errorMessage = "Неверный пароль администратора"
            return
        }
        errorMessage = null
        val formattedName = name.trim().capitalizeFirst()

        viewModelScope.launch {
            repository.saveUser(User(formattedName, role))
        }
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