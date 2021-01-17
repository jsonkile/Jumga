package com.bigheadapps.monkee.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bigheadapps.monkee.R
import com.bigheadapps.monkee.helpers.Functions

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val darkMode = Functions.getBooleanFromSharedPreference("darkmode", this)
        if (!darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}