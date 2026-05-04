package com.example.scoreconnect

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.scoreconnect.model.FriendRequest
import com.example.scoreconnect.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvSports: TextView
    private lateinit var btnSendFriendRequest: Button

    private var profileUser: User? = null
    private var profileUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        NavigationUtils.setupHomeButton(this)

        tvUsername = findViewById(R.id.tvUserProfileUsername)
        tvEmail = findViewById(R.id.tvUserProfileEmail)
        tvDescription = findViewById(R.id.tvUserProfileDescription)
        tvSports = findViewById(R.id.tvUserProfileSports)
        btnSendFriendRequest = findViewById(R.id.btnSendFriendRequestFromProfile)

        profileUserId = intent.getStringExtra("userId").orEmpty()

        if (profileUserId.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnSendFriendRequest.setOnClickListener {
            profileUser?.let { user ->
                sendFriendRequest(user)
            }
        }

        loadProfile()
    }

    private fun loadProfile() {
        val currentUid = auth.currentUser?.uid.orEmpty()

        db.collection("users")
            .document(profileUserId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)?.copy(uid = document.id)

                if (user == null) {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                profileUser = user

                tvUsername.text = if (user.username.isBlank()) "Unknown user" else user.username
                tvEmail.text = "Email: ${user.email}"
                tvDescription.text =
                    if (user.description.isBlank()) "No description" else user.description

                tvSports.text = if (user.sports.isEmpty()) {
                    "Sports: No sports selected"
                } else {
                    "Sports: ${user.sports.joinToString(", ")}"
                }

                if (user.uid == currentUid) {
                    btnSendFriendRequest.visibility = View.GONE
                } else {
                    btnSendFriendRequest.visibility = View.VISIBLE
                    checkFriendStatus(user.uid)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun checkFriendStatus(targetUid: String) {
        val currentUid = auth.currentUser?.uid ?: return
        val requestId = listOf(currentUid, targetUid).sorted().joinToString("_")

        db.collection("friendRequests")
            .document(requestId)
            .get()
            .addOnSuccessListener { document ->
                val status = document.getString("status")

                when (status) {
                    "pending" -> {
                        btnSendFriendRequest.text = "Request pending"
                        btnSendFriendRequest.isEnabled = false
                    }
                    "accepted" -> {
                        btnSendFriendRequest.text = "Already friends"
                        btnSendFriendRequest.isEnabled = false
                    }
                    else -> {
                        btnSendFriendRequest.text = "Send friend request"
                        btnSendFriendRequest.isEnabled = true
                    }
                }
            }
    }

    private fun sendFriendRequest(targetUser: User) {
        val currentUser = auth.currentUser ?: return
        val currentUid = currentUser.uid

        if (targetUser.uid == currentUid) {
            Toast.makeText(this, "You cannot add yourself", Toast.LENGTH_SHORT).show()
            return
        }

        val requestId = listOf(currentUid, targetUser.uid).sorted().joinToString("_")
        val requestRef = db.collection("friendRequests").document(requestId)

        requestRef.get()
            .addOnSuccessListener { existingRequest ->
                val existingStatus = existingRequest.getString("status")

                if (existingRequest.exists() && existingStatus == "pending") {
                    Toast.makeText(this, "Friend request already pending", Toast.LENGTH_SHORT).show()
                    checkFriendStatus(targetUser.uid)
                    return@addOnSuccessListener
                }

                if (existingRequest.exists() && existingStatus == "accepted") {
                    Toast.makeText(this, "You are already friends", Toast.LENGTH_SHORT).show()
                    checkFriendStatus(targetUser.uid)
                    return@addOnSuccessListener
                }

                db.collection("users")
                    .document(currentUid)
                    .get()
                    .addOnSuccessListener { currentUserDoc ->
                        val senderUsername = currentUserDoc.getString("username")
                            ?.takeIf { it.isNotBlank() }
                            ?: currentUser.email.orEmpty()

                        val request = FriendRequest(
                            id = requestId,
                            senderId = currentUid,
                            senderUsername = senderUsername,
                            receiverId = targetUser.uid,
                            receiverUsername = targetUser.username,
                            status = "pending",
                            createdAt = System.currentTimeMillis()
                        )

                        requestRef.set(request)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Friend request sent", Toast.LENGTH_SHORT).show()
                                checkFriendStatus(targetUser.uid)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error sending request: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
            }
    }
}