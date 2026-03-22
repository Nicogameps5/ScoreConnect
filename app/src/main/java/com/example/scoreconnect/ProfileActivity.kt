package com.example.scoreconnect

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            Toast.makeText(this, "Settings screen pending", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnEditUsername).setOnClickListener {
            Toast.makeText(this, "Edit username", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnEditSports).setOnClickListener {
            Toast.makeText(this, "Edit sports", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnEditDescription).setOnClickListener {
            Toast.makeText(this, "Edit description", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnLogoutProfile).setOnClickListener {
            finishAffinity()
        }
    }
}
