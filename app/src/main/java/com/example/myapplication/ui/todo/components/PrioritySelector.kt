package com.example.myapplication.ui.todo.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.myapplication.ui.todo.TodoUiState
import com.example.myapplication.ui.todo.TodoUiState.Companion.PRIORITY_HIGH
import com.example.myapplication.ui.todo.TodoUiState.Companion.PRIORITY_LOW
import com.example.myapplication.ui.todo.TodoUiState.Companion.PRIORITY_MEDIUM

@Composable
fun PrioritySelector(
    selectedPriority: Int,
    onPrioritySelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        PriorityChip(
            label = "Низкий",
            selected = selectedPriority == PRIORITY_LOW,
            selectedColor = Color.Green.copy(alpha = 0.7f),
            onClick = { onPrioritySelected(PRIORITY_LOW) },
        )
        PriorityChip(
            label = "Средний",
            selected = selectedPriority == PRIORITY_MEDIUM,
            selectedColor = Color.Yellow.copy(alpha = 0.7f),
            onClick = { onPrioritySelected(PRIORITY_MEDIUM) },
        )
        PriorityChip(
            label = "Высокий",
            selected = selectedPriority == PRIORITY_HIGH,
            selectedColor = Color.Red.copy(alpha = 0.7f),
            onClick = { onPrioritySelected(PRIORITY_HIGH) },
        )
    }
}

@Composable
private fun PriorityChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, color = Color.Black) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = selectedColor),
    )
}