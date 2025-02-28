package com.example.intrapp.android                                                                                                   
                                                                                                                                      
import android.content.Intent
import android.os.Bundle
import android.util.Log                                                                                                               
import androidx.activity.ComponentActivity                                                                                            
import androidx.activity.compose.setContent                                                                                           
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.activity.viewModels
import com.example.intrapp.android.ProfileViewModel



class MainActivity : ComponentActivity() {

    // Declara viewModel
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración inicial de la UI
        setContent {
            App(viewModel) // Pasa viewModel a App
        }

        // Recoge los intent de Deep Link
        handleIntent(intent)
    }

    // Si la app estaba en segundo plano: se llama desde onNewIntent()
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("AuthIntra", "onNewIntent - Intent: ${intent?.data}")
        handleIntent(intent)
    }

    //Recibe el callback y procede a flujo WebFlowApplicaton de OAuth
    private fun handleIntent(intent: Intent?) {


        val uri = intent?.data ?: return //Intent sin data , probablemente launcher. No es callback
        val code = uri.getQueryParameter("code") ?: return //Extraer code

        Log.d("AuthIntra", "Authorization code received: $code")

            // Usar el ViewModel para manejar el callback:

            viewModel.handleAuthCallback(code)

            //CoroutineScope(Dispatchers.IO).launch {
                //val profile = Api42().handleCallback(code)
                //Log.d("AuthIntra", "Profile received: $profile")
            //}

        this.intent = Intent()
    }

}

@Preview
@Composable
fun AppAndroidPreview() {
    // Crea un ViewModel de prueba
    val viewModel = ProfileViewModel()

    // Llama a App con el ViewModel de prueba
    App(viewModel)
}











