package com.example.myapplication.data

import com.example.myapplication.model.User
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun currentUser(): Flow<User?>
    suspend fun saveUser(user: User)
    suspend fun clearUser()
}