package com.example.scoreconnect

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton

object NavigationUtils {

    fun goToHome(activity: Activity) {
        val intent = Intent(activity, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        activity.startActivity(intent)
    }

    fun setupHomeButton(activity: AppCompatActivity, buttonId: Int = R.id.btnHomeTop) {
        activity.findViewById<ImageButton>(buttonId).setOnClickListener {
            goToHome(activity)
        }
    }
}