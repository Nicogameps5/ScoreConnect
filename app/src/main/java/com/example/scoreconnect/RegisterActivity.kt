package com.example.scoreconnect

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.scoreconnect.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailField = findViewById<EditText>(R.id.etRegisterEmail)
        val usernameField = findViewById<EditText>(R.id.etRegisterUsername)
        val descriptionField = findViewById<EditText>(R.id.etRegisterDescription)
        val passwordField = findViewById<EditText>(R.id.etRegisterPassword)
        val createAccountButton = findViewById<Button>(R.id.btnCreateAccount)
        val backToLoginButton = findViewById<Button>(R.id.btnBackToLogin)

        createAccountButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val username = usernameField.text.toString().trim()
            val description = descriptionField.text.toString().trim()
            val password = passwordField.text.toString()

            if (email.isEmpty() || username.isEmpty() || description.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Complete all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createAccountButton.isEnabled = false

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid

                    if (uid == null) {
                        createAccountButton.isEnabled = true
                        Toast.makeText(this, "Could not get user ID", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val user = User(
                        uid = uid,
                        email = email,
                        username = username,
                        description = description,
                        sports = emptyList()
                    )

                    db.collection("users")
                        .document(uid)
                        .set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finishAffinity()
                        }
                        .addOnFailureListener { e ->
                            createAccountButton.isEnabled = true
                            Toast.makeText(
                                this,
                                "User created, but profile could not be saved: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    createAccountButton.isEnabled = true
                    Toast.makeText(
                        this,
                        e.message ?: "Error creating account",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        backToLoginButton.setOnClickListener {
            finish()
        }
    }
}