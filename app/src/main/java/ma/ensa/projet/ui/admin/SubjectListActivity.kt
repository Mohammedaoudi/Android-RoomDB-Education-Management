package ma.ensa.projet.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensa.projet.R
import ma.ensa.projet.adapters.admin.SubjectListRecycleViewAdapter
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.data.entities.Classe
import ma.ensa.projet.data.entities.Major
import ma.ensa.projet.data.entities.Semester
import ma.ensa.projet.data.entities.Subject
import ma.ensa.projet.data.entities.SubjectSemesterCrossRef
import ma.ensa.projet.utilities.Utils
import java.util.ArrayList

class SubjectListActivity : AppCompatActivity() {


    private var selectedMajor: Major? = null
    private var majors = ArrayList<Major>()
    private lateinit var majorNames: Array<String>
    private var selectedClass: Classe? = null
    private var classes = ArrayList<Classe>()
    private lateinit var classNames: ArrayList<String>
    private var selectedSemester: Semester? = null
    private var semesters = ArrayList<Semester>()
    private lateinit var semesterNames: Array<String>
    private lateinit var subjectListRecycleViewAdapter: SubjectListRecycleViewAdapter

    private lateinit var layoutSubject: LinearLayout
    private lateinit var btnBack: ImageView
    private lateinit var searchViewSubject: SearchView
    private lateinit var btnAddSubject: Button

