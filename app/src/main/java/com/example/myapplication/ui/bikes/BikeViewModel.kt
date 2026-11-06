package com.example.myapplication.ui.bikes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.myapplication.data.BikeRepository
import com.example.myapplication.model.Bike
import com.example.myapplication.model.User
import com.example.myapplication.model.UserRole
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BikeViewModel(
    private val repository: BikeRepository = BikeRepository(),
    val currentUser: User,
) : ViewModel() {

    val isAdmin: Boolean = currentUser.role == UserRole.ADMIN

    // 1. Сначала объявляем поток данных
    val bikes: StateFlow<List<Bike>> = repository.getAllBikes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Потом объявляем флаг загрузки
    var isLoading by mutableStateOf(true)
        private set

    // 3. И только теперь init, который подписывается на готовый StateFlow
    init {
        viewModelScope.launch {
            bikes.collect {
                isLoading = false
            }
        }
    }

    fun addBike(bike: Bike) {
        viewModelScope.launch { repository.insert(bike) }
    }

    fun updateBike(bike: Bike) {
        viewModelScope.launch { repository.update(bike) }
    }

    fun deleteBike(bike: Bike) {
        if (!isAdmin) return
        viewModelScope.launch { repository.delete(bike) }
    }
}