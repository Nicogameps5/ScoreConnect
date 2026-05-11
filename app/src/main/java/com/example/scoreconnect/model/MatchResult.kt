package com.example.scoreconnect.model

data class MatchResult(
    val id: String = "",
    val creatorId: String = "",
    val creatorUsername: String = "",
    val sport: String = "",
    val level: String = "",
    val date: String = "",
    val positionNeeded: String = "",
    val location: String = "",
    val occupation: String = "",
    val availablePositions: List<String> = emptyList()
)
