package com.example.scoreconnect.model

data class FriendRequest(
    val id: String = "",
    val senderId: String = "",
    val senderUsername: String = "",
    val receiverId: String = "",
    val receiverUsername: String = "",
    val status: String = "pending",
    val createdAt: Long = 0L
)