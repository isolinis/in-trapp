package com.example.intrapp

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class Api42() {

    // Credenciales y URLs
    private val client_id: String = "u-s4t2ud-77006aca79f5d7f31a8a47f1ee21aaae7419d2fe992e37ad80c1877ba879de6e"
    private val redirect_uri: String = "intrap://auth/callback"
    private val client_secret: String = "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"
    private val uri: String = "https://api.intra.42.fr/oauth/authorize?client_id=${client_id}&redirect_uri=${redirect_uri}&response_type=code"

    // Tokens y datos de usuario (sera un objeto modelo en un futuro ? )
    var access_token: String? = null
    var refresh_token: String? = null
    var user_id: String?  = null

    //Devuelve URI de autorizacion de 42
    fun getURI(): String{
        return uri
    }

    // Maneja el callback: intercambia el code por el token y obtiene el perfil
    //Guarda token en la clase?? Y el profile??  (y devuelve?)
    suspend fun handleCallback(code: String): String {

        println("[API42] Iniciando manejo del callback con code: $code")

        //NECESITAMOS HACER LO DE LOS STATE , strings random para mas seguridad
        val state: String = ""

        return try {

            // Paso 1: Intercambiar el code por el token
            val tokenResponse = exchangeCodeForToken(code)
            if (tokenResponse == null) {
                throw Exception("Error: No se pudo obtener el token")
            }

            // Paso 2: Obtener el perfil del usuario
            val profileResponse = getProfile()
            if (profileResponse == null) {
                throw Exception("Error: No se pudo obtener el perfil")
            }

            // Paso 3: Extraer y guardar el ID del usuario (MODELO?)
            val profileBody = profileResponse.bodyAsText()
            val userId = profileBody.substringAfter("\"id\":").substringBefore(',').trim().removeSurrounding("\"")
            println("[API42] ID de usuario obtenido: $userId")

            // Paso 4: Obtener proyectos del usuario (MODELO?)
            val projectsResponse = getProjects()
            if (projectsResponse != null) {
                val projectsBody = projectsResponse.bodyAsText()
                println("[API42] Proyectos del usuario: $projectsBody")
            } else {
                println("[API42] Error: No se pudieron obtener los proyectos")
            }

            // Retornar el perfil del usuario
            profileBody

        } catch (e: Exception) {
            // Limpiar tokens en caso de error
            access_token = null
            refresh_token = null
            user_id = null

            // Log del error
            println("[API42] Error en handleCallback: ${e.message}")
            throw e // Relanzar la excepción para que el caller la maneje
        }
    }


    // Intercambia el code por el token de acceso
    private suspend fun exchangeCodeForToken(code: String): HttpResponse? {
        val url = "https://api.intra.42.fr/oauth/token"
        val body = "grant_type=authorization_code" +
                "&client_id=$client_id" +
                "&client_secret=$client_secret" +
                "&code=$code" +
                "&redirect_uri=$redirect_uri"

        println("[API42] POST: $url")
        println("[API42] Body: $body")

        return try {
            val response = ApiClient().post(url, body)
            println("[API42] RESPONSE: ${response?.status?.value}")

            if (response?.status?.value == 200) {
                val bodyText = response.bodyAsText()
                val jsonObject = Json.parseToJsonElement(bodyText).jsonObject

                access_token = jsonObject["access_token"]?.toString()?.replace("\"", "")
                refresh_token = jsonObject["refresh_token"]?.toString()?.replace("\"", "")

                println("[API42] Access Token: $access_token")
                println("[API42] Refresh Token: $refresh_token")

                response
            } else {
                println("[API42] Error en la respuesta: Código de estado ${response?.status?.value}")
                null
            }
        } catch (e: Exception) {
            println("[API42] Error en exchangeCodeForToken: ${e.message}")
            null
        }
    }

    // Función wrapper para Swift que llama a HandleCallback
    @Throws(Throwable::class)
    fun handleCallbackWrapper(code: String) {
        return runBlocking { // Crea un scope de corrutina
            handleCallback(code) // Llama a la función suspendida
        }
    }

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