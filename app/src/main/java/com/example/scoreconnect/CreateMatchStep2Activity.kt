package com.example.scoreconnect

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateMatchStep2Activity : AppCompatActivity() {

    private var missingPeople = 2
    private var selectedSport = ""
    private var selectedLevel = ""

    private val positionCounts = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_match_step2)

        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val location = intent.getStringExtra("location") ?: ""
        val date = intent.getStringExtra("date") ?: ""

        val container = findViewById<LinearLayout>(R.id.positionsContainer)
        val counter = findViewById<TextView>(R.id.tvMissingPeopleCount)

        fun refreshCounter() {
            counter.text = missingPeople.toString()
        }

        refreshCounter()

        findViewById<Button>(R.id.btnIncrease).setOnClickListener {
            missingPeople++
            refreshCounter()
        }

        findViewById<Button>(R.id.btnDecrease).setOnClickListener {
            if (missingPeople > 1) {
                missingPeople--
                refreshCounter()
            }
        }
        fun selectChip(selected: TextView, all: List<TextView>) {
            all.forEach { chip ->
                chip.alpha = 0.5f
                chip.scaleX = 1f
                chip.scaleY = 1f
            }

            selected.alpha = 1f
            selected.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).start()
        }
        // 🔥 GENERAR POSICIONES SEGÚN DEPORTE
        fun loadPositions(sport: String) {
            container.removeAllViews()
            positionCounts.clear()

            val positions = when (sport) {
                "Football" -> listOf("Goalkeeper", "Defender", "Midfielder", "Forward")
                "Basketball" -> listOf("Base", "Alero", "Pivot")
                else -> listOf("Player")
            }

            positions.forEach { position ->

                positionCounts[position] = 0

                val row = LinearLayout(this)
                row.orientation = LinearLayout.HORIZONTAL
                row.gravity = Gravity.CENTER_VERTICAL

                val name = TextView(this)
                name.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                name.text = position

                val minus = Button(this)
                minus.text = "-"

                val count = TextView(this)
                count.text = "0"
                count.setPadding(20, 0, 20, 0)

                val plus = Button(this)
                plus.text = "+"

                plus.setOnClickListener {
                    val total = positionCounts.values.sum()
                    if (total < missingPeople) {
                        positionCounts[position] = positionCounts[position]!! + 1
                        count.text = positionCounts[position].toString()
                    }
                }

                minus.setOnClickListener {
                    if (positionCounts[position]!! > 0) {
                        positionCounts[position] = positionCounts[position]!! - 1
                        count.text = positionCounts[position].toString()
                    }
                }

                row.addView(name)
                row.addView(minus)
                row.addView(count)
                row.addView(plus)

                container.addView(row)
            }
        }

        // SPORT SELECTION
        val football = findViewById<TextView>(R.id.chipFootball)
        val basket = findViewById<TextView>(R.id.chipBasketball)

        val sports = listOf(football, basket)

        football.setOnClickListener {
            selectChip(football, sports)
            selectedSport = "Football"
            loadPositions("Football")
        }

        basket.setOnClickListener {
            selectChip(basket, sports)
            selectedSport = "Basketball"
            loadPositions("Basketball")
        }

        // LEVEL
        val beginner = findViewById<TextView>(R.id.chipBeginner)
        val intermediate = findViewById<TextView>(R.id.chipIntermediate)

        val levels = listOf(beginner, intermediate)

        beginner.setOnClickListener {
            selectChip(beginner, levels)
            selectedLevel = "Beginner"
        }

        intermediate.setOnClickListener {
            selectChip(intermediate, levels)
            selectedLevel = "Intermediate"
        }
        // SAVE
        findViewById<Button>(R.id.btnContinue).setOnClickListener {

            val totalSelected = positionCounts.values.sum()

            if (totalSelected != missingPeople) {
                Toast.makeText(this, "La suma debe ser $missingPeople", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val match = hashMapOf(
                "creatorId" to userId,
                "location" to location,
                "date" to date,
                "missingPlayers" to missingPeople,
                "sport" to selectedSport,
                "level" to selectedLevel,
                "positions" to positionCounts
            )

            db.collection("matches")
                .add(match)
                .addOnSuccessListener { doc ->

                    // 🔥 GUARDAR EN HISTORIAL
                    db.collection("users")
                        .document(userId!!)
                        .collection("history")
                        .document(doc.id)
                        .set(match)

                    Toast.makeText(this, "Match Created", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, WaitingPlayersActivity::class.java))
                    finish()
                }
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}