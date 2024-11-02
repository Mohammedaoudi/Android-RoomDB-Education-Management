package ma.ensa.projet.utilities


import java.util.regex.Pattern

object Validator {

    private val EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

    fun isValidEmail(email: String?): Boolean {
        return !email.isNullOrEmpty() && EMAIL_PATTERN.matches(email)
    }
}