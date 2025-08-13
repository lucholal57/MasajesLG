package com.example.masajeslg.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.masajeslg.ui.ClientsScreen
import com.example.masajeslg.ui.ServicesScreen
import com.example.masajeslg.ui.SoftBackground
import com.example.masajeslg.ui.SplashScreen
import com.example.masajeslg.ui.StatsScreen

data class NavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val items = listOf(
        NavItem("agenda",   "Agenda",       Icons.Filled.Event),
        NavItem("clients",  "Clientes",     Icons.Filled.Group),
        NavItem("services", "Servicios",    Icons.Filled.Build),
        NavItem("stats",    "Estadísticas", Icons.Filled.Insights)
    )

    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    // Fondo global (gradiente)
    Box(Modifier.fillMaxSize()) {
        // Solo dibuja el fondo, sin contenido
        SoftBackground(Modifier.fillMaxSize()) { }

        // UI encima, con Scaffold transparente
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (current != "splash") {
                    NavigationBar(tonalElevation = 8.dp) {
                        items.forEach { item ->
                            NavigationBarItem(
                                selected = current == item.route,
                                onClick = {
                                    if (current != item.route) {
                                        nav.navigate(item.route) { launchSingleTop = true }
                                    }
                                },
                                icon = { Icon(item.icon, null) },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = "splash",
                modifier = Modifier
                    .fillMaxSize()    // el contenido ocupa todo
                    .padding(padding) // respeta la bottom bar
            ) {
                composable("splash") {
                    SplashScreen {
                        nav.navigate("agenda") {
                            popUpTo("splash") { inclusive = true } // no volver a splash
                            launchSingleTop = true
                        }
                    }
                }
                composable("agenda")   { AppointmentsScreen() }
                composable("clients")  { ClientsScreen() }
                composable("services") { ServicesScreen() }
                composable("stats")    { StatsScreen() }
            }
        }
    }
}
