package com.example.scoreconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.scoreconnect.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvUsernameValue: TextView
    private lateinit var tvEmailValue: TextView
    private lateinit var tvDescriptionValue: TextView
    private lateinit var layoutSportsContainer: LinearLayout

    private val availableSports = arrayOf(
        "Football",
        "Basketball",
        "Padel",
        "Tennis",
        "Volleyball",
        "Running"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        tvUsernameValue = findViewById(R.id.tvUsernameValue)
        tvEmailValue = findViewById(R.id.tvEmailValue)
        tvDescriptionValue = findViewById(R.id.tvDescriptionValue)
        layoutSportsContainer = findViewById(R.id.layoutSportsContainer)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            loadUserProfile()
            Toast.makeText(this, "Profile refreshed", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnEditUsername).setOnClickListener {
            showEditUsernameDialog()
        }

        findViewById<Button>(R.id.btnEditSports).setOnClickListener {
            showEditSportsDialog()
        }

        findViewById<Button>(R.id.btnEditDescription).setOnClickListener {
            showEditDescriptionDialog()
        }

        findViewById<TextView>(R.id.btnInstagram).setOnClickListener {
            openUrl("https://www.instagram.com/")
        }

        findViewById<TextView>(R.id.btnX).setOnClickListener {
            openUrl("https://x.com/")
        }

        findViewById<TextView>(R.id.btnTikTok).setOnClickListener {
            openUrl("https://www.tiktok.com/")
        }

        findViewById<Button>(R.id.btnLogoutProfile).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }

        loadUserProfile()
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser ?: return

        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)

                    if (user != null) {
                        showUserData(
                            user.copy(
                                email = if (user.email.isBlank()) {
                                    currentUser.email.orEmpty()
                                } else {
                                    user.email
                                }
                            )
                        )
                    } else {
                        showUserData(
                            User(
                                uid = currentUser.uid,
                                email = currentUser.email.orEmpty(),
                                username = "",
                                description = "",
                                sports = emptyList()
                            )
                        )
                    }
                } else {
                    val newUser = User(
                        uid = currentUser.uid,
                        email = currentUser.email.orEmpty(),
                        username = "",
                        description = "",
                        sports = emptyList()
                    )

                    db.collection("users")
                        .document(currentUser.uid)
                        .set(newUser)

                    showUserData(newUser)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showUserData(user: User) {
        tvUsernameValue.text =
            if (user.username.isBlank()) "No username yet" else user.username

        tvEmailValue.text =
            if (user.email.isBlank()) "No email available" else user.email

        tvDescriptionValue.text =
            if (user.description.isBlank()) "No description yet" else user.description

        renderSports(user.sports)
    }

    private fun renderSports(sports: List<String>) {
        layoutSportsContainer.removeAllViews()

        if (sports.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No sports selected yet"
                textSize = 16f
            }
            layoutSportsContainer.addView(emptyText)
            return
        }

        sports.forEach { sport ->
            val chip = TextView(this).apply {
                text = sport
                textSize = 15f
                setBackgroundResource(R.drawable.bg_chip)
                setPadding(14.dp(), 10.dp(), 14.dp(), 10.dp())

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.bottomMargin = 8.dp()
                layoutParams = params
            }

            layoutSportsContainer.addView(chip)
        }
    }

    private fun showEditUsernameDialog() {
        val currentValue =
            if (tvUsernameValue.text.toString() == "No username yet") "" else tvUsernameValue.text.toString()

        val input = EditText(this).apply {
            setText(currentValue)
            setSelection(text.length)
            inputType = InputType.TYPE_CLASS_TEXT
            hint = "Username"
        }

        AlertDialog.Builder(this)
            .setTitle("Edit username")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newUsername = input.text.toString().trim()

                if (newUsername.isEmpty()) {
                    Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                saveUserFields(
                    mapOf("username" to newUsername),
                    "Username updated"
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDescriptionDialog() {
        val currentValue =
            if (tvDescriptionValue.text.toString() == "No description yet") "" else tvDescriptionValue.text.toString()

        val input = EditText(this).apply {
            setText(currentValue)
            setSelection(text.length)
            hint = "Description"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 4
            gravity = Gravity.TOP or Gravity.START
        }

        AlertDialog.Builder(this)
            .setTitle("Edit description")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newDescription = input.text.toString().trim()

                saveUserFields(
                    mapOf("description" to newDescription),
                    "Description updated"
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditSportsDialog() {
        val currentSports = getCurrentSportsFromLayout()

        val checkedItems = BooleanArray(availableSports.size) { index ->
            currentSports.contains(availableSports[index])
        }

        AlertDialog.Builder(this)
            .setTitle("Choose your sports")
            .setMultiChoiceItems(availableSports, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Save") { _, _ ->
                val selectedSports = availableSports.filterIndexed { index, _ ->
                    checkedItems[index]
                }

                saveUserFields(
                    mapOf("sports" to selectedSports),
                    "Sports updated"
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getCurrentSportsFromLayout(): List<String> {
        val sports = mutableListOf<String>()

        for (i in 0 until layoutSportsContainer.childCount) {
            val view = layoutSportsContainer.getChildAt(i)
            if (view is TextView) {
                val text = view.text.toString()
                if (text != "No sports selected yet") {
                    sports.add(text)
                }
            }
        }

        return sports
    }

    private fun saveUserFields(data: Map<String, Any>, successMessage: String) {
        val currentUser = auth.currentUser ?: return

        val finalData = mutableMapOf<String, Any>()
        finalData["uid"] = currentUser.uid
        finalData["email"] = currentUser.email.orEmpty()
        finalData.putAll(data)

        db.collection("users")
            .document(currentUser.uid)
            .set(finalData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
                loadUserProfile()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error saving profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No browser available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun Int.dp(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}