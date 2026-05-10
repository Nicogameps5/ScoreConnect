package com.example.scoreconnect

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.scoreconnect.model.CreatedMatch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateMatchStep2Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var spinnerSport: Spinner
    private lateinit var spinnerLevel: Spinner
    private lateinit var layoutPositionsContainer: LinearLayout
    private lateinit var tvMissingPeopleCount: TextView
    private lateinit var createButton: Button

    private val selectedPositions = mutableMapOf<String, Int>()

    private val availableSports = listOf("Football", "Basketball", "Padel", "Tennis", "Volleyball", "Running")
    private val levels = listOf("Beginner", "Intermediate", "Advanced")
    private val sportPositions = mapOf(
        "Football" to listOf("Goalkeeper", "Defender", "Midfielder", "Forward"),
        "Basketball" to listOf("Point Guard", "Shooting Guard", "Small Forward", "Power Forward", "Center"),
        "Padel" to listOf("Left Side", "Right Side"),
        "Tennis" to listOf("Singles Player", "Doubles Player"),
        "Volleyball" to listOf("Setter", "Outside Hitter", "Opposite", "Middle Blocker", "Libero"),
        "Running" to listOf("Runner")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_match_step2)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        spinnerSport = findViewById(R.id.spinnerSport)
        spinnerLevel = findViewById(R.id.spinnerLevel)
        layoutPositionsContainer = findViewById(R.id.layoutPositionsContainer)
        tvMissingPeopleCount = findViewById(R.id.tvMissingPeopleCount)
        createButton = findViewById(R.id.btnContinue)

        // Setup Adapters
        spinnerSport.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, availableSports).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerLevel.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, levels).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinnerSport.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                renderPositionSelectors(availableSports[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        createButton.setOnClickListener {
            val location = intent.getStringExtra("location").orEmpty()
            val dateTime = intent.getStringExtra("dateTime").orEmpty()

            // Retrieve coordinates from Intent
            val lat = intent.getDoubleExtra("latitude", 0.0)
            val lng = intent.getDoubleExtra("longitude", 0.0)

            val selectedSport = spinnerSport.selectedItem?.toString().orEmpty()
            val selectedLevel = spinnerLevel.selectedItem?.toString().orEmpty()
            val filteredPositions = selectedPositions.filterValues { it > 0 }
            val missingPeople = filteredPositions.values.sum()

            if (missingPeople <= 0) {
                Toast.makeText(this, "Add at least one missing player", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveMatch(location, lat, lng, dateTime, selectedSport, selectedLevel, filteredPositions)
        }
    }

    private fun renderPositionSelectors(sport: String) {
        layoutPositionsContainer.removeAllViews()
        selectedPositions.clear()
        val positions = sportPositions[sport] ?: listOf("Player")

        positions.forEach { positionName ->
            selectedPositions[positionName] = 0
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(14.dp(), 14.dp(), 14.dp(), 14.dp())
                val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.bottomMargin = 10.dp()
                layoutParams = params
            }

            val label = TextView(this).apply {
                text = positionName
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val countText = TextView(this).apply {
                text = "0"
                textSize = 18f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(50.dp(), ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            val btnMinus = Button(this).apply { text = "-"; layoutParams = LinearLayout.LayoutParams(45.dp(), 45.dp()) }
            val btnPlus = Button(this).apply { text = "+"; layoutParams = LinearLayout.LayoutParams(45.dp(), 45.dp()) }

            btnMinus.setOnClickListener {
                val current = selectedPositions[positionName] ?: 0
                if (current > 0) {
                    selectedPositions[positionName] = current - 1
                    countText.text = selectedPositions[positionName].toString()
                    updateMissingPeopleText()
                }
            }

            btnPlus.setOnClickListener {
                val current = selectedPositions[positionName] ?: 0
                selectedPositions[positionName] = current + 1
                countText.text = selectedPositions[positionName].toString()
                updateMissingPeopleText()
            }

            row.addView(label); row.addView(btnMinus); row.addView(countText); row.addView(btnPlus)
            layoutPositionsContainer.addView(row)
        }
        updateMissingPeopleText()
    }

    private fun updateMissingPeopleText() {
        tvMissingPeopleCount.text = "Missing people: ${selectedPositions.values.sum()}"
    }

    private fun saveMatch(
        location: String,
        lat: Double,
        lng: Double,
        dateTime: String,
        sport: String,
        level: String,
        positions: Map<String, Int>
    ) {
        val currentUser = auth.currentUser ?: return
        createButton.isEnabled = false

        val matchRef = db.collection("matches").document()
        val totalNeeded = positions.values.sum()

        val match = CreatedMatch(
            id = matchRef.id,
            ownerId = currentUser.uid,
            location = location,
            latitude = lat,       // Added for filtering
            longitude = lng,      // Added for filtering
            dateTime = dateTime,
            sport = sport,
            level = level,
            totalPlayers = totalNeeded + 1,
            currentPlayers = 1,
            createdAt = System.currentTimeMillis(),
            positions = positions.mapValues { it.value.toLong() }
        )

        matchRef.set(match)
            .addOnSuccessListener {
                Toast.makeText(this, "Match created successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HistoryActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                createButton.isEnabled = true
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()
}