package com.example.myapplication.ui.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.EventRepository
import com.example.myapplication.model.AppEvent
import com.example.myapplication.model.User
import com.example.myapplication.utils.ReminderScheduler // <--- ИМПОРТ
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val repository: EventRepository = EventRepository(),
    private val context: Context, // <--- ДОБАВИЛИ CONTEXT
    val currentUser: User,
) : ViewModel() {

    val isAdmin: Boolean = currentUser.role == com.example.myapplication.model.UserRole.ADMIN

    val events: StateFlow<List<AppEvent>> = repository.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEvent(title: String, dateMillis: Long, isMonthly: Boolean) {
        if (title.isBlank()) return

        val newEvent = AppEvent(
            title = title,
            dateMillis = dateMillis,
            isMonthlyRecurring = isMonthly,
            createdBy = currentUser.name
        )

        viewModelScope.launch {
            repository.insert(newEvent)
            // Планируем будильник!
            ReminderScheduler.scheduleReminder(context, newEvent)
        }
    }

    fun deleteEvent(event: AppEvent) {
        if (!isAdmin) return
        viewModelScope.launch {
            repository.delete(event)
            // В идеале здесь нужно отменить будильник (AlarmManager.cancel)
        }
    }
}