package ma.ensa.projet.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ma.ensa.projet.R
import ma.ensa.projet.data.AppDatabase
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.room.Query
import kotlinx.coroutines.withContext
import ma.ensa.projet.data.entities.SubjectSemesterCrossRef

class SplashActivity : AppCompatActivity() {

    private lateinit var logo: View
    private lateinit var appTitle: View
    private lateinit var progressBar: View
    private lateinit var loadingText: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        logo = findViewById(R.id.logo)
        appTitle = findViewById(R.id.appTitle)
        progressBar = findViewById(R.id.progressBar)
        startAnimations()

        CoroutineScope(Dispatchers.Main).launch {
            // Initialize the database you can comment this after


//            AppDatabase.initializeDatabase(this@SplashActivity)

            delay(3000)

            // Navigate to LoginActivity
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }    }

    private fun startAnimations() {
        val logoFade = ObjectAnimator.ofFloat(logo, View.ALPHA, 0f, 1f)
        val logoSlide = ObjectAnimator.ofFloat(logo, View.TRANSLATION_Y, -100f, 0f)

        val titleFade = ObjectAnimator.ofFloat(appTitle, View.ALPHA, 0f, 1f)
        val titleSlide = ObjectAnimator.ofFloat(appTitle, View.TRANSLATION_Y, 50f, 0f)

        AnimatorSet().apply {
            playTogether(logoFade, logoSlide, titleFade, titleSlide)
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

}