package com.yugioh_memorise.yugioh_memorise

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var clickSound: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = getPreferences(Context.MODE_PRIVATE)

        // Vérifie si c'est le tout premier lancement de l'application
        val isFirstLaunch = sharedPreferences.getBoolean("is_first_launch", true)

        // Si c'est le premier lancement, configure le volume à 100%
        if (isFirstLaunch) {
            setVolumeToMax()
            // Marque que ce n'est plus le premier lancement
            sharedPreferences.edit().putBoolean("is_first_launch", false).apply()
        }

        // Initialize the MediaPlayer for the click sound
        clickSound = MediaPlayer.create(this, R.raw.testsound)

        val isNightMode = when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }

        // Element get
        val soloButton = findViewById<Button>(R.id.soloButton)
        val multiButton = findViewById<Button>(R.id.multiplayerButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        val scoreButton = findViewById<Button>(R.id.scoreButton)
        val creditButton = findViewById<Button>(R.id.creditButton)

        // UI Content
        if (isNightMode) {
            soloButton.setTextColor(Color.WHITE)
            multiButton.setTextColor(Color.WHITE)
            settingsButton.setTextColor(Color.WHITE)
            scoreButton.setTextColor(Color.WHITE)
            creditButton.setTextColor(Color.WHITE)
        } else {
            soloButton.setTextColor(Color.BLACK)
            multiButton.setTextColor(Color.BLACK)
            settingsButton.setTextColor(Color.BLACK)
            scoreButton.setTextColor(Color.BLACK)
            creditButton.setTextColor(Color.BLACK)
        }

        // Functionnality
        soloButton.setOnClickListener {
            playClickSound()
            val intent = Intent(this, SoloMode::class.java)
            startActivity(intent)
        }

        scoreButton.setOnClickListener {
            playClickSound()
            val intent = Intent(this, Score::class.java)
            startActivity(intent)
        }

        creditButton.setOnClickListener {
            playClickSound()
            val intent = Intent(this, Credits::class.java)
            startActivity(intent)
        }

        settingsButton.setOnClickListener {
            playClickSound()
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        multiButton.setOnClickListener {
            playClickSound()
            val intent = Intent(this, MultiMode::class.java)
            startActivity(intent)
        }
    }

    private fun setVolumeToMax() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
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
