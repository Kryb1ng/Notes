package com.example.myapplication.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.model.User
import com.example.myapplication.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Хранилище Preferences, привязанное к контексту приложения
private val Context.sessionDataStore by preferencesDataStore(name = "session")

class LocalSessionRepository(
    private val context: Context,
) : SessionRepository {

    override fun currentUser(): Flow<User?> = context.sessionDataStore.data.map { prefs ->
        val name = prefs[KEY_NAME]
        val roleRaw = prefs[KEY_ROLE]
        if (name != null && roleRaw != null) {
            runCatching { User(name, UserRole.valueOf(roleRaw)) }.getOrNull()
        } else null
    }

    override suspend fun saveUser(user: User) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_NAME] = user.name
            prefs[KEY_ROLE] = user.role.name
        }
    }

    override suspend fun clearUser() {
        context.sessionDataStore.edit { it.clear() }
    }

    private companion object {
        val KEY_NAME = stringPreferencesKey("user_name")
        val KEY_ROLE = stringPreferencesKey("user_role")
    }
}