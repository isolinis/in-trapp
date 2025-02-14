package com.example.intrapp

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse


class ApiClient {

    private val client: HttpClient = HttpClient()

    suspend fun post(url:String): HttpResponse? {

        return try {
            val response = client.post(url)
            response
        } catch (e: Exception) {
            null
        } finally {
            client.close()
        }
    }
    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): HttpResponse? {
        return try {
            val response = client.get(url) {
                headers.forEach { (key, value) ->
                    this.headers.append(key, value)
                }
            }
            response
        } catch (e: Exception) {
            null
        } finally {
            client.close()
        }
    }

}

