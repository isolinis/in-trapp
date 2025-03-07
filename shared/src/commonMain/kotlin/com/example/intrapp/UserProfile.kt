package com.example.intrapp

import kotlinx.serialization.Serializable


@Serializable
data class UserProfile(
    val id: Int,
    val login: String,
    val email: String,
    val first_name: String?,
    val last_name: String?,
    var image: Image?,
    //val level: Double?,
    val location: String?,
    val wallet: Int,
    var accessToken: String = "",
    var refreshToken: String = "",


    //al principio estara vacia hasta que ejecute el click de projects y lo rellene.
    //var evaluations
    //var projects



) {
    // Clase anidada para el objeto image
    @Serializable
    data class Image(
        val link: String, // URL de la imagen
    )
}
