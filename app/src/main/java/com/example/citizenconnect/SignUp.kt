package com.example.citizenconnect

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignUp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Handle "Login" text click to navigate back to Login Activity
        val loginText = findViewById<TextView>(R.id.login) // ID from the TextView in Sign Up page
        loginText.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish() // Optional: Close SignUp activity so the user can't go back to it
        }

        // Handle the Sign Up button click (if you want to handle the sign-up logic)
        val signupButton = findViewById<Button>(R.id.signupButton)
        signupButton.setOnClickListener {
            // Handle sign-up logic here
            // After successful sign-up, move to MainActivity or another appropriate activity
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
}
