package com.example.scoreconnect.model

data class JoinedMatch(
    val id: String = "",
    val sport: String = "",
    val level: String = "",
    val dateTime: String = "",
    val location: String = "",
    val currentPlayers: Int = 0,
    val totalPlayers: Int = 0,
    val requestedPosition: String = "",
    val requestStatus: String = ""
)