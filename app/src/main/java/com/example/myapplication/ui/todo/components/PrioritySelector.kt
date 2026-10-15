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
import com.example.myapplication.model.Priority

@Composable
fun PrioritySelector(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Priority.entries.forEach { priority ->
            PriorityChip(
                label = priority.label(),
                selected = selectedPriority == priority,
                selectedColor = priority.color(),
                onClick = { onPrioritySelected(priority) },
            )
        }
    }
}

private fun Priority.label(): String = when (this) {
    Priority.LOW -> "Низкий"
    Priority.MEDIUM -> "Средний"
    Priority.HIGH -> "Высокий"
}

private fun Priority.color(): Color = when (this) {
    Priority.LOW -> Color.Green.copy(alpha = 0.7f)
    Priority.MEDIUM -> Color.Yellow.copy(alpha = 0.7f)
    Priority.HIGH -> Color.Red.copy(alpha = 0.7f)
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