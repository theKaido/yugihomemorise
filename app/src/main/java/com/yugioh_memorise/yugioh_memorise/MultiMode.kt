package com.yugioh_memorise.yugioh_memorise

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

class MultiMode : AppCompatActivity() {

    private lateinit var cards: List<ImageButton>
    private var isClickable = true
    private var firstCard: ImageButton? = null
    private var secondCard: ImageButton? = null
    private var scoreP1 = 0
    private var scoreP2 = 0
    private lateinit var scoreP1TextView: TextView
    private lateinit var scoreP2TextView: TextView

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
        setContentView(R.layout.activity_multi_mode)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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

        // Trouver les vues des scores par leur ID
        scoreP1TextView = findViewById<TextView>(R.id.scorepone)
        scoreP2TextView = findViewById<TextView>(R.id.scoreptwo)

        // Mélanger les tags et les attribuer aux cartes
        for ((index, card) in cards.withIndex()) {
            card.tag = shuffledTags[index]
            card.setOnClickListener { onCardClick(it as ImageButton) }
        }

        val restartButton = findViewById<Button>(R.id.restart)
        val quitterButton = findViewById<Button>(R.id.quitter)

        restartButton.setOnClickListener {
            resetGame()
        }

        quitterButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
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

    private fun resetGame() {
        scoreP1 = 0
        scoreP2 = 0
        updateScoreViews()

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

    private var currentPlayer = 1 // Variable pour suivre le joueur actuel

    private fun checkForMatch() {
        isClickable = false

        if (getCardDrawableResource(firstCard) == getCardDrawableResource(secondCard)) {
            // Paire trouvée, laissez les cartes retournées
            if (currentPlayer == 1) {
                scoreP1 += 100
            } else {
                scoreP2 += 100
            }
            updateScoreViews()
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

                // Changer de joueur après le retournement des cartes incorrectes
                currentPlayer = 3 - currentPlayer // Passe de 1 à 2 et de 2 à 1
            }, 1000)
        }
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

    private fun resetCards() {
        firstCard = null
        secondCard = null
        isClickable = true
    }

    private fun allPairsFound(): Boolean {
        // Vérifiez si toutes les paires ont été trouvées en vérifiant si tous les boutons sont non cliquables
        return cards.all { !it.isClickable }
    }

    private fun updateScoreViews() {
        scoreP1TextView.text = "Score P1: $scoreP1"
        scoreP2TextView.text = "Score P2: $scoreP2"
    }

    private fun getCardDrawableResource(card: ImageButton?): Int {
        return card?.tag as? Int ?: 0
    }

    private fun flipBack(card: ImageButton) {
        card.rotationY = 0f
        card.animate().rotationY(-90f).setDuration(250).withEndAction {
            card.setImageResource(R.drawable.card_back)
            card.rotationY = 90f
            card.animate().rotationY(0f).setDuration(250).start()
            card.isClickable = true
        }.start()
    }
}