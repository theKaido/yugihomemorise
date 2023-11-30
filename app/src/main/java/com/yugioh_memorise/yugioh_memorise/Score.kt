package com.yugioh_memorise.yugioh_memorise

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Score : AppCompatActivity() {

    private lateinit var clickSound: MediaPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_score)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        clickSound = MediaPlayer.create(this, R.raw.testsound)

        val isNightMode = when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }

        val backButton = findViewById<Button>(R.id.backbutton)
        val scoreEditText = findViewById<EditText>(R.id.editTextTextMultiLine)

        if (isNightMode) {
            backButton.setTextColor(Color.WHITE)
            scoreEditText.setTextColor(Color.WHITE)
            scoreEditText.setBackgroundResource(R.color.grayspec)
        } else {
            backButton.setTextColor(Color.BLACK)
            scoreEditText.setTextColor(Color.BLACK)
            scoreEditText.setBackgroundColor(Color.WHITE)
        }

        // Récupérer les scores et temps depuis les préférences partagées
        val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        val scoresList: List<Int> = sharedPreferences.getString("scoresList", null)?.split(",")?.map { it.toInt() } ?: emptyList()
        val timesList: List<Int> = sharedPreferences.getString("timesList", null)?.split(",")?.map { it.toInt() } ?: emptyList()

        // Construire la chaîne de texte pour afficher les scores et temps dans l'EditText
        val stringBuilder = StringBuilder()
        stringBuilder.append("Score | Temps\n")

        for (i in scoresList.indices) {
            val scoreText = scoresList[i].toString().padEnd(10)
            val timeText = formatTime(timesList[i])
            stringBuilder.append("$scoreText| $timeText\n")
        }

        // Afficher la chaîne de texte dans l'EditText
        scoreEditText.setText(stringBuilder.toString())


        backButton.setOnClickListener {
            playClickSound()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // Méthode pour formater le temps en minutes:secondes
    private fun formatTime(timeInSeconds: Int): String {
        val minutes = timeInSeconds / 60
        val seconds = timeInSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
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
