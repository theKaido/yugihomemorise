package com.yugioh_memorise.yugioh_memorise

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Credits : AppCompatActivity() {

    private lateinit var clickSound: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_credits)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val isNightMode = when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }

        val back = findViewById<Button>(R.id.back)

        // Initialize the MediaPlayer for the click sound
        clickSound = MediaPlayer.create(this, R.raw.testsound)

        if (isNightMode) {
            back.setTextColor(Color.WHITE)
        } else {
            back.setTextColor(Color.BLACK)
        }

        back.setOnClickListener {
            playClickSound()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun playClickSound() {
        // Check if the MediaPlayer is playing, stop it and reset
        if (clickSound.isPlaying) {
            clickSound.stop()
            clickSound.reset()
        }

        // Start playing the click sound
        clickSound.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release the MediaPlayer resources when the activity is destroyed
        clickSound.release()
    }
}
