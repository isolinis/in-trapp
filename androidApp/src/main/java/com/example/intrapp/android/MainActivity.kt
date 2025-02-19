package com.example.intrapp.android                                                                                                   
                                                                                                                                      
import android.content.Intent                                                                                                         
import android.net.Uri                                                                                                                
import android.os.Bundle
import android.util.Log                                                                                                               
import androidx.activity.ComponentActivity                                                                                            
import androidx.activity.compose.setContent                                                                                           
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview                                                                                    
import com.example.intrapp.Api42                                                                                                      
import kotlinx.coroutines.CoroutineScope                                                                                              
import kotlinx.coroutines.Dispatchers                                                                                                 
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración inicial de la UI
        setContent {

            App() }

        // Recoge los intent de Deep Link
        handleIntent(intent)
    }

    //Si la app estaba en segundo plano: se llama desde onNewIntent()
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("AuthIntra", "onNewIntent - Intent: ${intent?.data}")
        handleIntent(intent)
    }

    //Recibe el callback y procede a flujo WebFlowApplicaton de OAuth
    private fun handleIntent(intent: Intent?) {

        //Si el intent no tiene data , probablemente sea el launcher. No procede del callback
        val uri = intent?.data ?: return


        // Extraer code
        val code = uri.getQueryParameter("code")
        if (code != null) {
            Log.d("AuthIntra", "Authorization code received: $code")

            /// Llamar a Api42 para obtener token
            CoroutineScope(Dispatchers.IO).launch {
                val profile = Api42().handleCallback(code)
                Log.d("AuthIntra", "Profile received: $profile")

                // Actualizar UI cuando detectemos la data de profile con variable mutable:
                    //opcion 1 - variable mutabkestateof
                    //opcion 2 - abrir desde aqui otro viewmodel
                    //opcion 3 : ambas XD

            }

            this.intent = Intent()// Resetear el intent para evitar reprocesar
        } else {
            Log.e("AuthIntra", "No code parameter found in callback")
        }
    }

}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}











