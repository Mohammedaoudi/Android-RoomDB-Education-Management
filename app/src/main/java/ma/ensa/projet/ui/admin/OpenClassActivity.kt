package ma.ensa.projet.ui.admin

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensa.projet.R
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.data.dto.SubjectWithRelations
import ma.ensa.projet.data.entities.LecturerSubjectCrossRef
import ma.ensa.projet.data.entities.Major
import ma.ensa.projet.data.entities.Semester
import ma.ensa.projet.data.entities.SubjectSemesterCrossRef
import ma.ensa.projet.data.entities.User
import ma.ensa.projet.utilities.Constants
import ma.ensa.projet.utilities.Utils
class OpenClassActivity : AppCompatActivity() {

    private lateinit var subjects: ArrayList<SubjectWithRelations>
    private lateinit var subjectNames: ArrayList<String>
    private var selectedSubjectId: Long = 0
    private lateinit var lecturers: ArrayList<User>
    private lateinit var lecturerNames: ArrayList<String>
    private var selectedLecturerId: Long = 0
    private lateinit var semesters: ArrayList<Semester>
    private lateinit var semesterNames: ArrayList<String>
    private var selectedSemesterId: Long = 0
    private lateinit var majors: ArrayList<Major>
    private lateinit var majorNames: ArrayList<String>
    private var selectedMajorId: Long = 0

    private lateinit var btnBack: ImageView
    private lateinit var edtSubject: EditText
    private lateinit var edtLecturer: EditText
    private lateinit var edtSemester: EditText
    private lateinit var edtMajor: EditText

