package com.example.myapplication.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.model.User
import com.example.myapplication.ui.bikes.BikesScreen
import com.example.myapplication.ui.calendar.CalendarScreen
import com.example.myapplication.ui.navigation.AppDrawer
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.todo.TodoScreen
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    user: User,
    onLogout: () -> Unit,
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }

    // Обработчик кнопки/свайпа "Назад"
    BackHandler {
        // Проверяем, нажали ли назад в течение последних 2 секунд (2000 мс)
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            // Закрываем приложение
            (context as? android.app.Activity)?.finish()
        } else {
            // Показываем подсказку
            Toast.makeText(
                context,
                "Нажмите «Назад» еще раз для выхода",
                Toast.LENGTH_SHORT
            ).show()
            backPressedTime = System.currentTimeMillis()
        }
    }
    // Получаем текущий маршрут, чтобы менять заголовок в шапке
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val title = when (currentRoute) {
        Screen.Tasks.route -> Screen.Tasks.title
        Screen.Calendar.route -> Screen.Calendar.title
        Screen.Bikes.route -> Screen.Bikes.title
        else -> "Notes"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                user = user,
                navController = navController,
                onLogout = onLogout,
                closeDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Открыть меню")
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Tasks.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Tasks.route) {
                    // Передаем только user и onLogout, шапку теперь рисует MainScreen
                    TodoScreen(user = user, onLogout = onLogout)
                }
                composable(Screen.Calendar.route) { CalendarScreen(user = user) }
                composable(Screen.Bikes.route) { BikesScreen(user = user) }
            }
        }
    }
}