    private lateinit var bottomSheetDialog: BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_list_subject)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutSubject)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initSubjectListView()
        handleEventListener()
    }


    private fun initSubjectListView() {
        layoutSubject = findViewById(R.id.layoutSubject)
        btnBack = findViewById(R.id.btnBack)
        searchViewSubject = findViewById(R.id.searchViewSubject)
        btnAddSubject = findViewById(R.id.btnAddSubject)
        bottomSheetDialog = BottomSheetDialog(this)

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(this@SubjectListActivity)
                majors = ArrayList(db?.majorDAO()?.getAll() ?: listOf())
                classes = ArrayList(db?.classDAO()?.getAll() ?: listOf())
                semesters = ArrayList(db?.semesterDAO()?.getAll() ?: listOf())
                val subjects = ArrayList(db?.subjectDAO()?.getAllWithRelations() ?: listOf())
                Log.d(
                    "SubjectListActivity", "Subjects from DB: ${
                        subjects
                    }")

                withContext(Dispatchers.Main) {
                    classNames = ArrayList(classes.size)
                    classNames.addAll(classes.map { it.name.toString() })
                    majorNames = Array(majors.size) { majors[it].name.toString() }

                    semesterNames = Array(semesters.size) { semesters[it].name.toString() }

                    val rvSubject: RecyclerView = findViewById(R.id.rvSubject)
                    subjectListRecycleViewAdapter =
                        SubjectListRecycleViewAdapter(this@SubjectListActivity, subjects)
                    rvSubject.layoutManager = LinearLayoutManager(this@SubjectListActivity)
                    rvSubject.adapter = subjectListRecycleViewAdapter
                }
            }
        }
    }


    private fun handleEventListener() {
        layoutSubject.setOnClickListener { v ->
            if (v.id == R.id.layoutSubject) {
                searchViewSubject.clearFocus()
            }
        }

        btnBack.setOnClickListener { finish() }

        searchViewSubject.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                subjectListRecycleViewAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                subjectListRecycleViewAdapter.filter.filter(newText)
                return false
            }
        })

        btnAddSubject.setOnClickListener { showAddSubjectDialog() }
    }


    private fun validateInputs(view: View): Boolean {
        val subjectName = view.findViewById<EditText>(R.id.edtSubjectName).text.toString()
        val subjectCredits = view.findViewById<EditText>(R.id.edtSubjectCredits).text.toString()

        when {
            subjectName.isEmpty() -> {
                Utils.showToast(this, "Please enter subject name")
                return false
            }

            subjectCredits.isEmpty() -> {
                Utils.showToast(this, "Please enter subject credits")
                return false
            }

            selectedMajor == null -> {
                Utils.showToast(this, "Please select a major")
                return false
            }

            selectedClass == null -> {
                Utils.showToast(this, "Please select a class")
                return false
            }

            selectedSemester == null -> {
                Utils.showToast(this, "Please select a semester")
                return false
            }
        }
        return true
    }

    private fun showAddSubjectDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.admin_bottom_sheet_add_subject, null)
        bottomSheetDialog.setContentView(view)
        val behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()

        // Reset selections when dialog opens
        selectedMajor = null
        selectedClass = null
        selectedSemester = null

        // Major selection
        view.findViewById<EditText>(R.id.edtMajor).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Select Major")
                .setItems(majorNames) { _, which ->
                    selectedMajor = majors[which]
                    view.findViewById<EditText>(R.id.edtMajor).setText(majorNames[which])
                    // Clear dependent selections
                    selectedClass = null
                    selectedSemester = null
                    view.findViewById<EditText>(R.id.edtClass).setText("")
                    view.findViewById<EditText>(R.id.edtSemester).setText("")
                    updateClassesForMajor(selectedMajor!!.id, view)
                }
                .show()
        }

        // Class selection
        view.findViewById<EditText>(R.id.edtClass).setOnClickListener {
            if (selectedMajor == null) {
                Utils.showToast(this, "Please select a major first")
                return@setOnClickListener
            }

            if (classNames.isNotEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Select Class")
                    .setItems(classNames.toTypedArray()) { _, which ->
                        selectedClass = classes.firstOrNull { it.name == classNames[which] }
                        Log.d(" selectesggg", selectedClass.toString())
                        view.findViewById<EditText>(R.id.edtClass).setText(classNames[which])
                        // Clear semester selection
                        selectedSemester = null
                        view.findViewById<EditText>(R.id.edtSemester).setText("")
                        updateSemestersForMajor(selectedClass!!.majorId, view)
                    }
                    .show()
            } else {
                Utils.showToast(this, "No classes available for selected major")
            }
        }
        // Semester selection
        view.findViewById<EditText>(R.id.edtSemester).setOnClickListener {
            if (selectedClass == null) {
                Utils.showToast(this, "Please select a class first")
                return@setOnClickListener
            }

            if (semesters.isNotEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Select Semester")
                    .setItems(semesterNames) { _, which ->
                        selectedSemester = semesters[which]
                        view.findViewById<EditText>(R.id.edtSemester).setText(semesterNames[which])
                    }
                    .show()
            } else {
                Utils.showToast(this, "No semesters available for selected class")
            }
        }

        view.findViewById<Button>(R.id.btnAddSubject).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Notification")
                .setMessage("Add new subject?")
                .setPositiveButton("Yes") { _, _ -> performAddSubject(view) }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun performAddSubject(view: View) {
        if (!validateInputs(view)) return

        // Create subject using the selected values
        val subject = Subject(
            name = view.findViewById<EditText>(R.id.edtSubjectName).text.toString(),
            credits = view.findViewById<EditText>(R.id.edtSubjectCredits).text.toString().toInt(),
            classId = selectedClass!!.id,
            majorId = selectedMajor!!.id  // Use the selected major's ID directly
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(this@SubjectListActivity)

                // Log the values being inserted
                Log.d("AddSubject", "Adding subject: ${subject.name}")
                Log.d(
                    "AddSubject",
                    "Selected Major: ${selectedMajor?.name} (ID: ${selectedMajor?.id})"
                )
                Log.d(
                    "AddSubject",
                    "Selected Class: ${selectedClass?.name} (ID: ${selectedClass?.id})"
                )
                Log.d(
                    "AddSubject",
                    "Selected Semester: ${selectedSemester?.name} (ID: ${selectedSemester?.id})"
                )

                // Insert the subject
                val subjectId = db?.subjectDAO()?.insert(subject)

                if (subjectId != null && selectedSemester != null) {
                    // Insert the semester cross reference
                    val crossRef = SubjectSemesterCrossRef(
                        subjectId = subjectId,
                        semesterId = selectedSemester!!.id
                    )
                    db?.subjectSemesterCrossRefDAO()?.insert(crossRef)

                    // Fetch the newly created subject with relations
                    val newSubjectWithRelations =
                        db?.subjectDAO()?.getSubjectWithRelationsById(subjectId)

                    withContext(Dispatchers.Main) {
                        if (newSubjectWithRelations != null) {
                            subjectListRecycleViewAdapter.addSubject(newSubjectWithRelations)
                            bottomSheetDialog.dismiss()
                            Utils.showToast(this@SubjectListActivity, "Subject added successfully")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AddSubject", "Error adding subject", e)
                withContext(Dispatchers.Main) {
                    Utils.showToast(this@SubjectListActivity, "Error: ${e.message}")
                }
            }
        }
    }

    // This function filters the semesters based on the selected class's major
    private fun updateClassesForMajor(selectedMajorId: Long, view: View) {
        // Clear the previous class names
        classNames.clear()

        // Filter classes based on the selected major
        val filteredClasses = classes!!.filter { it.majorId == selectedMajorId }

        // Check if any classes were found
        if (filteredClasses.isNotEmpty()) {
            // Create a new ArrayList from the filtered class names
            classNames.addAll(filteredClasses.map { it.name ?: "Unknown" }) // Use map to extract names
        } else {
            Utils.showToast(this, "No classes found for the selected major.")
        }

        // Update the class EditText view
        (view.findViewById<View>(R.id.edtClass) as EditText).setText("") // Clear selection
    }
    private fun updateSemestersForMajor(majorId: Long?, view: View) {
        if (majorId != null) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(this@SubjectListActivity)
                    val filteredSemesters =
                        db?.semesterDAO()?.getSemestersByMajorId(majorId) ?: emptyList()

                    withContext(Dispatchers.Main) {
                        semesters.clear()
                        semesters.addAll(filteredSemesters)
                        semesterNames = Array(semesters.size) { semesters[it].name.toString() }
                    }
                }
            }
        } else {
            semesters.clear()
            semesterNames = emptyArray()
        }
    }

}