package com.example.myapplication.ui.todo.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import java.util.UUID

@Composable
fun CompleteTaskDialog(
    comment: String,
    imageUri: Uri?,
    requiresPhoto: Boolean,
    isUploading: Boolean,
    onCommentChange: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> onImageSelected(uri) }

    var cameraUriString by rememberSaveable { mutableStateOf<String?>(null) }
    val cameraUri: Uri? = cameraUriString?.let(Uri::parse)

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) onImageSelected(cameraUri)
    }

    fun launchCamera() {
        val file = File(File(context.cacheDir, "images").apply { mkdirs() }, "photo_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        cameraUriString = uri.toString()
        takePictureLauncher.launch(uri)
    }

    val canConfirm = !isUploading && (!requiresPhoto || imageUri != null)

    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = { Text("Отчёт о выполнении", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = comment,
                    onValueChange = onCommentChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Комментарий (необязательно)") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp), // Единый стиль
                    enabled = !isUploading,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )

                // Заголовок фото-блока
                Text(
                    text = if (requiresPhoto) "Фото-отчёт (обязательно)" else "Фото-отчёт (опционально)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = if (requiresPhoto && imageUri == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )

                if (imageUri != null) {
                    // Превью фото в аккуратной карточке
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Прикреплённое фото",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                            // Кнопка "Убрать" поверх фото
                            Surface(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(bottomStart = 8.dp),
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                TextButton(
                                    onClick = { onImageSelected(null) },
                                    enabled = !isUploading,
                                ) {
                                    Text("Убрать", color = Color.White)
                                }
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            pickImageLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly,
                                ),
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isUploading,
                    ) {
                        Text("Из галереи")
                    }
                    OutlinedButton(
                        onClick = { launchCamera() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isUploading,
                    ) {
                        Text("Сделать фото")
                    }
                }

                if (isUploading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text("Загрузка фото...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = canConfirm) {
                Text("Завершить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUploading) {
                Text("Отмена")
            }
        },
    )
}