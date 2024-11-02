package ma.ensa.projet.fragments


import android.app.Activity.RESULT_OK
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ma.ensa.projet.R
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.data.entities.User
import ma.ensa.projet.ui.LoginActivity
import ma.ensa.projet.utilities.Constants
import ma.ensa.projet.utilities.Utils

class SettingFragment : Fragment() {

    private lateinit var user: User
    private lateinit var avatar: ImageView
    private lateinit var txtUsername: TextView

    private lateinit var btnLogout: RelativeLayout

    private val editProfileLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val userId = result.data?.getLongExtra(Constants.USER_ID, 0) ?: 0
                user = AppDatabase.getInstance(requireContext())?.userDAO()?.getById(userId)!!

                txtUsername.text = user.fullName
                avatar.setImageBitmap(user.avatar?.let { Utils.getBitmapFromBytes(it) })
            }
        }

    companion object {
        fun newInstance(params: Map<String, String>): SettingFragment {
            val fragment = SettingFragment()
            val args = Bundle()

            args.putString(Constants.USER_ID, params[Constants.USER_ID])
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val userId = it.getString(Constants.USER_ID)?.toLong() ?: 0
            fetchUserById(userId)
        }
    }

    private fun fetchUserById(userId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            user = AppDatabase.getInstance(requireContext())?.userDAO()?.getById(userId)!!


        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.z_fragment_settings, container, false)
        val window: Window = requireActivity().window
        window.statusBarColor = requireContext().getColor(R.color.grey)

        initSettingsView(view)
        handleEventListener()

        return view
    }

    private fun initSettingsView( view: View) {
        avatar = view.findViewById(R.id.avatar)
        txtUsername = view.findViewById(R.id.txtUsername)

        btnLogout = view.findViewById(R.id.btnLogout)


        avatar.setImageBitmap(user.avatar?.let { Utils.getBitmapFromBytes(it) })
        txtUsername.text = user.fullName
    }

    private fun handleEventListener() {
        val bundle = Bundle().apply {
            putLong(Constants.USER_ID, user.id)
        }




        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Notification")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ -> performLogout() }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }

    }

    private fun performLogout() {

        val intent = Intent(activity, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }
}
