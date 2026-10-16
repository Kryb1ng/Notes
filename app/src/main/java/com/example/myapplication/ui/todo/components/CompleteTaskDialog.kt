package com.example.myapplication.ui.todo.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.compose.runtime.saveable.rememberSaveable
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
        val file = File(context.cacheDir, "images").apply { mkdirs() }
            .let { File(it, "photo_${UUID.randomUUID()}.jpg") }
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
        title = { Text("Отчёт о выполнении") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = comment,
                    onValueChange = onCommentChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Комментарий (необязательно)") },
                    minLines = 2,
                    maxLines = 4,
                    enabled = !isUploading,
                )

                // Заголовок фото-блока с подписью о статусе обязательности
                Text(
                    text = if (requiresPhoto) "Фото-отчёт (обязательно)" else "Фото-отчёт (опционально)",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (requiresPhoto && imageUri == null) Color(0xFFB71C1C) else Color.Black,
                )

                if (imageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    ) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Прикреплённое фото",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        TextButton(
                            onClick = { onImageSelected(null) },
                            enabled = !isUploading,
                            modifier = Modifier.align(Alignment.TopEnd),
                        ) {
                            Text("Убрать", color = Color.White)
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
                        enabled = !isUploading,
                    ) {
                        Text("Из галереи")
                    }
                    OutlinedButton(
                        onClick = { launchCamera() },
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
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Text("Загрузка…", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = canConfirm) {
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