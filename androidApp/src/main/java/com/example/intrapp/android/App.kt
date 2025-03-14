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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.intrapp.SessionManager
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
import com.example.intrapp.Project
import java.io.File

//-------------------------//APP NAVEGADOR//---------------------------//

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
            ProfileScreen(navController, viewModel)
        }
        composable("projects") {
            ProjectsScreen(navController, viewModel)
        }
    }
}



//-------------------------//SCREENS//---------------------------//

@Composable
fun LoginScreen(navController: NavController, viewModel: ProfileViewModel) {

    val context = LocalContext.current
    var videoFinished by remember { mutableStateOf(false) }

    MaterialTheme {

        //VIDEO FONDO // Usa el @componente VideoPlayer

        Box(modifier = Modifier.fillMaxSize()) {

            VideoPlayer(
                videoFileName = "loginvideo.mp4", // Nombre del archivo de video
                onVideoFinished = { videoFinished = true } // Callback cuando el video termina
            )
            if (videoFinished) {

                //BOTON
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd // Alinear en la esquina inferior derecha
                ) {
                    Button(
                        onClick = {
                            // Navegar a la pantalla de carga
                            navController.navigate("loading")
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
                            containerColor = Color.Yellow
                        )
                    ) {
                        Text(
                            text = "LOG\nIN",
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel) {

    // Observar el estado de carga del perfil (y de los proyectos?)
    val profileLoaded by viewModel.profileLoaded.collectAsState()
    //val projectsLoaded by viewModel.projectsLoaded.collectAsState()

    // Observar reproduccion del video
    //var videoFinished by remember { mutableStateOf(false) }

    // Obtener el perfil (y los proyectos? )desde SessionManager
    val profile = SessionManager.userProfile
    //val projects = SessionManager.projects

    //val context = LocalContext.current


    //STYLES // Definir un TextStyle personalizado

    val profileTextStyle = TextStyle(
        color = Color.White,
        fontSize = 18.sp,
        //fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Default,
        letterSpacing = 0.5.sp
    )

    MaterialTheme {

        //VIDEO FONDO // Usa el @componente VideoPlayer

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // Usar el componente VideoPlayer
            //VideoPlayer(
            //    videoFileName = "profilevideo.mp4", // Nombre del archivo de video
            //    onVideoFinished = { videoFinished = true } // Callback cuando el video termina
            //)

            //if (videoFinished) {

                Column(
                    modifier = Modifier.fillMaxSize(),//.background(Color.Black),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center

                ) {

                // AVATAR
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(Color.Yellow, CircleShape)
                        .clip(CircleShape)
                        .background(Color.Black)
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
                text = " ${profile.first_name} ${profile.last_name} ",
                style = profileTextStyle
            )
            Text(text = "email: ${profile.email}", style = profileTextStyle)
            Text(
                text = "Location: ${profile.location ?: "No available"}",
                style = profileTextStyle
            )
            Text(text = "Wallet: ${profile.wallet}", style = profileTextStyle)

            Spacer(modifier = Modifier.height(50.dp))

            // PROJECTS

            Button(onClick = {
                // Navegar a la pantalla de carga
                //navController.navigate("loading")
                viewModel.loadProjects()
                navController.navigate("projects")
            },
                modifier = Modifier
                    .size(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Yellow
                )
                ) {
                Text(
                    text= "PROJECTS",
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center))
            }
            //}
        }
    }
}
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color.Black,
            modifier = Modifier.size(100.dp)
        )

    }
}

@Composable
fun ProjectsScreen(navController: NavController, viewModel: ProfileViewModel) {
    // Observar el estado de carga de proyectos
    val projectsLoaded by viewModel.projectsLoaded.collectAsState()

    // Obtener los proyectos desde SessionManager
    val projects = SessionManager.userProfile?.projects

    MaterialTheme {

        //VIDEO FONDO // Usa el @componente VideoPlayer

        Box(modifier = Modifier.fillMaxSize().background(Color.Yellow)) {
            // Usar el componente VideoPlayer??

            // Mostrar la pantalla de carga si los proyectos no están cargados
            if (!projectsLoaded) {
                LoadingScreen() // Pantalla de carga
            } else {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Mostrar la lista de proyectos si están disponibles
                    if (projects != null) {
                        LazyColumn {
                            items(projects) { project ->
                                ProjectItem(project = project)
                            }
                        }
                    } else {
                        // Mostrar un mensaje de error si no hay proyectos
                        Text(text = "Proyectos no encontrados", color = Color.Black)
                    }
                }
            }

            // PROJECTS

            Button(onClick = {
                // Navegar atras
                navController.navigate("profile")
            },
                modifier = Modifier
                    .size(70.dp) // Aumenta el tamaño del botón
                    .align(Alignment.TopStart)
                    .offset(30.dp, 50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                )
            ) {
                Text(
                    text= "<", //Tengo que poner un icono mas mono
                    color = Color.Yellow,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center))
            }
        }
    }
    //Necesito un boton de LOG OUT
}


///////////////// COMPONENTES REUTILIZABLES ///////////////////


//-------------------------//REPRODUCTOR DE VIDEO DE FONDO//---------------------------//

@Composable
fun VideoPlayer(
    videoFileName: String,
    modifier: Modifier = Modifier,
    onVideoFinished: () -> Unit = {} // Callback cuando el video termina
) {
    val context = LocalContext.current

    // Cargar el video desde shared
    val inputStream = context.assets.open("videos/$videoFileName")

    Log.d("VIDEO", "INPUT $videoFileName")

    // Obtener su URI (
    val videoUri = remember {

        if (inputStream != null) {
            // Video en un archivo temp (Por que está en commonMain/assets hace falta temp)
            val tempFile = File.createTempFile("video", ".mp4", context.cacheDir)

            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            Log.d("VIDEO", "Uri OK")
            Uri.fromFile(tempFile)
        } else {
            Log.d("VIDEO", "URI Empty")
            Uri.EMPTY //Por si no encuentra el recurso
        }
    }

    // ExoPlayer para reproducir el video
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
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
                    // Cuando el video termina, llamamos al callback
                    onVideoFinished()
                }
            }
        })
    }

    // Liberar el ExoPlayer cuando el componente se destruya
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

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
        modifier = modifier.fillMaxSize()
    )
}

//-------------------------//TARJETA DE PROYECTO//---------------------------//

@Composable
fun ProjectItem(project: Project) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)

    ) {
        Column(
            modifier = Modifier.padding(16.dp, 30.dp)
        ) {
            Text(text = project.project.name, fontWeight = FontWeight.Bold, color = Color.Yellow)
            //Text(text = project.project.description)

        }
    }
}

//-------------------------//BOTONES//---------------------------//

@Composable
fun ButtonBack(Text){
    //Este es el de back, con la flechita que quiero que sea mas mona, (negro con flechita amarilla)
}

@Composable
fun ButtonNext(Text){
    //Este es el de LOG IN y el de PROJECTS (amarillo y letrs negras )

}
