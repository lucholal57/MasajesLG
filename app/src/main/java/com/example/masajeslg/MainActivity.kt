package com.example.masajeslg

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.masajeslg.ui.AppNav
import com.example.masajeslg.ui.AppointmentsScreen
import com.example.masajeslg.ui.ClientsScreen
import com.example.masajeslg.ui.ServicesScreen
import com.example.masajeslg.ui.theme.MasajesLGTheme

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔔 Pedir permiso de notificaciones en Android 13+ (API 33)
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            val pm = androidx.core.content.ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (pm != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        setContent {
            MasajesLGTheme {
                AppNav() // tu navegación bottom
            }
        }
    }
}

// --- Si seguís usando estas previews/utilidades, dejalas como están ---
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MasajesLGApp() {
    val navController = rememberNavController()
    MaterialTheme {
        TopAppBar(
            title = { Text("MasajesLG - Agenda") },
            navigationIcon = {
                val canBack = navController.previousBackStackEntry != null
                if (canBack) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            }
        )
        AppNavHost(navController)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = "home") {
        composable("home") { /* ... tu HomeScreen si lo usás ... */ }
        composable("clients") { ClientsScreen() }
        composable("services") { ServicesScreen() }
        composable("agenda") { AppointmentsScreen() }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview @Composable
private fun PreviewApp() { MasajesLGApp() }
