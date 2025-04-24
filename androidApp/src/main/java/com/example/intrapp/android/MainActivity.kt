package com.example.intrapp.android                                                                                                   
                                                                                                                                      
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity                                                                                            
import androidx.activity.compose.setContent                                                                                           
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.intrapp.ApiClient
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Observa los errores de autenticación
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authError.collect { error ->
                    error?.let {
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                        viewModel.clearAuthError()
                    }
                }
            }
        }



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

        //Caso de Intent sin data , probablemente launcher. No es callback
        val uri = intent?.data ?: return

        //Si es callback, extrae CODE y lo manda a VIEWMODEL
        val code = uri.getQueryParameter("code") ?: return //Extraer code
        Log.d("AuthIntra", "Authorization code received: $code")
        viewModel.handleAuthCallback(code)

        this.intent = Intent()
    }

    override fun onDestroy() {
        super.onDestroy()
        ApiClient().close() // <- Cierra el cliente aquí
        Log.d("[APP]", "HttpClient cerrado")
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











