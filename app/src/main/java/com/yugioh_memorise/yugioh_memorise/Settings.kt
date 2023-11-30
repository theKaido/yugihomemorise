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
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat

class Settings : AppCompatActivity() {

    private lateinit var volumeSeekBar: SeekBar
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var clickSound: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        clickSound = MediaPlayer.create(this, R.raw.testsound)

        val isNightMode = when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }

        // Element get
        val back = findViewById<Button>(R.id.back)
        val switchCompat = findViewById<SwitchCompat>(R.id.nightmode)
        val skinButton = findViewById<Button>(R.id.skinbutton)
        val resestButton = findViewById<Button>(R.id.resetbutton)
        val soundController = findViewById<ImageButton>(R.id.Soundcontroller)

        soundController.setOnClickListener {
            toggleSeekBar()
            playClickSound()
        }

        // SeekBar pour le volume
        volumeSeekBar = findViewById(R.id.volume)
        volumeSeekBar.max = getMaxVolume()

        // Charger le niveau de volume actuel et définir la valeur initiale de la SeekBar
        volumeSeekBar.progress = loadVolume()

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateVolume(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Non nécessaire pour cette implémentation
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Non nécessaire pour cette implémentation
            }
        })

        // UI Content
        if (isNightMode) {
            back.setTextColor(Color.WHITE)
            skinButton.setTextColor(Color.WHITE)
            resestButton.setTextColor(Color.WHITE)
            switchCompat.setTextColor(Color.WHITE)
            switchCompat.setBackgroundResource(R.color.grayspec)
        } else {
            back.setTextColor(Color.BLACK)
            skinButton.setTextColor(Color.BLACK)
            resestButton.setTextColor(Color.BLACK)
            switchCompat.setTextColor(Color.BLACK)
            switchCompat.setBackgroundColor(Color.WHITE)
        }

        // Functionnalité
        back.setOnClickListener {
            playClickSound()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        switchCompat.isChecked = loadNightModeState()

        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            playClickSound()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )

            saveNightModeState(isChecked)
            recreate()
        }

        // Ajout du listener pour le bouton de réinitialisation
        resestButton.setOnClickListener {
            playClickSound()
            showResetConfirmationDialog()
        }

        skinButton.setOnClickListener {
            playClickSound()
            val intent = Intent(this, CardSkin::class.java)
            startActivity(intent)
        }
    }

    // Fonction pour afficher la boîte de dialogue de confirmation de réinitialisation des scores
    private fun showResetConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Réinitialiser les scores")
        builder.setMessage("Êtes-vous sûr de vouloir réinitialiser tous les scores enregistrés?")
        builder.setPositiveButton("Oui") { _, _ ->
            // Réinitialiser les scores (vous pouvez appeler la fonction appropriée ici)
            resetScores()
            showScoreResetPopup()
        }
        builder.setNegativeButton("Non") { _, _ ->
            // L'utilisateur a choisi de ne pas réinitialiser les scores
        }
        builder.create().show()
    }

    // Fonction pour réinitialiser les scores (supprimer les scores enregistrés)
    private fun resetScores() {
        val editor = sharedPreferences.edit()
        editor.remove("scoresList")
        editor.remove("timesList")
        editor.apply()
    }

    // Fonction pour afficher un pop-up indiquant que les scores ont été réinitialisés avec succès
    private fun showScoreResetPopup() {
        val popupBuilder = AlertDialog.Builder(this)
        popupBuilder.setTitle("Scores réinitialisés")
        popupBuilder.setMessage("Les scores ont été réinitialisés avec succès.")
        popupBuilder.setPositiveButton("OK") { _, _ ->
            // L'utilisateur a appuyé sur OK après la réinitialisation des scores
        }
        popupBuilder.create().show()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Vous pouvez ajouter des actions supplémentaires si nécessaire
    }

    private fun saveNightModeState(isNightMode: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("night_mode", isNightMode)
        editor.apply()
    }

    private fun loadNightModeState(): Boolean {
        return sharedPreferences.getBoolean("night_mode", false)
    }

    private fun updateVolume(progress: Int) {
        val maxVolume = getMaxVolume()
        val volume = (progress.toFloat() / volumeSeekBar.max.toFloat() * maxVolume).toInt()
        setVolume(volume)
        val soundController = findViewById<ImageButton>(R.id.Soundcontroller)

        // Vérifier si la progression est à zéro et mettre à jour l'image de l'ImageButton
        if (progress == 0) {
            soundController.setImageResource(R.drawable.mute)
        } else {
            // Si la progression n'est pas à zéro, utilisez l'image de volume par défaut
            soundController.setImageResource(R.drawable.volume)
        }
    }

    private fun setVolume(volume: Int) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)

        // Sauvegarde le niveau de volume
        saveVolume(volume)
    }

    private fun saveVolume(volume: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("volume", volume)
        editor.apply()
    }

    private fun loadVolume(): Int {
        return sharedPreferences.getInt("volume", 0)
    }

    private fun getMaxVolume(): Int {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    private fun toggleSeekBar() {
        val volumeSeekBar = findViewById<SeekBar>(R.id.volume)

        if (volumeSeekBar.progress == 0) {
            // Si la progression est à zéro, définissez-la à 100%
            volumeSeekBar.progress = 100
        } else {
            // Sinon, définissez-la à zéro
            volumeSeekBar.progress = 0
        }

        // Mettez à jour le volume en fonction de la nouvelle progression
        updateVolume(volumeSeekBar.progress)
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
