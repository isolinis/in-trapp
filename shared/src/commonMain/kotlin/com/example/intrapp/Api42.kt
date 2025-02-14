package com.example.intrapp

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class Api42() {

    private val client_id: String = "u-s4t2ud-77006aca79f5d7f31a8a47f1ee21aaae7419d2fe992e37ad80c1877ba879de6e"
    private val redirect_uri: String = "intrap://auth/callback"
    private val client_secret: String = "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"
    private val uri: String = "https://api.intra.42.fr/oauth/authorize?client_id=${client_id}&redirect_uri=${redirect_uri}&response_type=code"

    var access_token: String? = null
    var refresh_token: String? = null
    var user_id: String?  = null

    //Devuelve URI de autorizacion de 42
    fun getURI(): String{
        return uri
    }

    //Recibe el callback de la uri con un code y lo cambia por token
    //Guarda token en la clase (y devuelve?)
    suspend fun handleCallback(code: String){

        //println("CODE: $code")

        //NECESITAMOS HACER LO DE LOS STATE , strings random para mas seguridad
        val state: String = ""

        //construir churro con code y state y mas movidas
        //y hacer el post a la API para cambiar code por token
        val response = ApiClient().post("https://api.intra.42.fr/oauth/token?grant_type=authorization_code&client_id=${client_id}&client_secret=${client_secret}&code=${code}&redirect_uri=${redirect_uri}")

        //Si la respuesta es (200 OK) extraer token del BODY
        if (response != null && response.status.value == 200) {

            val bodyText = response.bodyAsText()
            //println("Body raw: $bodyText")

            try {
                val jsonObject = Json.parseToJsonElement(bodyText).jsonObject
                val accessToken = jsonObject["access_token"]?.toString()?.replace("\"", "")
                val refreshToken = jsonObject["refresh_token"]?.toString()?.replace("\"", "")

                if (accessToken != null) {
                    println("Access Token extraído: $accessToken y refresh $refreshToken")
                    // Guardar el token en  variable de clase
                    access_token = accessToken
                    refresh_token = refreshToken


                    // Voy a Hacer la llamada a la API con el nuevo access_token, ya vere desde donde llamo luego, es una prueba

                    val profile = getProfile()
                    val body = profile?.bodyAsText()
                    user_id = body?.substringAfter("\"id\":")?.substringBefore(',')?.trim()?.removeSurrounding("\"")

                    println( "PROFILE: $body")
                    println( "ID : $user_id ")

                    val projects = getProjects()
                    val bodyproject = projects?.bodyAsText()
                    println( "PROJECT: $bodyproject Código de estado ${projects?.status?.value}")


                } else {
                    println("No se encontró el access_token en la respuesta")
                }
            } catch (e: Exception) {
                println("Error parseando JSON: ${e.message}")
            }
        } else {
            println("Error en la respuesta: Código de estado ${response?.status?.value}")
        }
    }

    // Función wrapper para Swift
    @Throws(Throwable::class)
    fun handleCallbackWrapper(code: String) {
        return runBlocking { // Crea un scope de corrutina
            handleCallback(code) // Llama a la función suspendida
        }
    }


    // Usa TOKEN para recibir info de perfil
    suspend fun getProfile() : HttpResponse? {
        val response: HttpResponse? = ApiClient().get(
            url = "https://api.intra.42.fr/v2/me",
            headers = mapOf(
                HttpHeaders.Authorization to "Bearer $access_token"
            )
        )
        return response
    }
    suspend fun getProjects() : HttpResponse? {
        val response: HttpResponse? = ApiClient().get(
            url = "https://api.intra.42.fr/v2/users/$user_id/projects_users",
            headers = mapOf(
                HttpHeaders.Authorization to "Bearer $access_token"
            )
        )
        return response
    }
}