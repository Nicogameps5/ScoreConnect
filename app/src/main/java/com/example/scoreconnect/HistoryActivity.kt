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
import com.example.scoreconnect.adapters.CreatedMatchAdapter
import com.example.scoreconnect.adapters.JoinedMatchAdapter
import com.example.scoreconnect.model.CreatedMatch
import com.example.scoreconnect.model.JoinedMatch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var createdAdapter: CreatedMatchAdapter
    private lateinit var joinedAdapter: JoinedMatchAdapter

    private lateinit var tvEmptyCreatedHistory: TextView
    private lateinit var tvEmptyJoinedHistory: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        db = FirebaseFirestore.getInstance()

        NavigationUtils.setupHomeButton(this)

        tvEmptyCreatedHistory = findViewById(R.id.tvEmptyCreatedHistory)
        tvEmptyJoinedHistory = findViewById(R.id.tvEmptyJoinedHistory)

        val rvCreatedMatches = findViewById<RecyclerView>(R.id.rvCreatedMatches)
        val rvJoinedMatches = findViewById<RecyclerView>(R.id.rvJoinedMatches)

        rvCreatedMatches.layoutManager = LinearLayoutManager(this)
        rvJoinedMatches.layoutManager = LinearLayoutManager(this)

        rvCreatedMatches.isNestedScrollingEnabled = false
        rvJoinedMatches.isNestedScrollingEnabled = false

        createdAdapter = CreatedMatchAdapter(mutableListOf()) { match ->
            val intent = Intent(this, WaitingPlayersActivity::class.java)
            intent.putExtra("matchId", match.id)
            startActivity(intent)
        }

        joinedAdapter = JoinedMatchAdapter(mutableListOf())

        rvCreatedMatches.adapter = createdAdapter
        rvJoinedMatches.adapter = joinedAdapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadCreatedMatches()
        loadJoinedMatches()
    }

    private fun loadCreatedMatches() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        db.collection("matches")
            .whereEqualTo("ownerId", currentUser.uid)
            .get()
            .addOnSuccessListener { result ->
                val matches = result.documents
                    .mapNotNull { document ->
                        document.toObject(CreatedMatch::class.java)?.copy(id = document.id)
                    }
                    .sortedByDescending { it.createdAt }

                createdAdapter.updateItems(matches)
                tvEmptyCreatedHistory.visibility =
                    if (matches.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading created matches: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun loadJoinedMatches() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        db.collection("matches")
            .get()
            .addOnSuccessListener { result ->
                val matchDocs = result.documents

                if (matchDocs.isEmpty()) {
                    joinedAdapter.updateItems(emptyList())
                    tvEmptyJoinedHistory.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                val joinedMatches = mutableListOf<JoinedMatch>()
                var remainingMatches = matchDocs.size

                matchDocs.forEach { matchDoc ->
                    matchDoc.reference
                        .collection("requests")
                        .whereEqualTo("requesterId", currentUser.uid)
                        .get()
                        .addOnSuccessListener { requestsResult ->

                            requestsResult.documents.forEach { requestDoc ->
                                val status = requestDoc.getString("status") ?: "pending"

                                if (status == "pending" || status == "accepted") {
                                    val dateTime = matchDoc.getString("dateTime")
                                        ?: matchDoc.getString("date")
                                        ?: ""

                                    val totalPlayers = (
                                            matchDoc.getLong("totalPlayers")
                                                ?: ((matchDoc.getLong("missingPlayers") ?: 0L) + 1L)
                                            ).toInt()

                                    val currentPlayers =
                                        (matchDoc.getLong("currentPlayers") ?: 1L).toInt()

                                    val joinedMatch = JoinedMatch(
                                        id = matchDoc.id,
                                        sport = matchDoc.getString("sport").orEmpty(),
                                        level = matchDoc.getString("level").orEmpty(),
                                        dateTime = dateTime,
                                        location = matchDoc.getString("location").orEmpty(),
                                        currentPlayers = currentPlayers,
                                        totalPlayers = totalPlayers,
                                        requestedPosition = requestDoc.getString("position").orEmpty(),
                                        requestStatus = status
                                    )

                                    joinedMatches.add(joinedMatch)
                                }
                            }

                            remainingMatches--
                            if (remainingMatches == 0) {
                                finishJoinedMatchesLoad(joinedMatches)
                            }
                        }
                        .addOnFailureListener {
                            remainingMatches--
                            if (remainingMatches == 0) {
                                finishJoinedMatchesLoad(joinedMatches)
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading joined matches: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
    private fun finishJoinedMatchesLoad(joinedMatches: List<JoinedMatch>) {
        val sortedMatches = joinedMatches.sortedByDescending { it.dateTime }

        joinedAdapter.updateItems(sortedMatches)
        tvEmptyJoinedHistory.visibility =
            if (sortedMatches.isEmpty()) View.VISIBLE else View.GONE
    }
}