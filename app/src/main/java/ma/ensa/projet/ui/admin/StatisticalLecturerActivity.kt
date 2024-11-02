package ma.ensa.projet.ui.admin


import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
import ma.ensa.projet.adapters.admin.LecturerStatisticalRecycleViewAdapter
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.data.dto.LecturerAndUser
import ma.ensa.projet.data.dto.StatisticalOfLecturer
import ma.ensa.projet.data.entities.Semester
import ma.ensa.projet.utilities.Utils

class StatisticalLecturerActivity : AppCompatActivity() {

    private lateinit var semesters: ArrayList<Semester>
    private lateinit var semesterNames: ArrayList<String>
    private var selectedSemesterId: Long = 0
    private lateinit var lecturers: ArrayList<LecturerAndUser>
    private lateinit var lecturerNames: ArrayList<String>
    private var selectedLecturerId: Long = 0

    private lateinit var btnBack: ImageView
    private lateinit var edtSemester: EditText
    private lateinit var edtLecturer: EditText
    private lateinit var titleTable: LinearLayout
    private lateinit var txtLecturerName: TextView
    private lateinit var txtSemesterName: TextView
    private lateinit var rvLecturer: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_statistical_lecturer)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initStatisticalLecturerView()
        handleEventListener()
    }

    private fun initStatisticalLecturerView() {
        btnBack = findViewById(R.id.btnBack)
        edtSemester = findViewById(R.id.edtSemester)
        edtLecturer = findViewById(R.id.edtLecturer)
        titleTable = findViewById(R.id.titleTable)
        txtLecturerName = findViewById(R.id.txtLecturerName)
        txtSemesterName = findViewById(R.id.txtSemesterName)
        rvLecturer = findViewById(R.id.rvLecturer)

        titleTable.visibility = View.GONE

        // Launch a coroutine to fetch semesters
        lifecycleScope.launch(Dispatchers.IO) {
            semesters = ArrayList(AppDatabase.getInstance(this@StatisticalLecturerActivity)?.semesterDAO()?.getAll() ?: emptyList())
            val semesterNamesList = mutableListOf<String>().apply {
                add("--- Select Semester ---")  // Translation of "Chọn học kỳ"
                for (semester in semesters) {
                    val startDate = Utils.formatDate("MM/yyyy").format(semester.startDate)
                    val endDate = Utils.formatDate("MM/yyyy").format(semester.endDate)
                    add("${semester.name} ($startDate - $endDate)")
                }
            }

            // Switch to the main thread to update the UI
            withContext(Dispatchers.Main) {
                semesterNames = ArrayList(semesterNamesList)
            }
        }
    }

    private fun handleEventListener() {
        btnBack.setOnClickListener { finish() }

        edtSemester.setOnClickListener {
            showSelectionDialog("Select Semester", semesterNames) { dialog, which ->
                if (which == 0) {
                    resetSelections(edtSemester, edtLecturer)
                } else {
                    selectedSemesterId = semesters[which - 1].id
                    edtSemester.setText(semesterNames[which])
                    resetSelections(edtLecturer)
                }
            }
        }

        edtLecturer.setOnClickListener {
            if (edtSemester.text.toString().isEmpty()) {
                Utils.showToast(this, "Semester not selected")
                return@setOnClickListener
            }

            // Launch a coroutine to fetch lecturers based on selected semester
            lifecycleScope.launch(Dispatchers.IO) {
                lecturers = ArrayList(AppDatabase.getInstance(this@StatisticalLecturerActivity)?.lecturerDAO()?.getAllLecturerAndUserBySemester(selectedSemesterId) ?: emptyList())
                val lecturerNamesList = mutableListOf<String>().apply {
                    add("--- Select Lecturer ---")
                    for (lecturer in lecturers) {
                        lecturer.user.fullName?.let { add(it) }
                    }
                }

                // Show the selection dialog on the main thread
                withContext(Dispatchers.Main) {
                    showSelectionDialog("Select Lecturer", ArrayList(lecturerNamesList)) { dialog, which ->
                        if (which == 0) {
                            resetSelections(edtLecturer)
                        } else {
                            selectedLecturerId = lecturers[which - 1].lecturer.id
                            edtLecturer.setText(lecturerNamesList[which])
                            titleTable.visibility = View.VISIBLE
                            txtLecturerName.text = lecturerNamesList[which]
                            txtSemesterName.text = edtSemester.text.toString()
                            updateStatistical()
                        }
                    }
                }
            }
        }
    }

    private fun updateStatistical() {
        lifecycleScope.launch(Dispatchers.IO) {
            val statisticalOfLecturers = if (selectedSemesterId > 0 && selectedLecturerId > 0) {
                AppDatabase.getInstance(this@StatisticalLecturerActivity)
                    ?.statisticalDAO()?.getStatisticalOfLecturer(selectedSemesterId, selectedLecturerId) ?: emptyList()
            } else {
                emptyList<StatisticalOfLecturer>()
            }

            // Switch to the main thread to update the UI
            withContext(Dispatchers.Main) {
                rvLecturer.layoutManager = LinearLayoutManager(this@StatisticalLecturerActivity)
                rvLecturer.adapter = LecturerStatisticalRecycleViewAdapter(this@StatisticalLecturerActivity, ArrayList(statisticalOfLecturers))
            }
        }
    }


    private fun showSelectionDialog(title: String, options: List<String>, listener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setItems(options.toTypedArray(), listener)
        builder.show()
    }

    private fun resetSelections(edtSemester: EditText, edtLecturer: EditText) {
        titleTable.visibility = View.GONE
        selectedSemesterId = 0
        edtSemester.setText("")
        resetSelections(edtLecturer)
    }

    private fun resetSelections(edtLecturer: EditText) {
        titleTable.visibility = View.GONE
        selectedLecturerId = 0
        edtLecturer.setText("")
        lecturerNames = ArrayList()
        updateStatistical()
    }
}
