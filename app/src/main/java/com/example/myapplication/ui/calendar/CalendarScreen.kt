package com.example.myapplication.ui.calendar

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.model.AppEvent
import com.example.myapplication.model.User
import java.text.SimpleDateFormat
import java.util.*
import com.example.myapplication.ui.common.EmptyState
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun CalendarScreen(user: User) {
    val context = LocalContext.current

    // 1. Запрос разрешения на уведомления (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Можно показать Snackbar, если не дали */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // 2. Логика запроса на отключение оптимизации батареи
    var showBatteryDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            showBatteryDialog = true
        }
    }

    if (showBatteryDialog) {
        AlertDialog(
            onDismissRequest = { showBatteryDialog = false },
            title = { Text("Надежные напоминания") },
            text = { Text("Чтобы напоминания приходили вовремя, разрешите приложению работать в фоне без ограничений.") },
            confirmButton = {
                TextButton(onClick = {
                    showBatteryDialog = false
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                    context.startActivity(intent)
                }) { Text("Разрешить") }
            },
            dismissButton = {
                TextButton(onClick = { showBatteryDialog = false }) { Text("Позже") }
            }
        )
    }

    // 3. ViewModel и данные
    val viewModel: CalendarViewModel = viewModel(
        factory = viewModelFactory {
            initializer { CalendarViewModel(context = context, currentUser = user) }
        }
    )

    val events by viewModel.events.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (events.isEmpty()) {
            EmptyState(
                icon = Icons.Default.DateRange, // Или другую иконку
                title = "Нет событий",
                description = "Здесь будут отображаться ваши напоминания и дедлайны."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = events.sortedBy { it.dateMillis },
                    key = { it.id },
                    contentType = { "event_item" } // <--- ДОБАВИТЬ
                ) { event ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    ) {
                        EventCard(event, viewModel.isAdmin) { viewModel.deleteEvent(event) }
                    }
                }
            }
        }

        // Кнопка добавления видна ТОЛЬКО админу
        if (viewModel.isAdmin) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить событие")
            }
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, date, isMonthly ->
                viewModel.addEvent(title, date, isMonthly)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun EventCard(event: AppEvent, isAdmin: Boolean, onDelete: () -> Unit) {
    val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(event.dateMillis))

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (event.isMonthlyRecurring) "Каждый месяц, $dateStr" else dateStr,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            if (isAdmin) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddEventDialog(onDismiss: () -> Unit, onConfirm: (String, Long, Boolean) -> Unit) {
    var title by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var isMonthly by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("ОК") }
            }
        ) { DatePicker(state = datePickerState, showModeToggle = false) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новое событие") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = {
                            if (title.isNotBlank() && selectedDate != null) {
                                onConfirm(title, selectedDate!!, isMonthly)
                            }
                        }
                    )
                )

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    val text = if (selectedDate != null) {
                        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(selectedDate!!))
                    } else "Выбрать дату"
                    Text(text)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isMonthly, onCheckedChange = { isMonthly = it })
                    Spacer(Modifier.width(8.dp))
                    Text("Повторять каждый месяц", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedDate != null) {
                        onConfirm(title, selectedDate!!, isMonthly)
                    }
                },
                enabled = title.isNotBlank() && selectedDate != null
            ) { Text("Создать") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}