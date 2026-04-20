package com.example.scoreconnect

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scoreconnect.adapters.WaitingPlayerAdapter
import com.example.scoreconnect.model.CreatedMatch
import com.example.scoreconnect.model.WaitingPlayer
import com.google.firebase.firestore.FirebaseFirestore

class WaitingPlayersActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: WaitingPlayerAdapter

    private lateinit var tvManagedMatchTitle: TextView
    private lateinit var tvOccupation: TextView
    private lateinit var tvEmptyRequests: TextView

    private var matchId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_players)
        NavigationUtils.setupHomeButton(this)

        db = FirebaseFirestore.getInstance()
        matchId = intent.getStringExtra("matchId").orEmpty()

        if (matchId.isEmpty()) {
            Toast.makeText(this, "Match not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvManagedMatchTitle = findViewById(R.id.tvManagedMatchTitle)
        tvOccupation = findViewById(R.id.tvOccupation)
        tvEmptyRequests = findViewById(R.id.tvEmptyRequests)

        val recyclerView = findViewById<RecyclerView>(R.id.rvWaitingPlayers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = WaitingPlayerAdapter(
            mutableListOf(),
            onAccept = { player ->
                updateRequestStatus(player, accepted = true)
            },
            onReject = { player ->
                updateRequestStatus(player, accepted = false)
            }
        )

        recyclerView.adapter = adapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadMatch()
        loadPendingRequests()
    }

    private fun loadMatch() {
        db.collection("matches")
            .document(matchId)
            .get()
            .addOnSuccessListener { document ->
                val match = document.toObject(CreatedMatch::class.java)

                if (match != null) {
                    tvManagedMatchTitle.text = "${match.sport} - ${match.dateTime}"
                    tvOccupation.text =
                        "Occupation: ${match.currentPlayers}/${match.totalPlayers} players · Pending: ${match.pendingRequests}"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading match: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun loadPendingRequests() {
        db.collection("matches")
            .document(matchId)
            .collection("requests")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { result ->
                val pendingPlayers = result.documents.mapNotNull { document ->
                    document.toObject(WaitingPlayer::class.java)?.copy(id = document.id)
                }

                adapter.updateItems(pendingPlayers)
                tvEmptyRequests.visibility =
                    if (pendingPlayers.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading requests: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun updateRequestStatus(player: WaitingPlayer, accepted: Boolean) {
        val matchRef = db.collection("matches").document(matchId)
        val requestRef = matchRef.collection("requests").document(player.id)

        db.runTransaction { transaction ->
            val matchSnapshot = transaction.get(matchRef)
            val requestSnapshot = transaction.get(requestRef)

            val currentStatus = requestSnapshot.getString("status") ?: "pending"
            if (currentStatus != "pending") {
                throw IllegalStateException("This request has already been processed")
            }

            val pendingRequests =
                (matchSnapshot.getLong("pendingRequests") ?: 0L).toInt()

            if (accepted) {
                val currentPlayers =
                    (matchSnapshot.getLong("currentPlayers") ?: 1L).toInt()
                val totalPlayers =
                    (matchSnapshot.getLong("totalPlayers") ?: 1L).toInt()

                if (currentPlayers >= totalPlayers) {
                    throw IllegalStateException("The match is already full")
                }

                transaction.update(matchRef, "currentPlayers", currentPlayers + 1)
                transaction.update(matchRef, "pendingRequests", maxOf(0, pendingRequests - 1))
                transaction.update(requestRef, "status", "accepted")
            } else {
                transaction.update(matchRef, "pendingRequests", maxOf(0, pendingRequests - 1))
                transaction.update(requestRef, "status", "rejected")
            }

            null
        }.addOnSuccessListener {
            Toast.makeText(
                this,
                if (accepted) "Player accepted" else "Player rejected",
                Toast.LENGTH_SHORT
            ).show()

            loadMatch()
            loadPendingRequests()
        }.addOnFailureListener { e ->
            Toast.makeText(
                this,
                e.message ?: "Error updating request",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}