package com.example.scoreconnect

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scoreconnect.adapters.MatchResultAdapter
import com.example.scoreconnect.model.MatchResult

class FindMatchResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_match_results)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvMatches)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MatchResultAdapter(
            listOf(
                MatchResult(
                    sport = "Football",
                    level = "Intermediate",
                    price = "€8",
                    date = "18 Dec - 19:30",
                    positionNeeded = "Goalkeeper",
                    location = "Las Palmas Center"
                ),
                MatchResult(
                    sport = "Basketball",
                    level = "Advanced",
                    price = "€6",
                    date = "19 Dec - 18:00",
                    positionNeeded = "Point Guard",
                    location = "Ciudad Alta Court"
                )
            )
        ) { result ->
            Toast.makeText(this, "Joined ${result.sport} match", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnStopSearching).setOnClickListener {
            Toast.makeText(this, "Stopped looking for matches", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
