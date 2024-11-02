package ma.ensa.projet.ui.admin

import ma.ensa.projet.R
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.data.entities.Semester
import ma.ensa.projet.utilities.Utils
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
import ma.ensa.projet.adapters.admin.SubjectStatisticalRecycleViewAdapter
import ma.ensa.projet.data.dto.StatisticalOfSubject


class StatisticalSubjectActivity : AppCompatActivity() {

    private lateinit var semesters: ArrayList<Semester>
    private lateinit var semesterNames: ArrayList<String>
    private var selectedSemesterId: Long = 0
    private var selectedSemesterName: String? = null

    private lateinit var btnBack: ImageView
    private lateinit var edtSemester: EditText
    private lateinit var titleTable: LinearLayout
    private lateinit var txtSemesterName: TextView
    private lateinit var txtSubjectCount: TextView
    private lateinit var rvSubject: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_statistical_subject)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initStatisticalSubjectView()
        handleEventListener()
    }



    private fun initStatisticalSubjectView() {
        btnBack = findViewById(R.id.btnBack)
        edtSemester = findViewById(R.id.edtSemester)
        titleTable = findViewById(R.id.titleTable)
        txtSemesterName = findViewById(R.id.txtSemesterName)
        txtSubjectCount = findViewById(R.id.txtSubjectCount)
        rvSubject = findViewById(R.id.rvSubject)

        titleTable.visibility = View.GONE

        // Access database asynchronously
        lifecycleScope.launch {
            // Query database in the IO dispatcher (background thread)
            val semestersList = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(this@StatisticalSubjectActivity)?.semesterDAO()?.getAll()
            }

            // Continue on the main thread with the retrieved data
            semestersList?.let { semesters ->
                this@StatisticalSubjectActivity.semesters = ArrayList(semesters)
                semesterNames = ArrayList(semesters.size + 1)
                semesterNames.add(0, "--- Select Semester ---")

                for (semester in semesters) {
                    val startDate = Utils.formatDate("MM/yyyy").format(semester.startDate)
                    val endDate = Utils.formatDate("MM/yyyy").format(semester.endDate)
                    val semesterName = String.format("%s (%s - %s)", semester.name, startDate, endDate)
                    semesterNames.add(semesterName)
                }
            }
        }
    }

    private fun handleEventListener() {
        btnBack.setOnClickListener { finish() }

        edtSemester.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Select Semester")
                .setItems(semesterNames.toTypedArray()) { dialog, which ->
                    if (which == 0) {
                        selectedSemesterId = 0
                        edtSemester.setText("")
                        updateStatistical()
                    } else {
                        selectedSemesterId = semesters[which - 1].id
                        selectedSemesterName = semesterNames[which]
                        edtSemester.setText(semesterNames[which])
                        updateStatistical()
                    }
                }
                .show()
        }
    }

    private fun updateStatistical() {
        lifecycleScope.launch(Dispatchers.IO) {
            val statisticalOfSubjects: List<StatisticalOfSubject> = if (selectedSemesterId > 0) {
                AppDatabase.getInstance(this@StatisticalSubjectActivity)?.statisticalDAO()?.getStatisticalOfSubject(selectedSemesterId) ?: emptyList()
            } else {
                emptyList()
            }

            // Switch to the main thread to update the UI
            withContext(Dispatchers.Main) {
                rvSubject.layoutManager = LinearLayoutManager(this@StatisticalSubjectActivity)
                rvSubject.adapter = SubjectStatisticalRecycleViewAdapter(this@StatisticalSubjectActivity, ArrayList(statisticalOfSubjects))
                txtSemesterName.text = selectedSemesterName
                txtSubjectCount.text = statisticalOfSubjects.size.toString()
                titleTable.visibility = View.VISIBLE
            }
        }
    }}