    private lateinit var btnOpenClass: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_open_class)

        // Retrieve the data passed from the previous activity
        selectedSubjectId = intent.getLongExtra("selectedSubjectId", 0)
        selectedLecturerId = intent.getLongExtra("selectedLecturerId", 0)
        selectedSemesterId = intent.getLongExtra("selectedSemesterId", 0)
        selectedMajorId = intent.getLongExtra("selectedMajorId", 0)

        // Log or use these values as needed
        Log.d("OpenClassActivity", "Subject ID: $selectedSubjectId, Lecturer ID: $selectedLecturerId, Semester ID: $selectedSemesterId, Major ID: $selectedMajorId")

        // Continue with your initialization code
        initCreateClassView()
        handleEventListener()
    }

    private fun initCreateClassView() {
        btnBack = findViewById(R.id.btnBack)
        edtSubject = findViewById(R.id.edtSubject)
        edtLecturer = findViewById(R.id.edtLecturer)
        edtSemester = findViewById(R.id.edtSemester)
        edtMajor = findViewById(R.id.edtMajor)
        btnOpenClass = findViewById(R.id.btnOpenClass)

        // Fetch data for Majors, Lecturers, and Semesters
        lifecycleScope.launch {
            // Fetch majors and populate major names
            majors = withContext(Dispatchers.IO) {
                ArrayList(AppDatabase.getInstance(this@OpenClassActivity)?.majorDAO()?.getAll() ?: listOf())
            }
            majorNames = ArrayList(majors.size)
            majors.forEach { it.name?.let { it1 -> majorNames.add(it1) } }

            // Fetch lecturers and populate lecturer names
            lecturers = withContext(Dispatchers.IO) {
                ArrayList(AppDatabase.getInstance(this@OpenClassActivity)?.userDAO()?.getByRole(Constants.Role.LECTURER) ?: listOf())
            }
            lecturerNames = ArrayList(lecturers.size)
            lecturers.forEach { it.fullName?.let { name -> lecturerNames.add(name) } }

            // Load Semesters for the selected major
            loadSemestersForSelectedMajor(selectedMajorId)

            // Set initial values if available (preselect the passed values)
            setInitialValues()
        }
    }

    private fun setInitialValues() {
        // Set initial values for subject, lecturer, semester, and major
        lifecycleScope.launch {
            // Get selected subject
            val selectedSubject = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(this@OpenClassActivity)?.subjectDAO()?.getById(selectedSubjectId)
            }
            selectedSubject?.let {
                edtSubject.setText(it.name)
            }

            // Get selected lecturer
            val selectedLecturer = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(this@OpenClassActivity)?.userDAO()?.getById(selectedLecturerId)
            }
            selectedLecturer?.let {
                edtLecturer.setText(it.fullName)
            }

            // Get selected semester
            val selectedSemester = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(this@OpenClassActivity)?.semesterDAO()?.getById(selectedSemesterId)
            }
            selectedSemester?.let {
                edtSemester.setText(it.name)
            }

            // Get selected major
            val selectedMajor = majors.find { it.id == selectedMajorId }
            selectedMajor?.let {
                edtMajor.setText(it.name)
            }
        }
    }


    private fun performOpenClass() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(this@OpenClassActivity)

                    // Log current state
                    val allLecturerSubjects = db?.crossRefDAO()?.getAllLecturerSubjectCrossRef()
                    val allSubjectSemesters = db?.crossRefDAO()?.getAllSubjectSemesterCrossRef()


                    // Check for lecturer-subject relationship
                    val lecturerSubjectExists = db?.crossRefDAO()?.getLecturerSubjectCrossRef(
                        selectedLecturerId,
                        selectedSubjectId
                    ) != null

                    Log.d("RelationCheck", "lecturerSubjectExists: $lecturerSubjectExists")

                    if (lecturerSubjectExists) {
                        withContext(Dispatchers.Main) {
                            Utils.showToast(this@OpenClassActivity,
                                "This lecturer is already assigned to this subject")
                        }
                        return@withContext
                    }

                    // If we get here, we can create the new lecturer-subject relationship
                    val lecturerSubjectCrossRef = LecturerSubjectCrossRef(
                        lecturerId = selectedLecturerId,
                        subjectId = selectedSubjectId
                    )

                    try {
                        db?.crossRefDAO()?.insertLecturerSubjectCrossRef(lecturerSubjectCrossRef)

                        withContext(Dispatchers.Main) {
                            Utils.showToast(this@OpenClassActivity, "Class created successfully")
                            // Set the result to return assigned lecturer and subject IDs
                            val resultIntent = Intent().apply {
                                putExtra("assignedLecturerId", selectedLecturerId)
                                putExtra("assignedSubjectId", selectedSubjectId)
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish() // Close the activity and return to the previous screen
                        }
                    } catch (e: Exception) {
                        Log.e("InsertError", "Error inserting lecturer-subject relationship", e)
                        withContext(Dispatchers.Main) {
                            Utils.showToast(this@OpenClassActivity, "Error creating class: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("OpenClassActivity", "Error in performOpenClass", e)
                withContext(Dispatchers.Main) {
                    Utils.showToast(this@OpenClassActivity, "Error: ${e.message}")
                }
            }
        }
    }


    private fun handleEventListener() {
        btnBack.setOnClickListener { finish() }

        edtMajor.setOnClickListener {
            showSelectionDialog("Select Major", majorNames) { _, which ->
                // Reset semester and subject selections when major changes
                resetSelections(edtSemester, edtSubject)
                selectedMajorId = majors[which].id
                edtMajor.setText(majorNames[which])
                loadSemestersForSelectedMajor(selectedMajorId) // Load semesters for selected major
            }
        }

        edtSemester.setOnClickListener {
            showSelectionDialog("Select Semester", semesterNames) { _, which ->
                selectedSemesterId = semesters[which].id
                edtSemester.setText(semesterNames[which])
            }
        }

        edtSubject.setOnClickListener {
            if (edtSemester.text.toString().isEmpty() || edtMajor.text.toString().isEmpty()) {
                Utils.showToast(this, "Major and Semester must be selected first")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // Load subjects and initialize `subjectNames`
                loadSubjectsForSelectedMajorAndSemester(selectedMajorId, selectedSemesterId)

                // Wait until `subjectNames` is initialized
                if (subjectNames.isNotEmpty()) {
                    showSelectionDialog("Select Subject", subjectNames) { _, which ->
                        selectedSubjectId = subjects[which].subject.id
                        edtSubject.setText(subjectNames[which])
                    }
                } else {
                    Toast.makeText(this@OpenClassActivity, "No subjects available", Toast.LENGTH_SHORT).show()
                }
            }
        }

        edtLecturer.setOnClickListener {
            showSelectionDialog("Select Lecturer", lecturerNames) { _, which ->
                lifecycleScope.launch {
                    selectedLecturerId = withContext(Dispatchers.IO) {
                        AppDatabase.getInstance(this@OpenClassActivity)
                            ?.lecturerDAO()?.getByUser(lecturers[which].id)?.id ?: 0
                    }
                    edtLecturer.setText(lecturerNames[which])
                }
            }

            btnOpenClass.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Notification")
                    .setMessage("Open class?")
                    .setPositiveButton("Yes") { _, _ -> performOpenClass() }
                    .setNegativeButton("No", null)
                    .show()
            }
        }

    }

    private fun loadSemestersForSelectedMajor(majorId: Long) {
        lifecycleScope.launch {
            semesters = withContext(Dispatchers.IO) {
                // Fetch semesters for the selected major
                ArrayList(AppDatabase.getInstance(this@OpenClassActivity)?.semesterDAO()?.getSemestersByMajorId(majorId) ?: listOf())
            }
            semesterNames = ArrayList(semesters.size)
            semesters.forEach {
                val startDate = Utils.formatDate("MM/yyyy").format(it.startDate)
                val endDate = Utils.formatDate("MM/yyyy").format(it.endDate)
                semesterNames.add("${it.name} ($startDate - $endDate)")
            }
        }
    }




    private suspend fun loadSubjectsForSelectedMajorAndSemester(majorId: Long, semesterId: Long) {
        withContext(Dispatchers.IO) {
            subjects = ArrayList(
                AppDatabase.getInstance(this@OpenClassActivity)?.subjectDAO()
                    ?.getByMajorAndSemester(majorId, semesterId) ?: listOf()
            )
        }
        // Populate `subjectNames` with subject names after fetching `subjects`
        subjectNames = ArrayList<String>(subjects.size).apply {
            subjects.forEach { add(it.subject.name) }
        }

    }

    private fun showSelectionDialog(title: String, options: List<String>, listener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setItems(options.toTypedArray(), listener)
            show()
        }
    }

    private fun resetSelections(edtSemester: EditText, edtSubject: EditText) {
        selectedSemesterId = 0
        edtSemester.setText("")
        resetSelections(edtSubject)
    }

    private fun resetSelections(edtSubject: EditText) {
        selectedSubjectId = 0
        edtSubject.setText("")
    }
}
