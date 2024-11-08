
package ma.ensa.projet.ui.prof

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensa.projet.R
import ma.ensa.projet.data.dao.StudentDAO
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.ui.adapters.StudentRecyclerViewAdapter

class StudentsInClassActivity : AppCompatActivity() {
    private lateinit var studentAdapter: StudentRecyclerViewAdapter
    private lateinit var studentDao: StudentDAO
    private var selectedClassId: Long = 0
    private var selectedMajorId: Long = 0  // Add majorId
    private lateinit var rvStudent: RecyclerView
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lecturer_layout_studentinclass_activity)

        Log.d("StudentsInClassActivity", "onCreate called")  // Verify onCreate is being triggered

        // Initialize views
        rvStudent = findViewById(R.id.rvStudent)
        btnBack = findViewById(R.id.btnBack)

        // Set up RecyclerView
        setupRecyclerView()

        // Initialize the DAO
        studentDao = AppDatabase.getInstance(this).studentDAO()

        // Retrieve class ID from the intent
        selectedClassId = intent.getLongExtra("CLASS_ID", -1)
        selectedMajorId = intent.getLongExtra("MAJOR_ID", -1)
        if (selectedClassId != -1L) {
            lifecycleScope.launch {
                fetchStudentsForClassAndMajor(selectedClassId, selectedMajorId)  // Ensure you're passing both class and major IDs
            }
        }

        // Back button functionality
        btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        rvStudent.layoutManager = LinearLayoutManager(this)
        studentAdapter = StudentRecyclerViewAdapter()
        rvStudent.adapter = studentAdapter
    }

    private suspend fun fetchStudentsForClassAndMajor(classId: Long, majorId: Long) {
        try {
            Log.d("StudentsInClassActivity", "Executing query for Class ID: $classId, Major ID: $majorId")

            val studentsWithRelations = withContext(Dispatchers.IO) {
                studentDao.getByClassAndMajor(classId, majorId)
            }

            Log.d("StudentsInClassActivity", "Fetched students Size: ${studentsWithRelations.size}")

            val studentNames = studentsWithRelations.map { it.user.fullName }
            Log.d("StudentsInClassActivity", "Raw students data: ${studentsWithRelations.map {
                "Student ID: ${it.student.id}, User: ${it.user.fullName}, Class ID: ${it.student.classId}, Major ID: ${it.student.majorId}"
            }}")

            withContext(Dispatchers.Main) {
                if (studentNames.isNotEmpty()) {
                    studentAdapter.submitList(studentNames)
                } else {
                    showNoStudentsMessage()
                }
            }
        } catch (e: Exception) {
            Log.e("StudentsInClassActivity", "Error fetching students", e)
            withContext(Dispatchers.Main) {
                Snackbar.make(rvStudent, "Error loading students", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showNoStudentsMessage() {
        Snackbar.make(rvStudent, "No students found for this class and major.", Snackbar.LENGTH_SHORT).show()
    }
}
