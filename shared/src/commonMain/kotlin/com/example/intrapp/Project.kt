package com.example.intrapp

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: Int,
    val name: String,
    val finalMark: Int?,
    val status: String
)