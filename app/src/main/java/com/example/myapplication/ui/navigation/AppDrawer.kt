package com.example.myapplication.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.model.User

@Composable
fun AppDrawer(
    user: User,
    navController: NavController,
    onLogout: () -> Unit,
    closeDrawer: () -> Unit
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    ModalDrawerSheet {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Привет, ${user.name}!",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = "Роль: ${user.role.displayName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()

        NavigationDrawerItem(
            label = { Text(Screen.Tasks.title) },
            icon = { Icon(Icons.Default.List, contentDescription = null) },
            selected = currentRoute == Screen.Tasks.route,
            onClick = {
                navController.navigate(Screen.Tasks.route) { popUpTo(Screen.Tasks.route) { inclusive = true } }
                closeDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text(Screen.Calendar.title) },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
            selected = currentRoute == Screen.Calendar.route,
            onClick = {
                navController.navigate(Screen.Calendar.route)
                closeDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text(Screen.Bikes.title) },
            icon = { Icon(Icons.Default.DirectionsBike, contentDescription = null) },
            selected = currentRoute == Screen.Bikes.route,
            onClick = {
                navController.navigate(Screen.Bikes.route)
                closeDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(Modifier.weight(1f))
        HorizontalDivider()

        NavigationDrawerItem(
            label = { Text("Выйти") },
            selected = false,
            onClick = {
                closeDrawer()
                onLogout()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        Spacer(Modifier.height(16.dp))
    }
}