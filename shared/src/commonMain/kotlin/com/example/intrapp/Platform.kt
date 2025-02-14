package com.example.intrapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform