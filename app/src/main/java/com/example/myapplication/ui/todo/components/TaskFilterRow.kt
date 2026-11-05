package com.example.myapplication.ui.todo.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.todo.TaskFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFilterRow(
    selected: TaskFilter,
    onFilterSelected: (TaskFilter) -> Unit,
) {
    val options = TaskFilter.entries

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, filter ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = { onFilterSelected(filter) },
                selected = selected == filter,
                label = { Text(filter.displayName) }
            )
        }
    }
}