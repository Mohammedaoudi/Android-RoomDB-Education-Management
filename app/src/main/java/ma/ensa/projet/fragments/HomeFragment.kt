package ma.ensa.projet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensa.projet.R
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.data.entities.User
import ma.ensa.projet.utilities.Constants
import ma.ensa.projet.utilities.Utils


class HomeFragment : Fragment() {

    private lateinit var user: User

    companion object {
        fun newInstance(params: Map<String, String>): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putString(Constants.USER_ID, params[Constants.USER_ID])
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            val userId = args.getString(Constants.USER_ID)?.toLong() ?: 0

            // Fetch the user data in a coroutine to avoid ILLEGAL EXCEPTION
            lifecycleScope.launch {
                user = withContext(Dispatchers.IO) {
                    AppDatabase.getInstance(requireContext())?.userDAO()?.getById(userId)!!
                }
                initHomeView()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.z_fragment_home, container, false)
        val window: Window = requireActivity().window
        window.statusBarColor = requireContext().getColor(R.color.grey_sub)
        return view
    }

    private fun initHomeView() {
        val view = view ?: return // Ensure the fragment view is available
        val params = HashMap<String, String>().apply {
            put(Constants.USER_ID, user.id.toString())
        }

        when (user.role) {
            Constants.Role.ADMIN -> loadFragment(AdminFragment.newInstance(params))

            Constants.Role.LECTURER -> loadFragment(LecturerFragment.newInstance(params))
            else -> {
            }
        }

        val avatar: ImageView = view.findViewById(R.id.avatar)
        val txtUsername: TextView = view.findViewById(R.id.txtUsername)

        avatar.setImageBitmap(user.avatar?.let { Utils.getBitmapFromBytes(it) })
        txtUsername.text = user.fullName
    }

    private fun loadFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = childFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }
}
