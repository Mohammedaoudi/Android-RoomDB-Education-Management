package ma.ensa.projet.ui.admin

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensa.projet.R
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.data.dao.MajorDAO
import ma.ensa.projet.data.dao.LecturerDAO
import ma.ensa.projet.data.dao.StudentDAO

class StatisticalActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var barChart: BarChart
    private lateinit var studentDAO: StudentDAO
    private lateinit var majorDAO: MajorDAO
    private lateinit var lecturerDAO: LecturerDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_statistical)

        btnBack = findViewById(R.id.btnBack)
        barChart = findViewById(R.id.barChart)

        btnBack.setOnClickListener { finish() }

        // Initialize DAOs
        studentDAO = AppDatabase.getInstance(this).studentDAO()
        majorDAO = AppDatabase.getInstance(this).majorDAO()
        lecturerDAO = AppDatabase.getInstance(this).lecturerDAO()

        // Load data for the chart
        loadChartData()
    }

    private fun loadChartData() {
        lifecycleScope.launch {
            try {
                // Fetch all students on a background thread
                val students = withContext(Dispatchers.IO) {
                    studentDAO.getAll() // Ensure this method returns a list of StudentWithRelations
                }

                // Fetch distinct majors and lecturers
                val majorCount = withContext(Dispatchers.IO) {
                    majorDAO.getAll().size // Assuming getAll() returns a list of majors
                }

                val lecturerCount = withContext(Dispatchers.IO) {
                    lecturerDAO.getAll().size // Assuming getAll() returns a list of lecturers
                }

                val studentCount = students.size

                // Prepare entries for the chart
                val entries = arrayListOf<BarEntry>(
                    BarEntry(0f, studentCount.toFloat()), // Students
                    BarEntry(1f, majorCount.toFloat()),   // Majors
                    BarEntry(2f, lecturerCount.toFloat()) // Lecturers
                )

                // Create dataset and configure it with different colors
                val dataSet = BarDataSet(entries, "Statistics").apply {
                    colors = listOf(
                        resources.getColor(R.color.blue),  // Color for Students
                        resources.getColor(R.color.green), // Color for Majors
                        resources.getColor(R.color.red)    // Color for Lecturers
                    )
                    valueFormatter = object : ValueFormatter() {
                        override fun getBarLabel(barEntry: BarEntry?): String {
                            return when (barEntry?.x?.toInt()) {
                                0 -> "Students: $studentCount"
                                1 -> "Majors: $majorCount"
                                2 -> "Lecturers: $lecturerCount"
                                else -> ""
                            }
                        }
                    }
                }

                // Update the chart on the main thread
                val barData = BarData(dataSet)
                barChart.data = barData
                barChart.invalidate() // Refresh chart

            } catch (e: Exception) {
                // Handle exceptions (e.g., database access issues)
                Toast.makeText(this@StatisticalActivity, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
