package com.example.scoreconnect.model

data class CreatedMatch(
    val id: String = "",
    val ownerId: String = "",
    val location: String = "",
    val dateTime: String = "",
    val sport: String = "",
    val level: String = "",
    val totalPlayers: Int = 0,
    val currentPlayers: Int = 1,
    val pendingRequests: Int = 0,
    val createdAt: Long = 0L,
    val positions: Map<String, Long> = emptyMap()
)
