package com.example.scoreconnect

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scoreconnect.adapters.FriendRequestAdapter
import com.example.scoreconnect.adapters.FriendUserAdapter
import com.example.scoreconnect.model.FriendRequest
import com.example.scoreconnect.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var usersAdapter: FriendUserAdapter
    private lateinit var requestsAdapter: FriendRequestAdapter
    private lateinit var friendsAdapter: FriendUserAdapter

    private lateinit var tvEmptyUsers: TextView
    private lateinit var tvEmptyRequests: TextView
    private lateinit var tvEmptyFriends: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        NavigationUtils.setupHomeButton(this)

        tvEmptyUsers = findViewById(R.id.tvEmptyUsers)
        tvEmptyRequests = findViewById(R.id.tvEmptyRequests)
        tvEmptyFriends = findViewById(R.id.tvEmptyFriends)

        val rvUsers = findViewById<RecyclerView>(R.id.rvUsers)
        val rvRequests = findViewById<RecyclerView>(R.id.rvFriendRequests)
        val rvFriends = findViewById<RecyclerView>(R.id.rvFriends)

        rvUsers.layoutManager = LinearLayoutManager(this)
        rvRequests.layoutManager = LinearLayoutManager(this)
        rvFriends.layoutManager = LinearLayoutManager(this)

        rvUsers.isNestedScrollingEnabled = false
        rvRequests.isNestedScrollingEnabled = false
        rvFriends.isNestedScrollingEnabled = false

        usersAdapter = FriendUserAdapter(
            mutableListOf(),
            primaryButtonText = "Add friend",
            onPrimaryClick = { user ->
                sendFriendRequest(user)
            },
            onViewProfile = { user ->
                openUserProfile(user.uid)
            }
        )

        requestsAdapter = FriendRequestAdapter(
            mutableListOf(),
            onAccept = { request ->
                updateRequestStatus(request, "accepted")
            },
            onReject = { request ->
                updateRequestStatus(request, "rejected")
            },
            onViewProfile = { request ->
                openUserProfile(request.senderId)
            }
        )

        friendsAdapter = FriendUserAdapter(
            mutableListOf(),
            primaryButtonText = null,
            onPrimaryClick = null,
            onViewProfile = { user ->
                openUserProfile(user.uid)
            }
        )

        rvUsers.adapter = usersAdapter
        rvRequests.adapter = requestsAdapter
        rvFriends.adapter = friendsAdapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUsers()
        loadIncomingRequests()
        loadFriends()
    }

    private fun loadUsers() {
        val currentUid = auth.currentUser?.uid ?: return

        val hiddenUserIds = mutableSetOf<String>()

        db.collection("friendRequests")
            .whereEqualTo("senderId", currentUid)
            .get()
            .addOnSuccessListener { sentResult ->

                sentResult.documents.forEach { document ->
                    val status = document.getString("status") ?: ""
                    val receiverId = document.getString("receiverId") ?: ""

                    if (status == "pending" || status == "accepted") {
                        hiddenUserIds.add(receiverId)
                    }
                }

                db.collection("friendRequests")
                    .whereEqualTo("receiverId", currentUid)
                    .get()
                    .addOnSuccessListener { receivedResult ->

                        receivedResult.documents.forEach { document ->
                            val status = document.getString("status") ?: ""
                            val senderId = document.getString("senderId") ?: ""

                            if (status == "pending" || status == "accepted") {
                                hiddenUserIds.add(senderId)
                            }
                        }

                        db.collection("users")
                            .get()
                            .addOnSuccessListener { usersResult ->
                                val users = usersResult.documents.mapNotNull { document ->
                                    document.toObject(User::class.java)?.copy(uid = document.id)
                                }.filter { user ->
                                    user.uid != currentUid && !hiddenUserIds.contains(user.uid)
                                }.sortedBy { user ->
                                    user.username.lowercase()
                                }

                                usersAdapter.updateItems(users)
                                tvEmptyUsers.visibility =
                                    if (users.isEmpty()) View.VISIBLE else View.GONE
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error loading users: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error loading friend requests: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading friend requests: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun loadIncomingRequests() {
        val currentUid = auth.currentUser?.uid ?: return

        db.collection("friendRequests")
            .whereEqualTo("receiverId", currentUid)
            .get()
            .addOnSuccessListener { result ->
                val requests = result.documents.mapNotNull { document ->
                    document.toObject(FriendRequest::class.java)?.copy(id = document.id)
                }.filter { request ->
                    request.status == "pending"
                }.sortedByDescending { request ->
                    request.createdAt
                }

                requestsAdapter.updateItems(requests)
                tvEmptyRequests.visibility = if (requests.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading friend requests: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun loadFriends() {
        val currentUid = auth.currentUser?.uid ?: return

        val acceptedRequests = mutableListOf<FriendRequest>()

        db.collection("friendRequests")
            .whereEqualTo("senderId", currentUid)
            .get()
            .addOnSuccessListener { sentResult ->
                acceptedRequests.addAll(
                    sentResult.documents.mapNotNull { document ->
                        document.toObject(FriendRequest::class.java)?.copy(id = document.id)
                    }.filter { request ->
                        request.status == "accepted"
                    }
                )

                db.collection("friendRequests")
                    .whereEqualTo("receiverId", currentUid)
                    .get()
                    .addOnSuccessListener { receivedResult ->
                        acceptedRequests.addAll(
                            receivedResult.documents.mapNotNull { document ->
                                document.toObject(FriendRequest::class.java)?.copy(id = document.id)
                            }.filter { request ->
                                request.status == "accepted"
                            }
                        )

                        loadFriendUsersFromRequests(acceptedRequests)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error loading friends: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading friends: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun loadFriendUsersFromRequests(requests: List<FriendRequest>) {
        val currentUid = auth.currentUser?.uid ?: return

        if (requests.isEmpty()) {
            friendsAdapter.updateItems(emptyList())
            tvEmptyFriends.visibility = View.VISIBLE
            return
        }

        val friends = mutableListOf<User>()
        var remaining = requests.size

        requests.forEach { request ->
            val friendUid = if (request.senderId == currentUid) {
                request.receiverId
            } else {
                request.senderId
            }

            db.collection("users")
                .document(friendUid)
                .get()
                .addOnSuccessListener { userDoc ->
                    userDoc.toObject(User::class.java)?.let { user ->
                        friends.add(user.copy(uid = userDoc.id))
                    }

                    remaining--
                    if (remaining == 0) {
                        val sortedFriends = friends.sortedBy { it.username.lowercase() }
                        friendsAdapter.updateItems(sortedFriends)
                        tvEmptyFriends.visibility =
                            if (sortedFriends.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                .addOnFailureListener {
                    remaining--
                    if (remaining == 0) {
                        val sortedFriends = friends.sortedBy { it.username.lowercase() }
                        friendsAdapter.updateItems(sortedFriends)
                        tvEmptyFriends.visibility =
                            if (sortedFriends.isEmpty()) View.VISIBLE else View.GONE
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
                    return@addOnSuccessListener
                }

                if (existingRequest.exists() && existingStatus == "accepted") {
                    Toast.makeText(this, "You are already friends", Toast.LENGTH_SHORT).show()
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
                                loadUsers()
                                loadIncomingRequests()
                                loadFriends()
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
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error checking request: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun updateRequestStatus(request: FriendRequest, newStatus: String) {
        db.collection("friendRequests")
            .document(request.id)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    if (newStatus == "accepted") "Friend request accepted" else "Friend request rejected",
                    Toast.LENGTH_SHORT
                ).show()

                loadUsers()
                loadIncomingRequests()
                loadFriends()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error updating request: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun openUserProfile(userId: String) {
        val intent = Intent(this, UserProfileActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }
}