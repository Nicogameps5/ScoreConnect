package com.example.scoreconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CreateMatchStep1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_match_step1)
        NavigationUtils.setupHomeButton(this)

        val etLocation = findViewById<EditText>(R.id.etLocation)
        val etDateTime = findViewById<EditText>(R.id.etDateTime)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            val location = etLocation.text.toString().trim()
            val dateTime = etDateTime.text.toString().trim()

            if (location.isEmpty() || dateTime.isEmpty()) {
                Toast.makeText(this, "Complete location and date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, CreateMatchStep2Activity::class.java)
            intent.putExtra("location", location)
            intent.putExtra("dateTime", dateTime)
            startActivity(intent)
        }
    }
}