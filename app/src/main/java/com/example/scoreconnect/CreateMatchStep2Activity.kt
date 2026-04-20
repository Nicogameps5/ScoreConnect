package com.example.scoreconnect

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.scoreconnect.model.CreatedMatch
import com.example.scoreconnect.model.WaitingPlayer
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

    private val availableSports = listOf(
        "Football",
        "Basketball",
        "Padel",
        "Tennis",
        "Volleyball",
        "Running"
    )

    private val levels = listOf(
        "Beginner",
        "Intermediate",
        "Advanced"
    )

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

        NavigationUtils.setupHomeButton(this)

        spinnerSport = findViewById(R.id.spinnerSport)
        spinnerLevel = findViewById(R.id.spinnerLevel)
        layoutPositionsContainer = findViewById(R.id.layoutPositionsContainer)
        tvMissingPeopleCount = findViewById(R.id.tvMissingPeopleCount)
        createButton = findViewById(R.id.btnContinue)

        val sportAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            availableSports
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

        spinnerSport.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedSport = availableSports[position]
                renderPositionSelectors(selectedSport)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        createButton.setOnClickListener {
            val location = intent.getStringExtra("location").orEmpty()
            val dateTime = intent.getStringExtra("dateTime").orEmpty()
            val selectedSport = spinnerSport.selectedItem?.toString().orEmpty()
            val selectedLevel = spinnerLevel.selectedItem?.toString().orEmpty()

            val filteredPositions = selectedPositions.filterValues { it > 0 }
            val missingPeople = filteredPositions.values.sum()

            if (location.isEmpty() || dateTime.isEmpty()) {
                Toast.makeText(this, "Location or date missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedSport.isEmpty() || selectedLevel.isEmpty()) {
                Toast.makeText(this, "Select sport and level", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (missingPeople <= 0) {
                Toast.makeText(this, "Select at least one needed position", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveMatch(
                location = location,
                dateTime = dateTime,
                sport = selectedSport,
                level = selectedLevel,
                positions = filteredPositions,
                createButton = createButton
            )
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
                setBackgroundResource(R.drawable.bg_card)
                setPadding(14.dp(), 14.dp(), 14.dp(), 14.dp())

                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.bottomMargin = 10.dp()
                layoutParams = params
            }

            val label = TextView(this).apply {
                text = positionName
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val minusButton = Button(this).apply {
                text = "-"
                setBackgroundResource(R.drawable.bg_button_secondary)
                layoutParams = LinearLayout.LayoutParams(46.dp(), 46.dp())
            }

            val countText = TextView(this).apply {
                text = "0"
                textSize = 20f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(52.dp(), ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            val plusButton = Button(this).apply {
                text = "+"
                setBackgroundResource(R.drawable.bg_button_secondary)
                layoutParams = LinearLayout.LayoutParams(46.dp(), 46.dp())
            }

            minusButton.setOnClickListener {
                val current = selectedPositions[positionName] ?: 0
                if (current > 0) {
                    val newValue = current - 1
                    selectedPositions[positionName] = newValue
                    countText.text = newValue.toString()
                    updateMissingPeopleText()
                }
            }

            plusButton.setOnClickListener {
                val current = selectedPositions[positionName] ?: 0
                val newValue = current + 1
                selectedPositions[positionName] = newValue
                countText.text = newValue.toString()
                updateMissingPeopleText()
            }

            row.addView(label)
            row.addView(minusButton)
            row.addView(countText)
            row.addView(plusButton)

            layoutPositionsContainer.addView(row)
        }

        updateMissingPeopleText()
    }

    private fun updateMissingPeopleText() {
        val total = selectedPositions.values.sum()
        tvMissingPeopleCount.text = "Missing people: $total"
    }

    private fun saveMatch(
        location: String,
        dateTime: String,
        sport: String,
        level: String,
        positions: Map<String, Int>,
        createButton: Button
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
            return
        }

        createButton.isEnabled = false

        val missingPeople = positions.values.sum()
        val matchRef = db.collection("matches").document()
        val pendingRequests = buildMockRequests(positions)

        val match = CreatedMatch(
            id = matchRef.id,
            ownerId = currentUser.uid,
            location = location,
            dateTime = dateTime,
            sport = sport,
            level = level,
            totalPlayers = missingPeople + 1,
            currentPlayers = 1,
            pendingRequests = pendingRequests.size,
            createdAt = System.currentTimeMillis(),
            positions = positions.mapValues { it.value.toLong() }
        )

        val batch = db.batch()
        batch.set(matchRef, match)

        pendingRequests.forEach { request ->
            val requestRef = matchRef.collection("requests").document()
            batch.set(
                requestRef,
                request.copy(id = requestRef.id)
            )
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Match created successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HistoryActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                createButton.isEnabled = true
                Toast.makeText(
                    this,
                    "Error creating match: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun buildMockRequests(positionCounts: Map<String, Int>): List<WaitingPlayer> {
        val names = listOf("Ruben", "Sonia", "Carlos", "Lucia", "Adrian", "Marta", "Javier", "Elena")
        val requests = mutableListOf<WaitingPlayer>()
        var index = 0

        positionCounts.forEach { (position, count) ->
            repeat(count) {
                requests.add(
                    WaitingPlayer(
                        name = names[index % names.size],
                        position = position,
                        status = "pending"
                    )
                )
                index++
            }
        }

        return requests
    }

    private fun Int.dp(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}