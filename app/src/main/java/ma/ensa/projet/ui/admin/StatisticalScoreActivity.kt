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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensa.projet.R
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.data.dto.ClassWithRelations
import ma.ensa.projet.data.dto.SubjectWithRelations
import ma.ensa.projet.data.entities.Semester
import ma.ensa.projet.utilities.Utils


class StatisticalScoreActivity : AppCompatActivity() {

    private lateinit var semesters: ArrayList<Semester>
    private lateinit var semesterNames: ArrayList<String>
    private var selectedSemesterId: Long = 0
    private lateinit var classes: ArrayList<ClassWithRelations>
    private lateinit var classNames: ArrayList<String>
    private var selectedClassId: Long = 0
    private lateinit var subjects: ArrayList<SubjectWithRelations>
    private lateinit var subjectNames: ArrayList<String>
    private var selectedSubjectId: Long = 0
    private lateinit var pieData: PieData
    private lateinit var pieDataSet: PieDataSet
    private lateinit var entries: ArrayList<PieEntry>

    private lateinit var btnBack: ImageView
    private lateinit var edtSemester: EditText
    private lateinit var edtClass: EditText
    private lateinit var edtSubject: EditText
    private lateinit var titleChart: LinearLayout
    private lateinit var txtSemesterName: TextView
    private lateinit var txtSubjectName: TextView
    private lateinit var chart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_statistical_score)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initStatisticalScoreView()
        handleEventListener()
    }

    private fun initStatisticalScoreView() {
        btnBack = findViewById(R.id.btnBack)
        edtSemester = findViewById(R.id.edtSemester)
        edtClass = findViewById(R.id.edtClass)
        edtSubject = findViewById(R.id.edtSubject)
        titleChart = findViewById(R.id.titleChart)
        txtSemesterName = findViewById(R.id.txtSemesterName)
        txtSubjectName = findViewById(R.id.txtSubjectName)
        chart = findViewById(R.id.chart)

        titleChart.visibility = View.GONE

        // Access database asynchronously
        lifecycleScope.launch(Dispatchers.IO) {
            val semestersList = AppDatabase.getInstance(this@StatisticalScoreActivity)?.semesterDAO()?.getAll() ?: emptyList()
            semesters = ArrayList(semestersList)

            // Switch to the main thread to update the UI
            withContext(Dispatchers.Main) {
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

        entries = ArrayList()

        pieDataSet = PieDataSet(entries, "Student Score Statistics")
        pieDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        pieDataSet.valueTextSize = 14f

        pieData = PieData(pieDataSet)
        pieData.setValueFormatter(PercentFormatter(chart))
        pieData.setValueTextSize(14f)

        chart.data = pieData
        chart.description.isEnabled = false
        chart.setUsePercentValues(true)
        chart.animateY(100)
        chart.legend.isEnabled = false
        chart.invalidate()
    }

    private fun handleEventListener() {
        btnBack.setOnClickListener { finish() }

        edtSemester.setOnClickListener {
            showSelectionDialog("Select Semester", semesterNames) { dialog, which ->
                if (which == 0) {
                    resetSelections(edtSemester, edtClass, edtSubject)
                } else {
                    selectedSemesterId = semesters[which - 1].id
                    edtSemester.setText(semesterNames[which])
                    resetSelections(edtSubject)
                }
            }
        }

        edtClass.setOnClickListener {
            if (edtSemester.text.toString().isEmpty()) {
                Utils.showToast(this, "Semester not selected")
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                classes = ArrayList(AppDatabase.getInstance(this@StatisticalScoreActivity)?.classDAO()?.getBySemester(selectedSemesterId) ?: emptyList())
                classNames = ArrayList(classes.size + 1)
                classNames.add(0, "--- Select Class ---")
                for (clazz in classes) {
                    clazz.clazz.name?.let { classNames.add(it) }
                }

                // Switch back to the main thread to show the dialog
                withContext(Dispatchers.Main) {
                    showSelectionDialog("Select Class", classNames) { dialog, which ->
                        if (which == 0) {
                            resetSelections(edtClass, edtSubject)
                        } else {
                            selectedClassId = classes[which - 1].clazz.id
                            edtClass.setText(classNames[which])
                            resetSelections(edtSubject)
                        }
                    }
                }
            }
        }

        edtSubject.setOnClickListener {
            if (edtSemester.text.toString().isEmpty()) {
                Utils.showToast(this, "Semester not selected")
                return@setOnClickListener
            }

            if (edtClass.text.toString().isEmpty()) {
                Utils.showToast(this, "Class not selected")
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                subjects = ArrayList(AppDatabase.getInstance(this@StatisticalScoreActivity)?.subjectDAO()?.getBySemesterClass(selectedSemesterId, selectedClassId) ?: emptyList())
                subjectNames = ArrayList(subjects.size + 1)
                subjectNames.add(0, "--- Select Subject ---")
                for (subject in subjects) {
                    val subjectName = String.format("%s - %s", subject.subject.name, subject.clazz.name)
                    subjectNames.add(subjectName)
                }

                // Switch back to the main thread to show the dialog
                withContext(Dispatchers.Main) {
                    showSelectionDialog("Select Subject", subjectNames) { dialog, which ->
                        if (which == 0) {
                            resetSelections(edtSubject)
                        } else {
                            selectedSubjectId = subjects[which - 1].subject.id
                            edtSubject.setText(subjectNames[which])
                            titleChart.visibility = View.VISIBLE
                            txtSemesterName.text = edtSemester.text.toString()
                            txtSubjectName.text = subjectNames[which]

                            // Fetch score distribution asynchronously
                            lifecycleScope.launch(Dispatchers.IO) {
                                val scoreDistribution = AppDatabase.getInstance(this@StatisticalScoreActivity)
                                    ?.statisticalDAO()
                                    ?.getStatisticalBySemesterSubject(selectedSemesterId, selectedSubjectId)

                                // Update entries based on score distribution
                                entries.clear()
                                scoreDistribution?.let {
                                    if (it.excellent > 0) entries.add(PieEntry(it.excellent.toFloat(), "Excellent"))
                                    if (it.good > 0) entries.add(PieEntry(it.good.toFloat(), "Good"))
                                    if (it.fair > 0) entries.add(PieEntry(it.fair.toFloat(), "Fair"))
                                    if (it.average > 0) entries.add(PieEntry(it.average.toFloat(), "Average"))
                                }

                                // Update chart on the main thread
                                withContext(Dispatchers.Main) {
                                    updateChart(entries)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateChart(entries: ArrayList<PieEntry>) {
        pieDataSet.values = entries
        pieData.notifyDataChanged()
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    private fun showSelectionDialog(title: String, options: List<String>, listener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setItems(options.toTypedArray(), listener)
        builder.show()
    }

    private fun resetSelections(edtSemester: EditText, edtClass: EditText, edtSubject: EditText) {
        titleChart.visibility = View.GONE
        selectedSemesterId = 0
        edtSemester.setText("")
        resetSelections(edtClass, edtSubject)
    }

    private fun resetSelections(edtClass: EditText, edtSubject: EditText) {
        titleChart.visibility = View.GONE
        selectedClassId = 0
        edtClass.setText("")
        resetSelections(edtSubject)
    }

    private fun resetSelections(edtSubject: EditText) {
        titleChart.visibility = View.GONE
        selectedSubjectId = 0
        edtSubject.setText("")
        updateChart(ArrayList())
    }
}
