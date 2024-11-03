package ma.ensa.projet.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import org.jetbrains.annotations.Contract
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object Utils {

    @NonNull
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(password.toByteArray())
        return hashedBytes.joinToString("") { String.format("%02x", it) }
    }

    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        val hashedInputPassword = hashPassword(password)
        return hashedInputPassword == hashedPassword
    }

    @NonNull
    fun getBytesFromDrawable(context: Context, drawableId: Int): ByteArray {
        val bitmap = BitmapFactory.decodeResource(context.resources, drawableId)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    @NonNull
    fun getBytesFromBitmap(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    fun getBitmapFromBytes(bytes: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    @NonNull
    fun getBitmapFromView(view: View): Bitmap {
        view.layout(view.left, view.top, view.right, view.bottom)
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun removeVietnameseAccents(str: String?): String? {
        if (str == null) return null
        val normalized = Normalizer.normalize(str, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{M}")
        return pattern.matcher(normalized).replaceAll("")
    }

    fun getYearFromDate(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.YEAR)
    }

    @NonNull
    @Contract("_ -> new")
    fun formatDate(pattern: String): SimpleDateFormat {
        return SimpleDateFormat(pattern, Locale.getDefault())
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @NonNull
    @Contract(pure = true)
    fun getRoleName(role: Constants.Role?): String {
        return when (role) {
            Constants.Role.ADMIN -> "Administrator"
            Constants.Role.LECTURER -> "Lecturer"
            else -> "Student"
        }
    }
}
