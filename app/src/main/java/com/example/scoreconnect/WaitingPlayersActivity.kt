package com.example.scoreconnect

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scoreconnect.adapters.WaitingPlayerAdapter
import com.example.scoreconnect.model.WaitingPlayer

class WaitingPlayersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_players)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvWaitingPlayers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = WaitingPlayerAdapter(
            listOf(
                WaitingPlayer("Goalkeeper", "Ruben"),
                WaitingPlayer("Central", "Sonia"),
                WaitingPlayer("Central", "Carlos")
            ),
            onAccept = { player ->
                Toast.makeText(this, "Accepted ${player.name}", Toast.LENGTH_SHORT).show()
            },
            onReject = { player ->
                Toast.makeText(this, "Rejected ${player.name}", Toast.LENGTH_SHORT).show()
            }
        )

        findViewById<Button>(R.id.btnStopSearching).setOnClickListener {
            Toast.makeText(this, "Search stopped", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}