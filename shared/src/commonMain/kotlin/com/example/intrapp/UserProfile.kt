package com.example.intrapp

import kotlinx.serialization.Serializable
import kotlin.jvm.Transient


@Serializable
data class UserProfile(
    val id: Int,
    val login: String,
    val email: String,
    val first_name: String?,
    val last_name: String?,
    var image: Image?,
    val location: String?,
    val wallet: Int,

    var projects: List<Project> = emptyList(),
    val cursus_users: List<CursusUser> = emptyList(),

    var skills: List<UserSkill> = emptyList() //COMO EN LOADPROJECTS NO TIENE SKILLS EL JSON NO ME AVANZA. TRANSIENT
) {
    @Serializable
    data class Image(
        val link: String,
    )

    @Serializable
    data class CursusUser(
        val id: Int,
        val level: Double?,
        val cursus: Cursus,
    )

    @Serializable
    data class Cursus(
        val id: Int,
        val name: String,
        val kind: String
    )

    // Lógica para obtener el level del cursus "Cursus" (el de id: 21)
    val level: Double? = cursus_users
        .firstOrNull { it.cursus.id == 21 }
        ?.level


    @Serializable
    data class UserSkill(
        val id: Int,
        val name: String,
        val level: Float // Nivel de 0 a 100
    )
}
