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
import com.example.scoreconnect.model.CreatedMatch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: CreatedMatchAdapter
    private lateinit var tvEmptyHistory: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        NavigationUtils.setupHomeButton(this)

        db = FirebaseFirestore.getInstance()
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory)

        val recyclerView = findViewById<RecyclerView>(R.id.rvCreatedMatches)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = CreatedMatchAdapter(mutableListOf()) { match ->
            val intent = Intent(this, WaitingPlayersActivity::class.java)
            intent.putExtra("matchId", match.id)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadCreatedMatches()
    }

    private fun loadCreatedMatches() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            return
        }

        db.collection("matches")
            .whereEqualTo("ownerId", currentUser.uid)
            .get()
            .addOnSuccessListener { result ->
                val matches = result.documents
                    .mapNotNull { document ->
                        document.toObject(CreatedMatch::class.java)?.copy(id = document.id)
                    }
                    .sortedByDescending { it.createdAt }

                adapter.updateItems(matches)
                tvEmptyHistory.visibility =
                    if (matches.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading matches: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}