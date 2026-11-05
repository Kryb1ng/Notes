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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    user: User,
    onLogout: () -> Unit,
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
                composable(Screen.Calendar.route) { CalendarScreen() }
                composable(Screen.Bikes.route) { BikesScreen() }
            }
        }
    }
}