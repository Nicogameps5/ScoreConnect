package com.example.scoreconnect.model

data class WaitingPlayer(
    val id: String = "",
    val requesterId: String = "",
    val position: String = "",
    val name: String = "",
    val status: String = "pending"
)