package com.example.intrapp.android

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.intrapp.Api42
import com.example.intrapp.android.ProfileViewModel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


@Composable
fun App(viewModel: ProfileViewModel) {
    // 1. Crear el NavController
    val navController = rememberNavController()

    // 2. Obtener el ViewModel
    val viewModel: ProfileViewModel = viewModel()

    // 3. Observar el estado de autenticación
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    // 4. Navegar entre pantallas : autentificado ->profile, default ->login
    if (isAuthenticated) {
        navController.navigate("profile")
    }

    // 5. Configurar NavHost
    NavHost(navController, startDestination = "login") {
        composable("login") {
            Log.d("NavHost", "login")
            LoginScreen(navController, viewModel)
        }
        composable("profile") {
            Log.d("NavHost", "profile")
            ProfileScreen(viewModel)
        }
    }
}

@Composable
fun LoginScreen(navController: NavController, viewModel: ProfileViewModel) {
    val context = LocalContext.current

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                // Iniciar flujo OAuth
                val url = Api42().getURI()
                Log.d("App", "URI for Intent: $url")
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(browserIntent)
            }) {
                Text("Log in with 42")
            }
        }
    }
}

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    // Observar el estado de autenticación
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isAuthenticated) {
                Text(text = "PERFIL",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold)
            } else {
                Text("No autenticado")
            }
        }
    }
}