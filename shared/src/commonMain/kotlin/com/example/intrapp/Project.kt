package com.example.intrapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: Int,
    val skills: List<String>?,
    @SerialName("final_mark") val finalMark: Int?,
    val status: String,
    @SerialName("updated_at") val updatedAt: String,
    val project: ProjectInfo  // <--- Subobjeto llamado "project"
)

@Serializable
data class ProjectInfo(
    val id: Int,
    val name: String
)