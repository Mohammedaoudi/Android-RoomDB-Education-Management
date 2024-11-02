package ma.ensa.projet.ui.admin


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import ma.ensa.projet.R
import ma.ensa.projet.adapters.admin.ClassListRecycleViewAdapter
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.data.dto.ClassWithRelations
import ma.ensa.projet.data.entities.AcademicYear
import ma.ensa.projet.data.entities.Classe
import ma.ensa.projet.data.entities.Major
import ma.ensa.projet.data.entities.Semester
import ma.ensa.projet.utilities.Utils
class ClassListActivity : AppCompatActivity() {

    private var selectedMajor: Major? = null
    private var majors: ArrayList<Major> = arrayListOf()
    private lateinit var majorNames: Array<String>
    private var selectedAcademicYear: AcademicYear? = null
    private var academicYears: ArrayList<AcademicYear> = arrayListOf()
    private lateinit var academicYearNames: Array<String>
    private lateinit var classListRecycleViewAdapter: ClassListRecycleViewAdapter

    private lateinit var layoutClass: RelativeLayout
    private lateinit var btnBack: ImageView
    private lateinit var searchViewClass: SearchView
    private lateinit var btnAddClass: Button

    private lateinit var bottomSheetDialog: BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_list_class)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutClass)) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initClassListView()
        handleEventListener()
    }

    private fun initClassListView() {
        layoutClass = findViewById(R.id.layoutClass)
        btnBack = findViewById(R.id.btnBack)
        btnAddClass = findViewById(R.id.btnAddClass)
        searchViewClass = findViewById(R.id.searchViewClass)
        bottomSheetDialog = BottomSheetDialog(this)

        lifecycleScope.launch {
            val fetchedMajors = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(this@ClassListActivity)?.majorDAO()?.getAll() ?: emptyList()
            }
            val fetchedAcademicYears = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(this@ClassListActivity)?.academicYearDAO()?.getAll() ?: emptyList()
            }
            val fetchedClasses = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(this@ClassListActivity)?.classDAO()?.getAllWithRelations() ?: emptyList()
            }

            majors = ArrayList(fetchedMajors)
            majorNames = majors.mapNotNull { it.name }.toTypedArray()
            academicYears = ArrayList(fetchedAcademicYears)
            academicYearNames = academicYears.mapNotNull { it.name }.toTypedArray()

            val classes = ArrayList(fetchedClasses)
            val rvClass = findViewById<RecyclerView>(R.id.rvClass)
            classListRecycleViewAdapter = ClassListRecycleViewAdapter(
                context = this@ClassListActivity,
                originalList = classes,
                majors = majors,
                academicYears = academicYears
            )
            rvClass.layoutManager = LinearLayoutManager(this@ClassListActivity)
            rvClass.adapter = classListRecycleViewAdapter
        }
    }

    @SuppressLint("InflateParams")
    private fun handleEventListener() {
        layoutClass.setOnClickListener {
            if (it.id == R.id.layoutClass) {
                searchViewClass.clearFocus()
            }
        }

        btnBack.setOnClickListener { finish() }

        searchViewClass.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                classListRecycleViewAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                classListRecycleViewAdapter.filter.filter(newText)
                return false
            }
        })

        btnAddClass.setOnClickListener { showAddClassDialog() }
    }

    @SuppressLint("InflateParams", "MissingInflatedId")
    private fun showAddClassDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.admin_bottom_sheet_add_class, null)
        bottomSheetDialog.setContentView(view)
        val behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        bottomSheetDialog.show()

        view.findViewById<EditText>(R.id.edtMajor).setOnClickListener {
            if (majorNames.isEmpty()) {
                Utils.showToast(this, "No majors available")
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Choose Major")
                .setItems(majorNames) { _, which ->
                    selectedMajor = majors[which]
                    view.findViewById<EditText>(R.id.edtMajor).setText(majorNames[which])
                }
                .show()
        }

        view.findViewById<EditText>(R.id.edtAcademicYear).setOnClickListener {
            if (academicYearNames.isEmpty()) {
                Utils.showToast(this, "No academic years available")
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Choose Academic Year")
                .setItems(academicYearNames) { _, which ->
                    selectedAcademicYear = academicYears[which]
                    view.findViewById<EditText>(R.id.edtAcademicYear).setText(academicYearNames[which])
                }
                .show()
        }

        view.findViewById<Button>(R.id.btnAddClass).setOnClickListener {
            // Logging the selected values
            Log.d("ClassListActivity", "Selected Major: ${selectedMajor?.name}, " +
                    "Selected Academic Year: ${selectedAcademicYear?.name}")

            AlertDialog.Builder(this)
                .setTitle("Notification")
                .setMessage("Add a new class?")
                .setPositiveButton("Yes") { _, _ -> performAddClass(view) }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun performAddClass(view: View) {
        Log.d("ClassListActivity", "Performing Add Class with Major: $selectedMajor, " +
                "Academic Year: $selectedAcademicYear")

        if (!validateInputs(view)) return

        if (selectedMajor == null || selectedAcademicYear == null) {
            Utils.showToast(this, "Please select major and academic year")
            return
        }

        try {
            val clazz = Classe(
                name = view.findViewById<EditText>(R.id.edtClassName).text.toString(),
                majorId = selectedMajor?.id,
                academicYearId = selectedAcademicYear?.id
            )

            val classWithRelations = ClassWithRelations(
                clazz = clazz,
                major = selectedMajor!!,
                academicYear = selectedAcademicYear!!
            )

            classListRecycleViewAdapter.addClass(classWithRelations)
            bottomSheetDialog.dismiss()
            Utils.showToast(this, "Added successfully")

        } catch (e: Exception) {
            Log.e("AddClass", "Error adding class", e)
        }
    }


private fun validateInputs(view: View): Boolean {
    return validateNotEmpty(view, R.id.edtClassName, "Class name cannot be empty") &&
            validateNotEmpty(view, R.id.edtMajor, "Major cannot be empty") &&
            validateNotEmpty(view, R.id.edtAcademicYear, "Academic year cannot be empty")
}

private fun validateNotEmpty(view: View, viewId: Int, errorMessage: String): Boolean {
    val editText = view.findViewById<EditText>(viewId)
    if (editText.text.toString().trim().isEmpty()) {
        Utils.showToast(this, errorMessage) // Correctly reference the activity context
        return false
    }
    return true
}

}


