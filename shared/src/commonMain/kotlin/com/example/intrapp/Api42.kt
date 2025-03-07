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
    suspend fun handleCallback(code: String) : UserProfile {

        println("[API42] Iniciando manejo del callback con code: $code")

        //NECESITAMOS HACER LO DE LOS STATE , strings random para mas seguridad
        val state: String = ""

        try {
            // Paso 1: Intercambiar el code por el token
            val tokenResponse = exchangeCodeForToken(code)
            if (tokenResponse == null) {
                throw Exception("Error: No se pudo obtener el token")
            }

            // Paso 2: Obtener el perfil del usuario
            val userProfile = getProfile()
            return userProfile

            // Paso 4: Obtener proyectos del usuario (MODELO?)
            //val projectsResponse = getProjects()
            //if (projectsResponse != null) {
            //    val projectsBody = projectsResponse.bodyAsText()
            //    println("[API42] Proyectos del usuario: $projectsBody")
            //} else {
            //    println("[API42] Error: No se pudieron obtener los proyectos")
            //}



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

    suspend fun getProfile() : UserProfile {
        val response: HttpResponse? = ApiClient().get(
            url = "https://api.intra.42.fr/v2/me",
            headers = mapOf(
                HttpHeaders.Authorization to "Bearer $access_token"
            )
        )
        if (response == null || response.status.value != 200) {
            throw Exception("Error: No se pudo obtener el perfil")
        }

        // Parsear el JSON a UserProfile
        val profileJson = response.bodyAsText()
        println("[API42] USER PROFILE JSON: $profileJson")

        //Parsear a modelo de datos tipo UseProfile y añadir los access token
        //val userProfile = Json.decodeFromString<UserProfile>(profileJson)
        val userProfile = Json { ignoreUnknownKeys = true }.decodeFromString<UserProfile>(profileJson) // Ignora las claves que no están en el modelo
        userProfile.accessToken = access_token ?: throw Exception("Access token no disponible")
        userProfile.refreshToken = refresh_token ?: throw Exception("Refresh token no disponible")

        println("[API42] USER PROFILE MODEL: ${userProfile.id}, ${userProfile.login}, ${userProfile.email}, ${userProfile.location}, ${userProfile.wallet}\")")
        return userProfile
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

//{
//  "id": 1,
//  "name": "Welcome, Cadet !",
//  "description": "You have passed the C Piscine! Welcome to 42!",
//  "tier": "none",
//  "kind": "project",
//  "visible": true,
//  "image": "/uploads/achievement/image/1/PRO001.svg",
//  "nbr_of_success": null,
//  "users_url": "https://api.intra.42.fr/v2/achievements/1/users",
//  "titles": [
//    {
//      "id": 906,
//      "name": "#Creative %login"
//    },
//    {
//      "id": 975,
//      "name": "Enxaneta, Rebel, %login"
//    }
//  ],
//  "titles_users": [
//    {
//      "id": 12163,
//      "user_id": 89976,
//      "title_id": 906,
//      "selected": false,
//      "created_at": "2022-06-07T09:31:39.969Z",
//      "updated_at": "2022-10-28T20:50:03.010Z"
//    },
//    {
//      "id": 19492,
//      "user_id": 89976,
//      "title_id": 975,
//      "selected": true,
//      "created_at": "2022-12-16T16:05:28.412Z",
//      "updated_at": "2022-12-16T23:29:37.379Z"
//    }
//  ],
//  "partnerships": [],
//  "patroned": [],
//  "patroning": [
//    {
//      "id": 2890,
//      "user_id": 108388,
//      "godfather_id": 89976,
//      "ongoing": false,
//      "created_at": "2022-04-11T10:05:26.718Z",
//      "updated_at": "2022-04-11T10:05:27.096Z"
//    },
//    {
//      "id": 2891,
//      "user_id": 108391,
//      "godfather_id": 89976,
//      "ongoing": false,
//      "created_at": "2022-04-11T10:05:27.782Z",
//      "updated_at": "2022-04-11T10:05:28.278Z"
//    },
//    {
//      "id": 2892,
//      "user_id": 108394,
//      "godfather_id": 89976,
//      "ongoing": false,
//      "created_at": "2022-04-11T10:05:28.795Z",
//      "updated_at": "2022-04-11T10:05:29.258Z"
//    },
//    {
//      "id": 2893,
//      "user_id": 108405,
//      "godfather_id": 89976,
//      "ongoing": false,
//      "created_at": "2022-04-11T10:05:29.706Z",
//      "updated_at": "2022-04-11T10:05:31.471Z"
//    }
//  ],
//  "expertises_users": [],
//  "roles": [],
//  "campus": [
//    {
//      "id": 40,
//      "name": "Urduliz",
//      "time_zone": "Europe/Madrid",
//      "language": {
//        "id": 11,
//        "name": "Spanish",
//        "identifier": "es",
//        "created_at": "2019-08-09T15:14:32.544Z",
//        "updated_at": "2025-03-04T12:39:48.116Z"
//      },
//      "users_count": 2193,
//      "vogsphere_id": 32,
//      "country": "Spain",
//      "address": "Aita Gotzon Kalea 37",
//      "zip": "48610",
//      "city": "Urduliz",
//      "website": "https://42urduliz.com/",
//      "facebook": "",
//      "twitter": "https://twitter.com/42UrdulizFTef",
//      "active": true,
//      "public": true,
//      "email_extension": "42urduliz.com",
//      "default_hidden_phone": true
//    }
//  ],
//  "campus_users": [
//    {
//      "id": 81507,
//      "user_id": 89976,
//      "campus_id": 40,
//      "is_primary": true,
//      "created_at": "2021-06-07T10:36:54.272Z",
//      "updated_at": "2021-06-07T10:36:54.272Z"
//    }
//  ]
//}
