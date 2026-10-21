package com.example.myapplication.ui.session

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.AdminPassword
import com.example.myapplication.model.User
import com.example.myapplication.model.UserRole
import com.example.myapplication.ui.todo.capitalizeFirst
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
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()) // Чтобы можно было прокрутить при открытой клавиатуре
            .padding(24.dp),
        verticalArrangement = Arrangement.Center, // Центрируем по вертикали
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Приветственный блок
        Text(
            text = "С возвращением!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Представьтесь, чтобы мы знали, кто добавляет и выполняет задачи.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // Поле ввода имени
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; error = null },
            label = { Text("Ваше имя") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words
            )
        )

        Spacer(Modifier.height(24.dp))

        // Выбор роли в виде карточек
        Text(
            text = "Выберите роль:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RoleCard(
                role = UserRole.EMPLOYEE,
                isSelected = role == UserRole.EMPLOYEE,
                modifier = Modifier.weight(1f),
                onClick = { role = UserRole.EMPLOYEE; error = null }
            )
            RoleCard(
                role = UserRole.ADMIN,
                isSelected = role == UserRole.ADMIN,
                modifier = Modifier.weight(1f),
                onClick = { role = UserRole.ADMIN; error = null }
            )
        }

        // Поле пароля (появляется только для админа)
        if (role == UserRole.ADMIN) {
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = { Text("Пароль администратора") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Сообщение об ошибке
        error?.let {
            Spacer(Modifier.height(12.dp))
            Surface(
                color = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = it,
                    color = Color(0xFFB71C1C),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Кнопка входа
        Button(
            onClick = {
                when {
                    name.isBlank() -> error = "Пожалуйста, введите имя"
                    role == UserRole.ADMIN && password != AdminPassword.VALUE ->
                        error = "Неверный пароль администратора"
                    else -> {
                        val formattedName = name.trim().capitalizeFirst()
                        onComplete(User(formattedName, role))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Начать работу",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Карточка для выбора роли. Меняет цвет рамки и фона при выборе.
 */
@Composable
private fun RoleCard(
    role: UserRole,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface

    OutlinedCard(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = backgroundColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = role.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}