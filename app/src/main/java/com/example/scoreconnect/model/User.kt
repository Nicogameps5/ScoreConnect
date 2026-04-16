package com.example.scoreconnect.model

data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val description: String = "",
    val sports: List<String> = emptyList()
)