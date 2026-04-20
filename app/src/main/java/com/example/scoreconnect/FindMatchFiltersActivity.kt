package com.example.scoreconnect

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class FindMatchFiltersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_match_filters)

        NavigationUtils.setupHomeButton(this)

        val spinnerSport = findViewById<Spinner>(R.id.spinnerFilterSport)
        val spinnerLevel = findViewById<Spinner>(R.id.spinnerFilterLevel)

        val sports = listOf(
            "Any",
            "Football",
            "Basketball",
            "Padel",
            "Tennis",
            "Volleyball",
            "Running"
        )

        val levels = listOf(
            "Any",
            "Beginner",
            "Intermediate",
            "Advanced"
        )

        val sportAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            sports
        )
        sportAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSport.adapter = sportAdapter

        val levelAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            levels
        )
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLevel.adapter = levelAdapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnStartSearching).setOnClickListener {
            val intent = Intent(this, FindMatchResultsActivity::class.java)
            intent.putExtra("filterSport", spinnerSport.selectedItem.toString())
            intent.putExtra("filterLevel", spinnerLevel.selectedItem.toString())
            startActivity(intent)
        }
    }
}