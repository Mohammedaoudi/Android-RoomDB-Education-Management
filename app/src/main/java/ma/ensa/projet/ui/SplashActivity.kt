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
            // Initialize the database
            AppDatabase.initializeDatabase(this@SplashActivity) // Ensure this method is defined in your database class
            loadInitialData() // Load initial data after database initialization

            // Display the splash screen for 3 seconds
            delay(3000)

            // Navigate to LoginActivity
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()  // Finish SplashActivity so it’s removed from the back stack
        }
    }

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

    private fun loadInitialData() {
        // Load majors, academic years, and classes from the database here
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(this@SplashActivity)
            val majors = db.majorDAO().getAll() // Assuming you have a getAll() method
            val academicYears = db.academicYearDAO().getAll()
            val classes = db.classDAO().getAll()

            // Log the results
            Log.d("SplashActivity", "Majors: $majors, Academic Years: $academicYears, Classes: $classes")
        }
    }
}
