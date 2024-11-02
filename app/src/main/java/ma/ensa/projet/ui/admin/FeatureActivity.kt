package ma.ensa.projet.ui.admin


import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.graphics.Insets


import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ma.ensa.projet.R

class FeatureActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnToStatisticalAdmin: CardView
    private lateinit var btnToOpenClass: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // EdgetoEdge.enable(this)
        setContentView(R.layout.admin_activity_feature)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initStatisticalView()
        handleEventListener()
    }

    private fun initStatisticalView() {
        btnBack = findViewById(R.id.btnBack)
        btnToStatisticalAdmin = findViewById(R.id.btnToStatisticalAdmin)
        btnToOpenClass = findViewById(R.id.btnToOpenClass)
    }

    private fun handleEventListener() {
        btnBack.setOnClickListener { finish() }

        btnToStatisticalAdmin.setOnClickListener {
            val intent = Intent(this, StatisticalActivity::class.java)
            startActivity(intent)
        }

        btnToOpenClass.setOnClickListener {
            val intent = Intent(this, OpenClassActivity::class.java)
            startActivity(intent)
        }
    }
}
