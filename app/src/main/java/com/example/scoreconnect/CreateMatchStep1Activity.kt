package com.example.scoreconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class CreateMatchStep1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_match_step1)

        val etLocation = findViewById<EditText>(R.id.etLocation)
        val etDate = findViewById<EditText>(R.id.etDate)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnContinue).setOnClickListener {

            val location = etLocation.text.toString()
            val date = etDate.text.toString()

            val intent = Intent(this, CreateMatchStep2Activity::class.java)
            intent.putExtra("location", location)
            intent.putExtra("date", date)

            startActivity(intent)
        }
    }
}