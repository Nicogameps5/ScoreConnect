package com.example.scoreconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        findViewById<Button>(R.id.btnCreateMatch).setOnClickListener {
            startActivity(Intent(this, CreateMatchStep1Activity::class.java))
        }

        findViewById<Button>(R.id.btnFindMatch).setOnClickListener {
            startActivity(Intent(this, FindMatchFiltersActivity::class.java))
        }

        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnLogout).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.btnFriends).setOnClickListener {
            //startActivity(Intent(this, FriendsActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}
