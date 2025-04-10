package com.example.intrapp.android

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.shared.VideoPlayer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.intrapp.Project
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow

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
        }
        //este cacho no tienen mucho sentido no? nunca va a pasar, se quedaria en loading eternamente  porque profileloaded no cambiaria su sestado
        //CAMBIAR A MANEJO DE ERROR CON UN TIEMOUT O ERROR (MAS ABAJO)
        else {
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
        composable("selected_project") {
            // Recuperamos el ID como un entero
            val projectId = navController.previousBackStackEntry?.savedStateHandle?.get<Int>("projectId") ?: -1
            SelectedProjectScreen(navController, projectId)
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
                modifier = Modifier, // Usa Modifier por defecto
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
                            // Abrir el navegador con la URL de OAuth
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

            // PROJECT BUTTON

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

        // BARRA DE NIVEL (es feisima, no la quiero d momento)

       Spacer(modifier = Modifier.height(50.dp))


       //profile.level?.let {
       //    ProgressBar(level = it, maxLevel = 21) // Usamos la barra de progreso
       //}


        }
    }
}
    //Necesito un boton de LOG OUT
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

        // PROJECTS
            if (!projectsLoaded) {
                LoadingScreen() // Pantalla de carga
            } else {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    if (projects != null) {
                        ScrollableCircularProjectCarousel(projects = projects, navController)

                    } else {
                        // Mostrar un mensaje de error si no hay proyectos
                        Text(text = "Proyectos no encontrados", color = Color.Black)
                    }
                }
            }


        //BOTON ATRAS
            ButtonBack(
                navController = navController,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(16.dp, 50.dp)
            )


        }
    }
}

@Composable
fun SelectedProjectScreen(
    navController: NavController,
    projectId: Int // ID del proyecto seleccionado
) {
    val project = SessionManager.userProfile?.projects?.find { it.project.id == projectId }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow)
    ) {
        if (project != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .background(Color.Black, CircleShape)
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = project.project.name,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // INFO
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp) // Más espacio
                ) {
                    Text(
                        text = "Final Mark: ${project.finalMark ?: "No available"}",
                        fontSize = 22.sp, // Texto más grande
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Status: ${project.status}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Updated At: ${project.updatedAt}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }


                Spacer(modifier = Modifier.height(50.dp))
            }
        } else {
            Text(
                text = "Proyecto no encontrado",
                color = Color.Red,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        //BOTON ATRAS

        ButtonBack(
            navController = navController,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(16.dp, 50.dp)
        )
    }
}


///////////////// COMPONENTES REUTILIZABLES ///////////////////

@Composable
fun ScrollableCircularProjectCarousel(projects: List<Project>, navController: NavController) {

    val listState = rememberLazyListState()    // Mantener el estado de la lista
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val itemHeight = 80.dp
    val circleHeight = 60.dp
    val padding = 12.dp

    // Para centrar
    val spacerHeight = (screenHeight - itemHeight) / 2

    // Calcular el índice para el seleccionado
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) return@derivedStateOf 0

            val center = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2

            var closestIndex = 0
            var minDistance = Int.MAX_VALUE

            for (itemInfo in visibleItemsInfo) {
                val itemCenter = itemInfo.offset + itemInfo.size / 2
                val distance = kotlin.math.abs(itemCenter - center)
                if (distance < minDistance) {
                    minDistance = distance
                    closestIndex = itemInfo.index
                }
            }

            closestIndex
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow)
    ) {
        // LazyColumn para los proyectos en carrusel
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                top = spacerHeight,
                bottom = spacerHeight
            )
        ) {
            itemsIndexed(projects) { index, project ->
                val isSelected = index == centerIndex

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = padding)
                            .height(itemHeight)
                            .background(Color.Black)
                            .clickable {
                                // Pasar el ID del proyecto como argumento de navegación
                                navController.navigate("selected_project") {
                                    launchSingleTop = true
                                    // Pasamos el ID como un entero
                                    navController.currentBackStackEntry?.savedStateHandle?.set("projectId", project.project.id)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = project.project.name,
                            color = Color.Yellow,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                            .size(circleHeight)
                            .background(Color.Black, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = project.project.name,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Efecto para desplazarse al primer ítem (índice 0)
    LaunchedEffect(projects) {
        if (projects.isNotEmpty()) {
            // Empezar con el primer proyecto seleccionado (índice 0)
            listState.scrollToItem(0)
        }
    }
}



//-------------------------//BOTONES//---------------------------//

@Composable
fun ButtonBack(navController: NavController, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .size(60.dp)
                .background(
                    color = Color.Black,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFFFFFC00), // Amarillo #FFFCC00
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun ProgressBar(level: Double, maxLevel: Int = 21, modifier: Modifier = Modifier) {
    // Calculamos el progreso como el nivel dividido por el nivel máximo
    val progress = level.toFloat() / maxLevel.toFloat()

    //BARRA GRIS
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .padding(horizontal = 16.dp) // Márgenes laterales para la barra
            .background(Color.DarkGray, RoundedCornerShape(10.dp)) // Fondo gris oscuro con esquinas redondeadas
    ) {
        //BARRA AMARILLA
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)  // barra amarilla basado en el progreso
                .background(Color.Yellow, RoundedCornerShape(10.dp))
        ) {
            Text(
                text = "$level",
                color = Color.Black,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.align(Alignment.Center) // Centra el texto dentro de la barra
            )
        }
    }
}








//ESTILOS DE TEXTO REUTILIZABLES



///MANEJO DE ERROR EN AUTH:
/* EN ProfileViewModel:
private val _authError = MutableStateFlow<String?>(null)
val authError: StateFlow<String?> = _authError

fun handleAuthCallback(code: String) {
    viewModelScope.launch {
        try {
            Api42().handleCallback(code)
            _profileLoaded.value = true
        } catch (e: Exception) {
            _authError.value = e.message // Guardar el mensaje de error
            _profileLoaded.value = false
        }
    }
}

EN App:
val authError by viewModel.authError.collectAsState()

LaunchedEffect(profileLoaded, authError) {
    if (profileLoaded) {
        navController.navigate("profile") {
            popUpTo("loading") { inclusive = true }
        }
    } else if (authError != null) {
        navController.navigate("login") {
            popUpTo("loading") { inclusive = true }
        }
    }

EN Loginscreen, mas bien en una nueva ErrorScreen (FUTUROOOOOOOOOO)
val authError by viewModel.authError.collectAsState()

if (authError != null) {
    Text(
        text = "Error: $authError",
        color = Color.Red
    )
}
 */