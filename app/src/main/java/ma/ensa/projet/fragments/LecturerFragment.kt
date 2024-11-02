package ma.ensa.projet.fragments


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import ma.ensa.projet.ui.prof.SemesterListActivity
import ma.ensa.projet.R
import ma.ensa.projet.utilities.Constants

class LecturerFragment : Fragment() {

    private var userId: Long = 0

    private lateinit var btnToSemester: CardView
    private lateinit var btnToStatistical: CardView

    companion object {
        fun newInstance(params: Map<String, String>): LecturerFragment {
            val fragment = LecturerFragment()
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
        val view = inflater.inflate(R.layout.z_fragment_lecturer, container, false)
        val window: Window = requireActivity().window
        window.statusBarColor = requireContext().getColor(R.color.grey)

        initLecturerView(view)
        handleEventListener()

        return view
    }

    private fun initLecturerView(view: View) {
        btnToSemester = view.findViewById(R.id.btnToSemester)
        btnToStatistical = view.findViewById(R.id.btnToStatistical)
    }

    private fun handleEventListener() {
        val bundle = Bundle().apply {
            putLong(Constants.USER_ID, userId)
        }

        btnToSemester.setOnClickListener {
            val intent = Intent(activity, SemesterListActivity::class.java).apply {
                putExtras(bundle)
            }
            startActivity(intent)
        }


    }
}
