package com.example.kotlinconversionsupportivehousing

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InformationDisplayActivity : AppCompatActivity() {
    var statusText: TextView? = null
    var lastDetected: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.information_display_activity)
//        statusText = findViewById<TextView>(R.id.statusText)
//        lastDetected = findViewById<TextView>(R.id.lastDetectedText)
    }
}
