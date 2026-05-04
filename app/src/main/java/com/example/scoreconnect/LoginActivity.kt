package com.example.scoreconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    companion object {
        private var sessionClearedForThisProcess = false
    }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        if (!sessionClearedForThisProcess) {
            auth.signOut()
            sessionClearedForThisProcess = true
        }

        val emailField = findViewById<EditText>(R.id.etEmail)
        val passwordField = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val signUpButton = findViewById<Button>(R.id.btnSignUp)
        val forgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString()

            emailField.error = null
            passwordField.error = null

            var hasError = false

            if (email.isEmpty()) {
                emailField.error = "Email is required"
                hasError = true
            }

            if (password.isEmpty()) {
                passwordField.error = "Password is required"
                hasError = true
            }

            if (hasError) {
                Toast.makeText(this, "Complete the required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginButton.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Welcome back", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    loginButton.isEnabled = true
                    Toast.makeText(
                        this,
                        e.message ?: "Login error",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        signUpButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        forgotPassword.setOnClickListener {
            val email = emailField.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Recovery email sent", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        e.message ?: "Could not send recovery email",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}