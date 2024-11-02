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
import ma.ensa.projet.data.entities.Semester
import ma.ensa.projet.data.entities.Subject
import ma.ensa.projet.data.entities.SubjectSemesterCrossRef
import ma.ensa.projet.utilities.Utils
import java.util.ArrayList

class SubjectListActivity : AppCompatActivity() {

    private var selectedClass: Classe? = null
    private var classes = ArrayList<Classe>()
    private lateinit var classNames: Array<String>
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
                classes = ArrayList(db?.classDAO()?.getAll() ?: listOf())
                semesters = ArrayList(db?.semesterDAO()?.getAll() ?: listOf())
                val subjects = ArrayList(db?.subjectDAO()?.getAllWithRelations() ?: listOf())
                Log.d("SubjectListActivity", "Subjects from DB: ${subjects.joinToString { it.subject.name }
                }")

                withContext(Dispatchers.Main) {
                    classNames = Array(classes.size) { classes[it].name.toString() }
                    semesterNames = Array(semesters.size) { semesters[it].name.toString() }

                    val rvSubject: RecyclerView = findViewById(R.id.rvSubject)
                    subjectListRecycleViewAdapter = SubjectListRecycleViewAdapter(this@SubjectListActivity, subjects)
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

    private fun showAddSubjectDialog() {
        val view: View = LayoutInflater.from(this).inflate(R.layout.admin_bottom_sheet_add_subject, null)
        bottomSheetDialog.setContentView(view)
        val behavior: BottomSheetBehavior<View> = BottomSheetBehavior.from(view.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()

        // Set up class selection
        view.findViewById<EditText>(R.id.edtClass).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Select Class")
                .setItems(classNames) { dialog, which ->
                    selectedClass = classes[which]
                    view.findViewById<EditText>(R.id.edtClass).setText(classNames[which])
                    updateSemestersForMajor(selectedClass!!.majorId, view) // Filter semesters
                }
                .show()
        }

        // Set up semester selection
        view.findViewById<EditText>(R.id.edtSemester).setOnClickListener {
            if (semesters.isNotEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Select Semester")
                    .setItems(semesterNames) { dialog, which ->
                        selectedSemester = semesters[which]
                        view.findViewById<EditText>(R.id.edtSemester).setText(semesterNames[which])
                    }
                    .show()
            } else {
                Utils.showToast(this, "Please select a class first")
            }
        }

        view.findViewById<Button>(R.id.btnAddSubject).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Notification")
                .setMessage("Add new subject?")
                .setPositiveButton("Yes") { dialog, which -> performAddSubject(view) }
                .setNegativeButton("No", null)
                .show()
        }
    }

    // This function filters the semesters based on the selected class's major
    private fun updateSemestersForMajor(majorId: Long?, view: View) {
        if (majorId != null) {
            // Assuming you have a method in your DAO to get semesters by major ID
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(this@SubjectListActivity)
                    val filteredSemesters = db?.semesterDAO()?.getSemestersByMajorId(majorId) ?: emptyList()

                    withContext(Dispatchers.Main) {
                        semesters.clear()
                        semesters.addAll(filteredSemesters)
                        semesterNames = Array(semesters.size) { semesters[it].name.toString() } // Update names
                    }
                }
            }
        } else {
            semesters.clear()
            semesterNames = emptyArray() // Clear semesters if majorId is null
        }
    }

    private fun performAddSubject(view: View) {
        if (!validateInputs(view)) return

        val subject = selectedClass!!.majorId?.let {
            Subject(
                name = view.findViewById<EditText>(R.id.edtSubjectName).text.toString(),
                credits = view.findViewById<EditText>(R.id.edtSubjectCredits).text.toString().toInt(),
                classId = selectedClass!!.id,
                majorId = it  // Use the major ID from the selected class
            )
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(this@SubjectListActivity)

                // Insert the subject
                val subjectId = subject?.let { db?.subjectDAO()?.insert(it) }

                // Insert the semester cross reference
                if (subjectId != null) {
                    val crossRef = SubjectSemesterCrossRef(
                        subjectId = subjectId,
                        semesterId = selectedSemester!!.id
                    )
                    db.subjectSemesterCrossRefDAO()?.insert(crossRef)
                }

                // Fetch the newly created subject with relations
                val newSubjectWithRelations = db?.subjectDAO()?.getSubjectWithRelationsById(subjectId!!)

                withContext(Dispatchers.Main) {
                    if (newSubjectWithRelations != null) {
                        subjectListRecycleViewAdapter.addSubject(newSubjectWithRelations)
                        bottomSheetDialog.dismiss()
                        Utils.showToast(this@SubjectListActivity, "Added successfully")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Utils.showToast(this@SubjectListActivity, "Error: ${e.message}")
                }
                Log.e("AddSubject", "Error: ${e.message}", e)
            }
        }
    }

    private fun validateInputs(view: View): Boolean {
        if (selectedClass == null || selectedSemester == null) {
            Utils.showToast(this, "Please select both class and semester")
            return false
        }

        val name = view.findViewById<EditText>(R.id.edtSubjectName).text.toString()
        if (name.isBlank()) {
            Utils.showToast(this, "Please enter subject name")
            return false
        }

        val credits = view.findViewById<EditText>(R.id.edtSubjectCredits).text.toString()
        if (credits.isBlank() || credits.toIntOrNull() == null) {
            Utils.showToast(this, "Please enter valid credits")
            return false
        }

        return true
    }}
