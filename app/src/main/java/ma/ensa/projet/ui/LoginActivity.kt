package ma.ensa.projet.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensa.projet.R
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.utilities.Constants
import ma.ensa.projet.utilities.Utils
import ma.ensa.projet.utilities.Validator


class LoginActivity : AppCompatActivity() {

    private lateinit var layoutLogin: LinearLayout
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initLoginView()
        handleEventListener()
    }

    @SuppressLint("SetTextI18n")
    private fun initLoginView() {
        layoutLogin = findViewById(R.id.layoutLogin)
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)

        // TODO: TEMP
        val builder = AlertDialog.Builder(this)
        builder.setItems(arrayOf("Admin")) { dialog, which ->
            when (which) {
                0 -> {
                    edtEmail.setText("admin@gmail.com")
                    edtPassword.setText("admin@123")
                }

            }
        }
        builder.show()
    }

    private fun handleEventListener() {
        layoutLogin.setOnClickListener { v ->
            if (v.id == R.id.layoutLogin) {
                val inm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                try {
                    inm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                } catch (ex: Exception) {
                    // Ignore
                }
            }
        }

        btnLogin.setOnClickListener { performLogin() }


    }

    private fun performLogin() {
        if (!validateInputs()) return

        // Launch a coroutine to perform database operations off the main thread
        CoroutineScope(Dispatchers.IO).launch {
            val user = AppDatabase.getInstance(this@LoginActivity)?.userDAO()?.getByEmail(edtEmail.text.toString().trim())
            val users = AppDatabase.getInstance(this@LoginActivity)?.userDAO()?.getAll() ?: emptyList()

            // Check if the user is valid
            if (user == null || !user.password?.let {
                    Utils.verifyPassword(edtPassword.text.toString().trim(),
                        it
                    )
                }!!) {
                withContext(Dispatchers.Main) {
                    Utils.showToast(this@LoginActivity, "Email or password is incorrect!")
                }
                return@launch
            }


            // Navigate to HomeActivity
            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            val bundle = Bundle()
            bundle.putLong(Constants.USER_ID, user.id)
            intent.putExtras(bundle)

            withContext(Dispatchers.Main) {
                startActivity(intent)
                finish()
            }
        }
    }




    private fun validateInputs(): Boolean {
        return validateNotEmpty(R.id.edtEmail, "Email cannot be empty")
                && validateEmail(R.id.edtEmail)
                && validateNotEmpty(R.id.edtPassword, "Password cannot be empty")
    }

    private fun validateNotEmpty(viewId: Int, errorMessage: String): Boolean {
        val editText = findViewById<EditText>(viewId)
        if (editText.text.toString().trim().isEmpty()) {
            Utils.showToast(this, errorMessage)
            return false
        }
        return true
    }

    private fun validateEmail(viewId: Int): Boolean {
        val editText = findViewById<EditText>(viewId)
        if (!Validator.isValidEmail(editText.text.toString())) {
            Utils.showToast(this, "Invalid email")
            return false
        }
        return true
    }
}
