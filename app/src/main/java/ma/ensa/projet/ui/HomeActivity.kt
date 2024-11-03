package ma.ensa.projet.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

import com.google.android.material.bottomnavigation.BottomNavigationView
import ma.ensa.projet.R
import ma.ensa.projet.fragments.HomeFragment
import ma.ensa.projet.fragments.SettingFragment
import ma.ensa.projet.utilities.Constants


class HomeActivity : AppCompatActivity() {

    private lateinit var params: Map<String, String>
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initBottomNavigationView()
        handleEventListener(params)

        // Load the default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.navHome
            loadFragment(HomeFragment.newInstance(params))
        }
    }

    private fun initBottomNavigationView() {
        val bundle = intent.extras

        val userId = bundle?.getLong(Constants.USER_ID, 0) ?: 0

        params = mapOf(Constants.USER_ID to userId.toString())

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
    }

    private fun handleEventListener(params: Map<String, String>) {
        bottomNavigationView.setOnItemSelectedListener { item ->
            val itemId = item.itemId

            when (itemId) {
                R.id.navHome -> {
                    loadFragment(HomeFragment.newInstance(params))
                    true
                }
                R.id.navSetting -> {
                    loadFragment(SettingFragment.newInstance(params))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }
}