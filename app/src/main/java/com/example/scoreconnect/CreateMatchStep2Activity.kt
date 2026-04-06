package com.example.scoreconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CreateMatchStep2Activity : AppCompatActivity() {

    private var missingPeople = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_match_step2)

        val missingCounter = findViewById<TextView>(R.id.tvMissingPeopleCount)
        val increase = findViewById<Button>(R.id.btnIncrease)
        val decrease = findViewById<Button>(R.id.btnDecrease)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        fun refreshCounter() {
            missingCounter.text = missingPeople.toString()
        }

        refreshCounter()

        increase.setOnClickListener {
            missingPeople += 1
            refreshCounter()
        }

        decrease.setOnClickListener {
            if (missingPeople > 1) {
                missingPeople -= 1
                refreshCounter()
            }
        }

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            //startActivity(Intent(this, WaitingPlayersActivity::class.java))
        }
    }
}
