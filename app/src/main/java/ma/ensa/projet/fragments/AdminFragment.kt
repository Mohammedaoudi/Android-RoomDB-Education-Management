package ma.ensa.projet.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import ma.ensa.projet.R
import ma.ensa.projet.ui.admin.ClassListActivity
import ma.ensa.projet.ui.admin.FeatureActivity
import ma.ensa.projet.ui.admin.StudentListActivity
import ma.ensa.projet.ui.admin.SubjectListActivity
import ma.ensa.projet.ui.admin.UserListActivity
import ma.ensa.projet.utilities.Constants

class AdminFragment : Fragment() {

    private var userId: Long = 0

    private lateinit var btnToUserManagement: CardView
    private lateinit var btnToStudentManagement: CardView
    private lateinit var btnToScoreManagement: CardView
    private lateinit var btnToClassManagement: CardView
    private lateinit var btnToSubjectManagement: CardView
    private lateinit var btnToFeature: CardView

    companion object {
        fun newInstance(params: Map<String, String>): AdminFragment {
            val fragment = AdminFragment()
            val args = Bundle()

            args.putString(Constants.USER_ID, params[Constants.USER_ID])
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            userId = it.getString(Constants.USER_ID)?.toLong() ?: 0
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.z_fragment_admin, container, false)
        val window: Window = requireActivity().window
        window.statusBarColor = requireContext().getColor(R.color.grey)

        initAdminView(view)
        handleEventListener()

        return view
    }

    private fun initAdminView(view: View) {
        btnToUserManagement = view.findViewById(R.id.btnToUserManagement)
        btnToStudentManagement = view.findViewById(R.id.btnToStudentManagement)
        btnToClassManagement = view.findViewById(R.id.btnToClassManagement)
        btnToSubjectManagement = view.findViewById(R.id.btnToSubjectManagement)
        btnToFeature = view.findViewById(R.id.btnToFeature)
    }

    private fun handleEventListener() {
        btnToUserManagement.setOnClickListener {
            val intent = Intent(activity, UserListActivity::class.java).apply {
                putExtras(Bundle().apply {
                    putLong(Constants.USER_ID, userId)
                })
            }
            startActivity(intent)
        }

        btnToStudentManagement.setOnClickListener {
            val intent = Intent(activity, StudentListActivity::class.java)
            startActivity(intent)
        }

        btnToClassManagement.setOnClickListener {
            val intent = Intent(activity, ClassListActivity::class.java)
            startActivity(intent)
        }

        btnToSubjectManagement.setOnClickListener {
            val intent = Intent(activity, SubjectListActivity::class.java)
            startActivity(intent)
        }

        btnToFeature.setOnClickListener {
            val intent = Intent(activity, FeatureActivity::class.java)
            startActivity(intent)
        }
    }
}
