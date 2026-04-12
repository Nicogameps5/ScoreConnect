package com.example.scoreconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scoreconnect.adapters.FriendAdapter
import com.example.scoreconnect.model.Friend


class FriendsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvFriends)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = FriendAdapter(
            listOf(
                Friend("Ruben"),
                Friend("Andrea"),
                Friend("Javi")
            )
        ) {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        findViewById<Button>(R.id.btnAddFriends).setOnClickListener {
            Toast.makeText(this, "Search friends flow pending", Toast.LENGTH_SHORT).show()
        }
    }
}
