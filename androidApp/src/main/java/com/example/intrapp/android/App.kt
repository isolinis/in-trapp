package com.example.intrapp.android

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.intrapp.Api42
import com.example.intrapp.android.ProfileViewModel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import androidx.media3.common.MediaItem
import androidx.media3.common.Player



@Composable
fun App(viewModel: ProfileViewModel) {
    // 1. Crear el NavController
    val navController = rememberNavController()

    // 2. Obtener el ViewModel
    val viewModel: ProfileViewModel = viewModel()

    // 3. Observar el estado de autenticación
    val profileLoaded by viewModel.profileLoaded.collectAsState()

    // 4. Navegar entre pantallas : autentificado ->profile, default ->login
    LaunchedEffect(profileLoaded) {
        if (profileLoaded) {
            navController.navigate("profile") {
                popUpTo("loading") { inclusive = true } // Elimina la pantalla de loading del backstack
            }
        } else {
            navController.navigate("login") {
                popUpTo("loading") { inclusive = true } // Elimina la pantalla de loading del backstack
            }
        }
    }

    // 5. Configurar NavHost
    NavHost(navController, startDestination = "login") {
        composable("loading") {
            LoadingScreen() // Pantalla de carga
        }
        composable("login") {
            LoginScreen(navController, viewModel)
        }
        composable("profile") {
            ProfileScreen(viewModel)
        }
    }
}

@Composable
fun LoginScreen(navController: NavController, viewModel: ProfileViewModel) {
    val context = LocalContext.current

    var videoFinished by remember { mutableStateOf(false) }

    // ExoPlayer for video
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.animacion}")
            val mediaItem = MediaItem.fromUri(videoUri)
            setMediaItem(mediaItem)
            repeatMode = ExoPlayer.REPEAT_MODE_OFF // Desactivar el bucle
            prepare()
            play()
        }
    }

    // Detener el video en el último frame
    LaunchedEffect(exoPlayer) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    // Cuando el video termina, actualizamos el estado
                    videoFinished = true
                }
            }
        })
    }

    // Liberar el ExoPlayer cuando la pantalla se destruya
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    MaterialTheme {

        //FONDO
        // Reproducir el video de fondo
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false // Ocultar controles de reproducción
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        if (videoFinished) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd // Alinear en la esquina inferior derecha
            ) {
                Button(onClick = {
                    // Iniciar flujo OAuth
                    val url = Api42().getURI()
                    Log.d("App", "URI for Intent: $url")
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(browserIntent)
                },
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-16).dp, y = (-150).dp), // Ajustar la posición
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    )
                ) {
                    Text(
                        text = "LOG\nIN",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    // Observar el estado de autenticación, y otras variables que luego vengan ( nombre, email, avatar)
    val profile by viewModel.profile.collectAsState()

    // Definir un TextStyle personalizado
    val profileTextStyle = TextStyle(
        color = Color.White,
        fontSize = 18.sp,
        //fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Default,
        letterSpacing = 0.5.sp
    )

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // AVATAR
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(Color.Gray, CircleShape)
                    .clip(CircleShape)
            ) {
                // Imagen
                AsyncImage(
                    model = profile!!.image?.link,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop, // Ajusta la imagen al círculo
                    //placeholder = painterResource(R.drawable.placeholder), // Imagen de placeholder mientras carga
                    //error = painterResource(R.drawable.error_image) // Imagen de error si falla la carga
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            //INFO
            Text(
                text = profile!!.login,
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = " ${profile!!.first_name} ${profile!!.last_name} ",
                style = profileTextStyle
            )
            Text(
                text = "email: ${profile!!.email}",
                style = profileTextStyle
            )
            Text(
                text = "Location: ${profile!!.location ?: "No available"}",
                style = profileTextStyle
            )
            Text(
                text = "Wallet: ${profile!!.wallet}",
                style = profileTextStyle
            )

            Spacer(modifier = Modifier.height(50.dp))

            // PROJECTS

            Button(onClick = {
                //Aqui no se si desplegar otra screeen de projects,
                // para ello si usar otra variable observable o simeplemtne por click
                //val projects  = Api42().getProjects()
                //LANZAR LA PETICION A LA API deberia hacerlo el viewmodel , y otro objeto ???
            }) {
                Text("PROJECTS")
            }


        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Fondo blanco (puedes cambiarlo)
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "CARGANDO",
            color = Color.White,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default,
            letterSpacing = 0.5.sp
        )
        // Puedes usar un indicador de progreso o una animación
        //CircularProgressIndicator(color = Color.Blue) // Indicador de carga circular
    }
}