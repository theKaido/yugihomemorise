package com.yugioh_memorise.yugioh_memorise

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SoloMode : AppCompatActivity() {

    private lateinit var cards: List<ImageButton>
    private var isClickable = true
    private var firstCard: ImageButton? = null
    private var secondCard: ImageButton? = null
    private var score = 0
    private lateinit var scoreTextView: TextView
    private lateinit var timeTextView: TextView
    private var timeInSeconds = 0
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var sharedPreferences: SharedPreferences

    // Tableau de tags mélangés
    private val shuffledTags = mutableListOf(
        R.drawable.carte1,
        R.drawable.carte2,
        R.drawable.carte3,
        R.drawable.carte1,
        R.drawable.carte2,
        R.drawable.carte3
    ).shuffled()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_solo_mode)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialise les préférences partagées
        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)


        val restartButton = findViewById<Button>(R.id.restart)

        restartButton.setOnClickListener {
            resetGame()
        }

        val quitterButton = findViewById<Button>(R.id.quitter)

        quitterButton.setOnClickListener {
            saveScoreAndTime() // Sauvegarde le score et le temps avant de quitter
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Initialiser la liste de cartes
        cards = listOf(
            findViewById(R.id.button1),
            findViewById(R.id.button2),
            findViewById(R.id.button3),
            findViewById(R.id.button4),
            findViewById(R.id.button5),
            findViewById(R.id.button6)
        )

        // Trouver la vue du score par son ID
        scoreTextView = findViewById(R.id.score)
        timeTextView = findViewById(R.id.time)

        // Mélanger les tags et les attribuer aux cartes
        for ((index, card) in cards.withIndex()) {
            card.tag = shuffledTags[index]
            card.setOnClickListener { onCardClick(it as ImageButton) }
        }

        // Mettre à jour la vue du score initiale
        updateScoreView()

        // Initialiser le chronomètre
        initTimer()
    }

    private fun onCardClick(card: ImageButton) {
        if (!isClickable || card == firstCard) {
            return
        }

        flipCard(card)

        if (firstCard == null) {
            // Première carte cliquée
            firstCard = card
        } else {
            // Deuxième carte cliquée
            secondCard = card
            checkForMatch()
        }
    }

    private fun flipCard(card: ImageButton) {
        // Sauvegarder le tag de la carte
        val currentTag = card.tag as? Int ?: 0

        // Effectuer une animation de retournement sur la carte
        val rotationOut = ObjectAnimator.ofPropertyValuesHolder(
            card,
            PropertyValuesHolder.ofFloat("rotationY", 0f, 90f),
            PropertyValuesHolder.ofFloat("alpha", 1f, 0f)
        )
        rotationOut.duration = 250
        rotationOut.start()

        val rotationIn = ObjectAnimator.ofPropertyValuesHolder(
            card,
            PropertyValuesHolder.ofFloat("rotationY", -90f, 0f),
            PropertyValuesHolder.ofFloat("alpha", 0f, 1f)
        )
        rotationIn.duration = 250
        rotationIn.startDelay = 250

        val set = android.animation.AnimatorSet()
        set.playSequentially(rotationOut, rotationIn)
        set.start()

        // Désactiver la carte après le retournement
        card.isClickable = false

        // Définir la ressource de l'image réelle après le retournement
        findViewById<View>(R.id.main).postDelayed({
            card.setImageResource(currentTag)

            // Redimensionner l'image pour remplir la taille de la carte
            card.scaleType = ImageView.ScaleType.FIT_XY
        }, 250) // Utiliser la valeur appropriée ici
    }

    private fun checkForMatch() {
        isClickable = false

        if (getCardDrawableResource(firstCard) == getCardDrawableResource(secondCard)) {
            // Paire trouvée, laissez les cartes retournées
            score += 100 // Augmentez le score de 100 points
            updateScoreView() // Mettez à jour la vue du score
            resetCards()

            // Vérifier si toutes les paires ont été trouvées
            if (allPairsFound()) {
                // Réinitialiser et remélanger les cartes après une courte pause
                findViewById<View>(R.id.main).postDelayed({
                    resetAndShuffleCards()
                }, 1000)
            }
        } else {
            // Paire incorrecte, retournez les cartes après une courte pause
            findViewById<View>(R.id.main).postDelayed({
                flipBack(firstCard!!)
                flipBack(secondCard!!)
                resetCards()
            }, 1000)
        }
    }

    private fun allPairsFound(): Boolean {
        return cards.all { !it.isClickable }
    }

    private fun resetAndShuffleCards() {
        // Créer une copie mélangée des tags
        val shuffledCopy = shuffledTags.toMutableList()
        shuffledCopy.shuffle()

        // Mélanger les tags et les attribuer aux cartes
        for ((index, card) in cards.withIndex()) {
            card.tag = shuffledCopy[index]
            card.setImageResource(R.drawable.card_back) // Réinitialiser l'image de la carte
            card.isClickable = true // Rendre la carte à nouveau cliquable
        }

        // Réinitialiser l'état des cartes
        resetCards()
    }

    private fun resetGame() {
        // Réinitialiser le score
        score = 0
        updateScoreView()

        // Réinitialiser le temps
        timeInSeconds = 0
        updateTimerView()

        // Créer une copie mélangée des tags
        val shuffledCopy = shuffledTags.toMutableList()
        shuffledCopy.shuffle()

        // Mélanger les tags et les attribuer aux cartes
        for ((index, card) in cards.withIndex()) {
            card.tag = shuffledCopy[index]
            card.setImageResource(R.drawable.card_back) // Réinitialiser l'image de la carte
            card.isClickable = true // Rendre la carte à nouveau cliquable
        }

        // Réinitialiser l'état des cartes
        resetCards()
    }

    private fun updateScoreView() {
        // Mettez à jour le texte de la vue du score
        scoreTextView.text = "Score : $score"
    }

    private fun updateTimerView() {
        val minutes = timeInSeconds / 60
        val seconds = timeInSeconds % 60
        val formattedTime = String.format("%02d:%02d", minutes, seconds)
        timeTextView.text = "Time:\n$formattedTime"
    }

    private fun resetCards() {
        firstCard = null
        secondCard = null
        isClickable = true
    }

    private fun getCardDrawableResource(card: ImageButton?): Int {
        // Retourne l'ID de la ressource drawable de la carte
        return card?.tag as? Int ?: 0
    }

    private fun flipBack(card: ImageButton) {
        // Effectuer une animation de retournement sur la carte pour la remettre face cachée
        val rotationOut = ObjectAnimator.ofPropertyValuesHolder(
            card,
            PropertyValuesHolder.ofFloat("rotationY", 0f, 90f),
            PropertyValuesHolder.ofFloat("alpha", 1f, 0f)
        )
        rotationOut.duration = 250
        rotationOut.start()

        val rotationIn = ObjectAnimator.ofPropertyValuesHolder(
            card,
            PropertyValuesHolder.ofFloat("rotationY", -90f, 0f),
            PropertyValuesHolder.ofFloat("alpha", 0f, 1f)
        )
        rotationIn.duration = 250
        rotationIn.startDelay = 250

        val set = android.animation.AnimatorSet()
        set.playSequentially(rotationOut, rotationIn)
        set.start()

        // Réinitialiser la carte pour afficher le dos de la carte
        findViewById<View>(R.id.main).postDelayed({
            card.setImageResource(R.drawable.card_back)
            card.scaleType = ImageView.ScaleType.FIT_XY // Réinitialiser l'échelle
            card.isClickable = true // Rendre la carte à nouveau cliquable
        }, 250) // Utiliser la valeur appropriée ici
    }

    private fun initTimer() {
        countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeInSeconds++
                runOnUiThread {
                    updateTimerView()
                }
            }

            override fun onFinish() {
                // Le chronomètre a atteint la fin
                // Vous pouvez ajouter un comportement spécifique ici si nécessaire
            }
        }

        // Démarrer le chronomètre
        countDownTimer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveScoreAndTime() // Sauvegarde le score et le temps lors de la fermeture de l'activité
        // Arrêter le chronomètre lorsqu'il n'est plus nécessaire
        countDownTimer.cancel()
    }

    private fun saveScoreAndTime() {
        // Utilise les préférences partagées pour sauvegarder le score et le temps
        val editor = sharedPreferences.edit()

        // Récupérer la liste existante des scores et temps
        val scoresList: MutableList<Int> = sharedPreferences.getString("scoresList", null)?.split(",")?.map { it.toInt() }?.toMutableList() ?: mutableListOf()
        val timesList: MutableList<Int> = sharedPreferences.getString("timesList", null)?.split(",")?.map { it.toInt() }?.toMutableList() ?: mutableListOf()

        // Ajouter le score et le temps actuels à la liste
        scoresList.add(score)
        timesList.add(timeInSeconds)

        // Limiter la taille des listes à un maximum de 10 éléments
        if (scoresList.size > 10) {
            scoresList.removeAt(0) // Supprimer le plus ancien score
        }

        if (timesList.size > 10) {
            timesList.removeAt(0) // Supprimer le plus ancien temps
        }

        // Enregistrer les listes mises à jour dans les préférences partagées
        editor.putString("scoresList", scoresList.joinToString(","))
        editor.putString("timesList", timesList.joinToString(","))

        // Enregistrer le score et le temps actuels
        editor.putInt("score", score)
        editor.putInt("timeInSeconds", timeInSeconds)

        editor.apply()
    }
}
