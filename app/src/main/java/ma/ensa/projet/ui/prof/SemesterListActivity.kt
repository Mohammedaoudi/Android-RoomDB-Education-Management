package ma.ensa.projet.ui.prof

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensa.projet.R
import ma.ensa.projet.adapters.prof.SemesterRecyclerViewAdapter
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.data.entities.LecturerSubjectCrossRef
import ma.ensa.projet.data.entities.Semester
import ma.ensa.projet.utilities.Constants


class SemesterListActivity : AppCompatActivity() {

    private lateinit var layoutSemester: LinearLayout
    private lateinit var btnBack: ImageView
    private lateinit var searchViewSemester: SearchView
    private lateinit var semesterRecycleViewAdapter: SemesterRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_semester)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutSemester)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initSemesterView()
        handleEventListener()
    }

    private fun initSemesterView() {
        layoutSemester = findViewById(R.id.layoutSemester)
        btnBack = findViewById(R.id.btnBack)
        searchViewSemester = findViewById(R.id.searchViewSemester)
        val rvSemester: RecyclerView = findViewById(R.id.rvSemester)

        val userId = intent.getLongExtra(Constants.USER_ID, 0)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(this@SemesterListActivity)

                // First, get the lecturer ID from the user ID
                val lecturer = db.lecturerDAO().getByUser(userId)

                if (lecturer == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@SemesterListActivity,
                            "Lecturer not found for this user",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                // Fetch all subject IDs taught by the lecturer
                val lecturerSubjectIds =
                    db.crossRefDAO().getSubjectsByLecturer(lecturer.id).map { it }

                // Fetch all semesters for the lecturer's subjects
                val relevantSemesters = mutableListOf<Semester>()
                for (subjectId in lecturerSubjectIds) {
                    val semesters = db.semesterDAO().getAll() // Get all semesters
                    for (semester in semesters) {
                        // Check if the subject is part of this semester
                        val subjectsInSemester = db.subjectDAO().getBySemester(semester.id)
                        if (subjectsInSemester.any { it.subject.id == subjectId }) {
                            relevantSemesters.add(semester) // Add semester if relevant
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (relevantSemesters.isEmpty()) {
                        Toast.makeText(
                            this@SemesterListActivity,
                            "No semesters found for this lecturer",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    // Create adapter with filtered semesters
                    semesterRecycleViewAdapter = SemesterRecyclerViewAdapter(
                        this@SemesterListActivity,
                        intent,
                        ArrayList(relevantSemesters),
                        lifecycleScope ,
                        lecturer.id
                    )
                    rvSemester.layoutManager = LinearLayoutManager(this@SemesterListActivity)
                    rvSemester.adapter = semesterRecycleViewAdapter
                }
            } catch (e: Exception) {
                Log.e("SemesterListActivityCheck", "Error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SemesterListActivity,
                        "Error loading semesters: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }



    private fun handleEventListener() {
        layoutSemester.setOnClickListener {
            searchViewSemester.clearFocus()
        }

        btnBack.setOnClickListener { finish() }

        searchViewSemester.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                semesterRecycleViewAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                semesterRecycleViewAdapter.filter.filter(newText)
                return false
            }
        })
    }
}