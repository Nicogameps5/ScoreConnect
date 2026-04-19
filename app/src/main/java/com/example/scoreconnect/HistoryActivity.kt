package com.example.scoreconnect

import android.os.Bundle
import android.view.Gravity
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val container = findViewById<LinearLayout>(R.id.historyContainer)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // 🔥 CARGAR HISTORIAL
        db.collection("users")
            .document(userId!!)
            .collection("history")
            .get()
            .addOnSuccessListener { result ->

                if (result.isEmpty) {
                    val empty = TextView(this)
                    empty.text = "No tienes partidos aún"
                    empty.textSize = 16f
                    empty.gravity = Gravity.CENTER
                    container.addView(empty)
                    return@addOnSuccessListener
                }

                for (doc in result) {

                    val sport = doc.getString("sport") ?: "Sport"
                    val date = doc.getString("date") ?: "Date"
                    val location = doc.getString("location") ?: "Location"
                    val level = doc.getString("level") ?: "Level"

                    // 🔥 TARJETA
                    val card = LinearLayout(this)
                    card.orientation = LinearLayout.VERTICAL
                    card.setPadding(30, 30, 30, 30)
                    card.setBackgroundResource(R.drawable.bg_card)

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 0, 0, 20)
                    card.layoutParams = params

                    val title = TextView(this)
                    title.text = "⚽ $sport"
                    title.textSize = 18f

                    val info = TextView(this)
                    info.text = "📅 $date\n📍 $location\n🏆 $level"
                    info.textSize = 14f

                    card.addView(title)
                    card.addView(info)

                    container.addView(card)
                }
            }
    }
}