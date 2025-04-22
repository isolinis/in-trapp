package com.example.intrapp.android

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            when {
            profileLoaded -> {
                navController.navigate("profile") {
                    popUpTo("loading") { inclusive = true }
                }
            }
            else -> {
            //este cacho no tienen mucho sentido no? nunca va a pasar, se quedaria en loading eternamente  porque profileloaded no cambiaria su sestado
            // MANEJO DE ERROR CON UN TIEMOUT en Loadingscreen
            }
        }
    }

    // 5. Configurar NavHost
    NavHost(navController, startDestination = "login") {
        composable("loading") {
            LoadingScreen(
                onTimeout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
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
        composable("skills") {
            SkillsScreen(navController = navController, viewModel = viewModel<ProfileViewModel>())
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

        Box(modifier = Modifier.fillMaxSize().padding(0.dp)) {

            VideoPlayer(
                videoFileName = "loginvideo.mp4",
                modifier = Modifier.fillMaxSize(),
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
                            .offset(x = (-16).dp, y = (-100).dp), // Ajustar la posición
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
    val profileLoaded by viewModel.profileLoaded.collectAsState()
    val profile = SessionManager.userProfile

    // Si no hay perfil, redirigir
    if (profile == null) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
        return LoadingScreen()
    }

    // Aumenté el lineHeight y añadí más espacio entre elementos
    val profileTextStyle = TextStyle(
        color = Color.White,
        fontSize = 20.sp,
        fontFamily = FontFamily.Default,
        letterSpacing = 0.5.sp,
        lineHeight = 28.sp // Aumentado de 24 a 28.sp
    )

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .systemBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Sección superior
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Avatar (250.dp)
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .background(Color.Yellow, CircleShape)
                            .clip(CircleShape)
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = profile?.image?.link,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Información con más espacio entre líneas
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp) // Aumentado de 8 a 12.dp
                    ) {
                        Text(
                            text = profile!!.login,
                            color = Color.White,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp)) // Aumentado de 8 a 12.dp

                        Text(
                            text = "${profile.first_name} ${profile.last_name}",
                            style = profileTextStyle
                        )
                        Text(
                            text = "email: ${profile.email}",
                            style = profileTextStyle
                        )
                        Text(
                            text = "Level: ${profile.level}",
                            style = profileTextStyle
                        )
                        Text(
                            text = "Wallet: ${profile.wallet}",
                            style = profileTextStyle
                        )

                    }
                }

                // BOTONES
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Botón PROJECTS
                    Button(
                        onClick = {
                            viewModel.loadProjects()
                            navController.navigate("projects")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        ),
                        border = BorderStroke(2.dp, Color.Yellow),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "PROJECTS",
                            color = Color.Yellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    // Botón SKILLS
                    Button(
                        onClick = {
                            // Crea un scope de corrutina
                            val coroutineScope = CoroutineScope(Dispatchers.Main)
                            coroutineScope.launch {
                                try {
                                    navController.navigate("skills")
                                } catch (e: Exception) {
                                    Log.e("SkillsButton", "Error al cargar skills", e)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        ),
                        border = BorderStroke(2.dp, Color.Yellow),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "SKILLS",
                            color = Color.Yellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    // Botón LOGOUT
                    Box(
                        modifier = Modifier
                            .clickable {
                                CoroutineScope(Dispatchers.Main).launch {
                                    // 1. Navegar primero a login (fuera de la corrutina )
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }

                                    // 2. Limpiar datos
                                    delay(100) //  asegurar
                                    viewModel.resetState()
                                    SessionManager.clearSession()
                                    Log.d("LOGOUT", "Logout completado")
                                }
                            }
                            .size(110.dp) // Mismo tamaño que en iOS (100x100)
                            .background(Color.Yellow, CircleShape) // Fondo amarillo circular
                            .border(2.dp, Color.Black, CircleShape), // Borde negro circular
                        contentAlignment = Alignment.Center // Centra el texto
                    ) {
                        Text(
                            text = "LOG OUT",
                            color = Color.Black,
                            fontSize = 16.sp, // Tamaño de texto igual que en iOS
                            fontWeight = FontWeight.Black, // Texto en negrita
                            textAlign = TextAlign.Center // Texto centrado
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(onTimeout: () -> Unit = {}) {

    LaunchedEffect(Unit) {
        delay(30000) // 30 segundos timeout y fuera
        onTimeout()
    }

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

        Box(modifier = Modifier.fillMaxSize().background( Color(0xFFFFFC00))) {
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
                        text = "Updated At:\n ${project.updatedAt}",
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

@Composable
fun ScrollableCircularProjectCarousel(projects: List<Project>, navController: NavController) {

    val listState = rememberLazyListState()    // Mantener el estado de la lista
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val itemHeight = 100.dp
    val circleHeight = 80.dp
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

@Composable
fun SkillsScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val userProfile = SessionManager.userProfile

    // Verificamos si hay un perfil cargado
    if (userProfile == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Yellow),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Perfil no disponible",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        return
    }

    // Obtenemos las skills del cursus principal (42cursus - id: 21)
    val mainCursusSkills = userProfile.cursus_users
        .firstOrNull { it.cursus.id == 21 }
        ?.skills.orEmpty()
        .sortedByDescending { it.level }  // Ordenamos por nivel descendente

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow)
            .systemBarsPadding() // Evita que el contenido se dibuje bajo la barra de estado
            .padding(16.dp)
    ) {
        Text(
            text = "SKILLS",
            color = Color.Black,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally) // Añade esto para centrar horizontalmente
                .padding(top = 16.dp, bottom = 24.dp)
        )

        if (mainCursusSkills.isEmpty()) {
            // Si no hay skills, mostramos un mensaje
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No skills found",
                    color = Color.Black,
                    fontSize = 18.sp
                )
            }
        } else {
            // Si hay skills, mostramos las barras verticales
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // BARRAS
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp), // Más espacio entre barras
                    contentPadding = PaddingValues(horizontal = 16.dp), // Padding a los lados
                    verticalAlignment = Alignment.Bottom
                ) {
                    items(mainCursusSkills) { skill ->
                        val percentage = ((skill.level / 10f) * 100).toInt().coerceIn(0, 100)
                        VerticalSkillItem(name = skill.name, percentage = percentage)
                    }
                }
            }
        }

        // Botón para volver al perfil - con más margen inferior
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp), // Más espacio arriba y abajo
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "BACK",
                color = Color.Yellow,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun VerticalSkillItem(name: String, percentage: Int) {
    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) percentage / 100f else 0f,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp) // Barras más anchas (para que entren ~3 por pantalla)
    ) {
        // Contenedor de la barra - más ancho
        Box(
            modifier = Modifier
                .width(70.dp) // Barra más gruesa
                .height(300.dp) // Barra más alta
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(Color.DarkGray)
        ) {
            // Barra animada que se llena desde abajo
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(animatedProgress)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(Color.Black)
            ) {
                // Porcentaje en la parte superior de la barra de progreso
                if (percentage > 10) {
                    Text(
                        text = "$percentage%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    )
                }
            }
        }

        // Nombre de la skill debajo de la barra
        Text(
            text = name,
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(100.dp)
                .padding(top = 12.dp)
        )
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
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = Color(0xFFFFFC00), // Amarillo #FFFCC00
                modifier = Modifier.size(56.dp)
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