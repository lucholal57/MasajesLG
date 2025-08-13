package com.example.masajeslg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import com.example.masajeslg.ui.ClientsScreen
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import com.example.masajeslg.ui.AppNav
import com.example.masajeslg.ui.AppointmentsScreen
import com.example.masajeslg.ui.ServicesScreen
import com.example.masajeslg.ui.theme.MasajesLGTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MasajesLGTheme {
                AppNav()   // <- solo esto, SIN paréntesis extra
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasajesLGApp() {
    val navController = rememberNavController()
    MaterialTheme {
        Scaffold(
            topBar = {
                val canNavigateBack = navController.previousBackStackEntry != null
                TopAppBar(
                    title = { Text("MasajesLG - Agenda") },
                    navigationIcon = {
                        if (canNavigateBack) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                // usa el ícono auto‑espejado para RTL
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver"
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                AppNavHost(navController)
            }
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(
            onGoClients = { navController.navigate("clients") },
            onGoServices = { navController.navigate("services") },
            onGoAgenda = { navController.navigate("agenda") }
        ) }
        composable("clients") { ClientsScreen() }
        composable("services") { ServicesScreen() }
        composable("agenda") { AppointmentsScreen() }
    }
}

@Composable
fun HomeScreen(onGoClients: () -> Unit, onGoServices: () -> Unit, onGoAgenda: () -> Unit) {
    Surface {
        Column(Modifier.padding(16.dp)) {
            Text("Hola, Luciano 👋 — Vamos a construir esto paso a paso.")
            Spacer(Modifier.height(12.dp))
            Button(onClick = onGoClients) { Text("Ir a Clientes") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onGoServices) { Text("Ir a Servicios") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onGoAgenda) { Text("Ir a Agenda") }
        }
    }
}

@Preview
@Composable
private fun PreviewApp() {
    MasajesLGApp()
}