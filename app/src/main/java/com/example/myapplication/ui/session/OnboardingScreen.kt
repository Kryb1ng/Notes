package com.example.myapplication.ui.session

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.AdminPassword
import com.example.myapplication.model.User
import com.example.myapplication.model.UserRole

@Composable
fun OnboardingScreen(
    onComplete: (User) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.EMPLOYEE) }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(32.dp))

        Text(
            text = "Добро пожаловать!",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Представьтесь, чтобы мы знали, кто добавляет и выполняет задачи.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; error = null },
            label = { Text("Ваше имя") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Роль:", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UserRole.entries.forEach { r ->
                FilterChip(
                    selected = role == r,
                    onClick = { role = r; error = null },
                    label = { Text(r.displayName) },
                )
            }
        }

        if (role == UserRole.ADMIN) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = { Text("Пароль администратора") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        error?.let {
            Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                when {
                    name.isBlank() -> error = "Введите имя"
                    role == UserRole.ADMIN && password != AdminPassword.VALUE ->
                        error = "Неверный пароль администратора"
                    else -> onComplete(User(name.trim(), role))
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Начать работу")
        }
    }
}