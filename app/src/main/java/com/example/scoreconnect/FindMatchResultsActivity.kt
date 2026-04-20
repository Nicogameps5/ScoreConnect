package com.example.scoreconnect

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scoreconnect.adapters.MatchResultAdapter
import com.example.scoreconnect.model.MatchResult
import com.example.scoreconnect.model.WaitingPlayer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FindMatchResultsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: MatchResultAdapter
    private lateinit var tvResultsSubtitle: TextView
    private lateinit var tvEmptyMatches: TextView

    private var filterSport: String = "Any"
    private var filterLevel: String = "Any"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_match_results)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        NavigationUtils.setupHomeButton(this)

        filterSport = intent.getStringExtra("filterSport") ?: "Any"
        filterLevel = intent.getStringExtra("filterLevel") ?: "Any"

        tvResultsSubtitle = findViewById(R.id.tvResultsSubtitle)
        tvEmptyMatches = findViewById(R.id.tvEmptyMatches)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvMatches)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = MatchResultAdapter(mutableListOf()) { result ->
            openJoinDialog(result)
        }
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btnStopSearching).setOnClickListener {
            finish()
        }

        updateSubtitle()
        loadMatches()
    }

    private fun updateSubtitle() {
        tvResultsSubtitle.text = "Filters -> Sport: $filterSport | Level: $filterLevel"
    }

    private fun loadMatches() {
        val currentUid = auth.currentUser?.uid.orEmpty()

        db.collection("matches")
            .get()
            .addOnSuccessListener { result ->
                val matches = result.documents.mapNotNull { document ->
                    val sport = document.getString("sport").orEmpty()
                    val level = document.getString("level").orEmpty()
                    val location = document.getString("location").orEmpty()
                    val date = document.getString("dateTime")
                        ?: document.getString("date")
                        ?: ""
                    val creatorId = document.getString("creatorId")
                        ?: document.getString("ownerId")
                        ?: ""
                    val totalPlayers = (
                            document.getLong("totalPlayers")
                                ?: ((document.getLong("missingPlayers") ?: 0L) + 1L)
                            ).toInt()
                    val currentPlayers = (document.getLong("currentPlayers") ?: 1L).toInt()

                    val rawPositions = document.get("positions") as? Map<*, *>
                    val availablePositions = rawPositions
                        ?.filterValues { value -> ((value as? Number)?.toInt() ?: 0) > 0 }
                        ?.keys
                        ?.map { it.toString() }
                        ?: listOf("Player")

                    val positionsText = if (rawPositions.isNullOrEmpty()) {
                        "Player"
                    } else {
                        rawPositions.entries
                            .filter { ((it.value as? Number)?.toInt() ?: 0) > 0 }
                            .joinToString(", ") { "${it.key} x${(it.value as? Number)?.toInt() ?: 0}" }
                    }

                    val matchesSportFilter = filterSport == "Any" || sport == filterSport
                    val matchesLevelFilter = filterLevel == "Any" || level == filterLevel
                    val isOwnMatch = creatorId == currentUid
                    val hasFreeSpace = currentPlayers < totalPlayers

                    if (!matchesSportFilter || !matchesLevelFilter || isOwnMatch || !hasFreeSpace) {
                        null
                    } else {
                        MatchResult(
                            id = document.id,
                            creatorId = creatorId,
                            sport = sport,
                            level = level,
                            date = date,
                            positionNeeded = positionsText,
                            location = location,
                            occupation = "$currentPlayers/$totalPlayers players",
                            availablePositions = availablePositions
                        )
                    }
                }

                adapter.updateItems(matches)
                tvEmptyMatches.visibility = if (matches.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading matches: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun openJoinDialog(result: MatchResult) {
        val positions = result.availablePositions.ifEmpty { listOf("Player") }

        if (positions.size == 1) {
            submitJoinRequest(result, positions.first())
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Choose position")
            .setItems(positions.toTypedArray()) { _, which ->
                submitJoinRequest(result, positions[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun submitJoinRequest(result: MatchResult, selectedPosition: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "You need to log in again", Toast.LENGTH_SHORT).show()
            return
        }

        if (result.creatorId == currentUser.uid) {
            Toast.makeText(this, "You cannot join your own match", Toast.LENGTH_SHORT).show()
            return
        }

        val matchRef = db.collection("matches").document(result.id)

        matchRef.collection("requests")
            .whereEqualTo("requesterId", currentUser.uid)
            .get()
            .addOnSuccessListener { existingRequests ->
                val alreadyRequested = existingRequests.documents.any { document ->
                    val status = document.getString("status") ?: "pending"
                    status == "pending" || status == "accepted"
                }

                if (alreadyRequested) {
                    Toast.makeText(this, "You already requested to join this match", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                db.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .addOnSuccessListener { userDoc ->
                        val username = userDoc.getString("username")
                            ?.takeIf { it.isNotBlank() }
                            ?: currentUser.email.orEmpty()

                        val requestRef = matchRef.collection("requests").document()

                        val waitingPlayer = WaitingPlayer(
                            id = requestRef.id,
                            requesterId = currentUser.uid,
                            position = selectedPosition,
                            name = username,
                            status = "pending"
                        )

                        val batch = db.batch()
                        batch.set(requestRef, waitingPlayer)
                        batch.update(matchRef, "pendingRequests", FieldValue.increment(1))

                        batch.commit()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Join request sent for $selectedPosition",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error sending request: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .addOnFailureListener {
                        val requestRef = matchRef.collection("requests").document()

                        val waitingPlayer = WaitingPlayer(
                            id = requestRef.id,
                            requesterId = currentUser.uid,
                            position = selectedPosition,
                            name = currentUser.email.orEmpty(),
                            status = "pending"
                        )

                        val batch = db.batch()
                        batch.set(requestRef, waitingPlayer)
                        batch.update(matchRef, "pendingRequests", FieldValue.increment(1))

                        batch.commit()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Join request sent for $selectedPosition",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                    "Could not verify previous requests: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}