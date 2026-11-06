package com.example.myapplication.ui.bikes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.model.Bike
import com.example.myapplication.model.BikeStatus
import com.example.myapplication.model.User
import com.example.myapplication.ui.theme.* // Импорт наших цветов
import com.example.myapplication.ui.common.EmptyState
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
@Composable
fun BikesScreen(user: User) {
    val viewModel: BikeViewModel = viewModel(
        factory = viewModelFactory {
            initializer { BikeViewModel(currentUser = user) }
        }
    )

    val bikes by viewModel.bikes.collectAsState()

    var showScanner by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var bikeToEdit by remember { mutableStateOf<Bike?>(null) }
    var scannedNumber by remember { mutableStateOf("") }

    if (showScanner) {
        BikeScannerScreen(
            onBack = { showScanner = false },
            onBikeScanned = { number ->
                showScanner = false
                scannedNumber = number
                showAddDialog = true
            }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (bikes.isEmpty()) {
            EmptyState(
                icon = Icons.Default.DirectionsBike,
                title = "Пока нет велосипедов",
                description = "Нажмите на кнопку сканера внизу, чтобы добавить первый байк в систему."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = bikes.sortedWith(compareBy({ it.status != BikeStatus.IN_REPAIR }, { it.number })),
                    key = { it.id },
                    contentType = { "bike_item" } // <--- ДОБАВИТЬ
                ) { bike ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    ) {
                        BikeCard(
                            bike = bike,
                            isAdmin = viewModel.isAdmin,
                            onEdit = { bikeToEdit = bike },
                            onDelete = { viewModel.deleteBike(bike) }
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(
                onClick = {
                    scannedNumber = ""
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(Icons.Default.Keyboard, contentDescription = "Ввести вручную")
            }

            FloatingActionButton(
                onClick = { showScanner = true }
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Сканировать номер")
            }
        }
    }

    if (showAddDialog || bikeToEdit != null) {
        val bike = bikeToEdit
        AddEditBikeDialog(
            bike = bike,
            initialNumber = if (bike == null) scannedNumber else "",
            onDismiss = {
                showAddDialog = false
                bikeToEdit = null
                scannedNumber = ""
            },
            onSave = { updatedBike ->
                if (bike == null) viewModel.addBike(updatedBike)
                else viewModel.updateBike(updatedBike)
                showAddDialog = false
                bikeToEdit = null
                scannedNumber = ""
            }
        )
    }
}

@Composable
fun BikeCard(bike: Bike, isAdmin: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    // ИСПОЛЬЗУЕМ КОНСТАНТЫ
    val statusColor = if (bike.status == BikeStatus.IN_REPAIR) BikeInRepairBg else BikeInShopBg
    val statusTextColor =
        if (bike.status == BikeStatus.IN_REPAIR) BikeInRepairText else BikeInShopText
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart && isAdmin) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = isAdmin,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = if (bike.hasRequest) BikeAlertCardBg else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (bike.hasRequest) BikeAlertCardBorder else MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                if (bike.hasRequest) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(6.dp)
                            .background(BikeAlertRedBar) // ИСПОЛЬЗУЕМ КОНСТАНТУ
                    )
                }

                Column(modifier = Modifier.padding(12.dp).weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = bike.number,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            if (bike.hasRequest) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    color = BikeInRepairBg, // ИСПОЛЬЗУЕМ КОНСТАНТУ
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "СЛОМАН",
                                        color = BikeInRepairText, // ИСПОЛЬЗУЕМ КОНСТАНТУ
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(
                                            horizontal = 6.dp,
                                            vertical = 2.dp
                                        )
                                    )
                                }
                            }
                        }

                        Surface(color = statusColor, shape = RoundedCornerShape(8.dp)) {
                            Text(
                                text = bike.status.displayName,
                                color = statusTextColor,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    if (bike.lastCourier.isNotBlank()) {
                        Text(
                            "Крайний курьер: ${bike.lastCourier}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(2.dp))
                    }

                    if (bike.comment.isNotBlank()) {
                        Text(
                            "Комментарий: ${bike.comment}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Редактировать",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun AddEditBikeDialog(
    bike: Bike?,
    initialNumber: String = "",
    onDismiss: () -> Unit,
    onSave: (Bike) -> Unit
) {
    val isEditMode = bike != null

    var number by remember { mutableStateOf(if (isEditMode) bike?.number ?: "" else initialNumber) }
    var status by remember { mutableStateOf(bike?.status ?: BikeStatus.IN_SHOP) }
    var hasRequest by remember { mutableStateOf(bike?.hasRequest ?: false) }
    var comment by remember { mutableStateOf(bike?.comment ?: "") }
    var lastCourier by remember { mutableStateOf(bike?.lastCourier ?: "") }

    // Выносим логику сохранения в отдельную функцию, чтобы не дублировать код
    val saveBike = {
        if (number.isNotBlank()) {
            onSave(
                bike?.copy(
                    number = number,
                    status = status,
                    hasRequest = hasRequest,
                    comment = comment,
                    lastCourier = lastCourier
                ) ?: Bike(
                    number = number
                )
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) "Редактировать велосипед" else "Новый велосипед") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = number,
                    onValueChange = { if (!isEditMode) number = it.uppercase() },
                    label = { Text(if (isEditMode) "Номер велосипеда" else "Введите или отсканируйте номер") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    readOnly = isEditMode,
                    enabled = !isEditMode,
                    colors = if (isEditMode) {
                        OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Gray,
                            disabledBorderColor = Color.LightGray,
                            disabledLabelColor = Color.Gray
                        )
                    } else {
                        OutlinedTextFieldDefaults.colors()
                    },
                    // --- ДОБАВИЛИ КНОПКУ "ГОТОВО" НА КЛАВИАТУРЕ ---
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { saveBike() })
                )

                if (isEditMode) {
                    Text("Статус:", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = status == BikeStatus.IN_SHOP,
                            onClick = { status = BikeStatus.IN_SHOP },
                            label = { Text("В Лавке") }
                        )
                        FilterChip(
                            selected = status == BikeStatus.IN_REPAIR,
                            onClick = { status = BikeStatus.IN_REPAIR },
                            label = { Text("В Ремонте") }
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = hasRequest, onCheckedChange = { hasRequest = it })
                        Spacer(Modifier.width(8.dp))
                        Text("Есть поломка?")
                    }

                    OutlinedTextField(
                        value = lastCourier,
                        onValueChange = { lastCourier = it },
                        label = { Text("Крайний курьер") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        // И сюда тоже можно добавить кнопку Готово
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { saveBike() })
                    )

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Комментарий") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        // И сюда, чтобы удобно было закрывать клавиатуру
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { saveBike() })
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { saveBike() },
                enabled = number.isNotBlank()
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}