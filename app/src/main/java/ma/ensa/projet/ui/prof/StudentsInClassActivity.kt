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
    private lateinit var rvStudent: RecyclerView
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lecturer_layout_studentinclass_activity)

        // Initialize views
        rvStudent = findViewById(R.id.rvStudent)
        btnBack = findViewById(R.id.btnBack)

        // Set up RecyclerView
        setupRecyclerView()

        // Initialize the DAO
        studentDao = AppDatabase.getInstance(this).studentDAO()

        // Retrieve class ID from the intent
        selectedClassId = intent.getLongExtra("CLASS_ID", -1)
        if (selectedClassId != -1L) {
            lifecycleScope.launch {
                fetchStudentsForClass(selectedClassId)
            }
        }

        // Back button functionality
        btnBack.setOnClickListener { finish() }

        // Handle window insets for layout padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutStudent)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        rvStudent.layoutManager = LinearLayoutManager(this)
        studentAdapter = StudentRecyclerViewAdapter()
        rvStudent.adapter = studentAdapter
    }

    private suspend fun fetchStudentsForClass(classId: Long) {
        // Fetch students for the specified class ID
        val studentsWithRelations = withContext(Dispatchers.IO) {
            studentDao.getByClass(classId) // Ensure this method is a suspend function
        }

        Log.d("StudentsInClassActivity", "Fetched students Size: ${studentsWithRelations.size}")

        val studentNames = studentsWithRelations.map { it.user.fullName } // Assuming 'student' is a property and 'name' is a field of Student
        Log.d("StudentsInClassActivity", "Selected class ID: $selectedClassId")

        if (studentNames.isNotEmpty()) {
            studentAdapter.submitList(studentNames) // Now pass the list of names
        } else {
            showNoStudentsMessage()
        }
    }


    private fun showNoStudentsMessage() {
        Snackbar.make(rvStudent, "No students found for this class.", Snackbar.LENGTH_SHORT).show()
    }
}
