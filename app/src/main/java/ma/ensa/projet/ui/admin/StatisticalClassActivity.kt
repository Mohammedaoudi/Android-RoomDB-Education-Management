package ma.ensa.projet.ui.admin


import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensa.projet.R
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.data.dto.ClassWithRelations
import ma.ensa.projet.data.entities.AcademicYear
import ma.ensa.projet.data.entities.Major
import ma.ensa.projet.adapters.admin.ClassStatisticalRecycleViewAdapter

class StatisticalClassActivity : AppCompatActivity() {

    private lateinit var facultyNames: ArrayList<String>
    private var selectedFacultyId: Long = 0
    private lateinit var majors: ArrayList<Major>
    private lateinit var majorNames: ArrayList<String>
    private var selectedMajorId: Long = 0
    private lateinit var academicYears: ArrayList<AcademicYear>
    private lateinit var academicYearNames: ArrayList<String>
    private var selectedAcademicYearId: Long = 0

    private lateinit var btnBack: ImageView
    private lateinit var edtFaculty: EditText
    private lateinit var edtMajor: EditText
    private lateinit var edtAcademicYear: EditText
    private lateinit var titleTable: LinearLayout
    private lateinit var txtClassCount: TextView
    private lateinit var rvClass: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_statistical_class)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initStatisticalClassView()
        handleEventListener()
    }

    private fun initStatisticalClassView() {
        btnBack = findViewById(R.id.btnBack)
        edtFaculty = findViewById(R.id.edtFaculty)
        edtMajor = findViewById(R.id.edtMajor)
        edtAcademicYear = findViewById(R.id.edtAcademicYear)
        titleTable = findViewById(R.id.titleTable)
        txtClassCount = findViewById(R.id.txtClassCount)
        rvClass = findViewById(R.id.rvClass)

        titleTable.visibility = View.GONE

        // Show loading indicator

        // Launch a coroutine to fetch data from the database
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val academicYearList = AppDatabase.getInstance(this@StatisticalClassActivity)?.academicYearDAO()?.getAll() ?: emptyList()
                val majorList = AppDatabase.getInstance(this@StatisticalClassActivity)?.majorDAO()?.getAll() ?: emptyList()


                val academicYearNamesList = mutableListOf<String>().apply {
                    add("--- Select Academic Year ---")
                    academicYearList.forEach { it.name?.let { name -> add(name) } }
                }
                val majorNamesList = mutableListOf<String>().apply {
                    add("--- Select Major ---")
                    majorList.forEach { it.name?.let { name -> add(name) } }
                }

                // Switch back to the main thread to update the UI
                withContext(Dispatchers.Main) {
                    academicYears = ArrayList(academicYearList)
                    academicYearNames = ArrayList(academicYearNamesList)
                    majors = ArrayList(majorList)
                    majorNames = ArrayList(majorNamesList)

                }
            } catch (e: Exception) {
                // Handle exceptions (e.g., show a toast)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@StatisticalClassActivity, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun handleEventListener() {
        btnBack.setOnClickListener { finish() }



        edtMajor.setOnClickListener {
            showSelectionDialog("Select Major", majorNames) { dialog: DialogInterface, which: Int -> // Specify types explicitly
                if (which == 0) {
                    selectedMajorId = 0
                    edtMajor.setText("")
                    titleTable.visibility = View.VISIBLE
                } else {
                    selectedMajorId = majors[which - 1].id
                    edtMajor.setText(majorNames[which])
                }
                updateClassList()
            }
        }

        edtAcademicYear.setOnClickListener {
            showSelectionDialog("Select Academic Year", academicYearNames) { dialog: DialogInterface, which: Int -> // Specify types explicitly
                if (which == 0) {
                    selectedAcademicYearId = 0
                    edtAcademicYear.setText("")
                } else {
                    selectedAcademicYearId = academicYears[which - 1].id
                    edtAcademicYear.setText(academicYearNames[which])
                }
                updateClassList()
            }
        }
    }

    private fun updateClassList() {
        // Show loading indicator

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val classes: List<ClassWithRelations>? = when {

                    selectedMajorId > 0 && selectedAcademicYearId > 0 ->
                        AppDatabase.getInstance(this@StatisticalClassActivity)?.classDAO()?.getByMajorAcademicYear(selectedMajorId, selectedAcademicYearId)

                    selectedMajorId > 0 ->
                        AppDatabase.getInstance(this@StatisticalClassActivity)?.classDAO()?.getByMajor(selectedMajorId)
                    selectedAcademicYearId > 0 ->
                        AppDatabase.getInstance(this@StatisticalClassActivity)?.classDAO()?.getByAcademicYear(selectedAcademicYearId)
                    else -> emptyList()
                }

                // Switch back to the main thread to update the UI
                withContext(Dispatchers.Main) {
                    rvClass.layoutManager = LinearLayoutManager(this@StatisticalClassActivity)
                    rvClass.adapter = ClassStatisticalRecycleViewAdapter(this@StatisticalClassActivity, ArrayList(classes))
                    txtClassCount.text = classes?.size.toString()
                    titleTable.visibility = View.VISIBLE

                    // Hide loading indicator
                }
            } catch (e: Exception) {
                // Handle exceptions (e.g., show a toast)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@StatisticalClassActivity, "Error loading classes: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showSelectionDialog(title: String, options: List<String>, listener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(options.toTypedArray(), listener)
            .show()
    }
}
