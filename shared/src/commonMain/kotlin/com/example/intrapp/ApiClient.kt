package com.example.intrapp

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*


class ApiClient {

    private val client: HttpClient = HttpClient()

    // Función para enviar solicitudes POST con body
    suspend fun post(url: String, body: String): HttpResponse? {
        return try {
            client.post(url) {
                header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
                setBody(body)
            }
        } catch (e: Exception) {
            println("[ApiClient] Error en la solicitud POST: ${e.message}")
            null
        }
    }

    // Función para enviar solicitudes GET con headers
    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): HttpResponse? {
        return try {
            client.get(url) {
                headers.forEach { (key, value) ->
                    this.headers.append(key, value)
                }
            }
        } catch (e: Exception) {
            println("[ApiClient] Error en la solicitud GET: ${e.message}")
            null
        }
    }

    // Cierra el cliente cuando ya no sea necesario (opcional)
    fun close() {
        client.close()
    }
}

