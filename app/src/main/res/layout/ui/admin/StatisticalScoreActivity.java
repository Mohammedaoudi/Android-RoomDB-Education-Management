package com.fh.app_student_management.ui.admin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fh.app_student_management.R;
import com.fh.app_student_management.data.AppDatabase;
import com.fh.app_student_management.data.entities.Semester;
import com.fh.app_student_management.data.relations.ClassWithRelations;
import com.fh.app_student_management.data.relations.ScoreDistribution;
import com.fh.app_student_management.data.relations.SubjectWithRelations;
import com.fh.app_student_management.utilities.Utils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class StatisticalScoreActivity extends AppCompatActivity {

    private ArrayList<Semester> semesters;
    private ArrayList<String> semesterNames;
    private long selectedSemesterId;
    private ArrayList<ClassWithRelations> classes;
    private ArrayList<String> classNames;
    private long selectedClassId;
    private ArrayList<SubjectWithRelations> subjects;
    private ArrayList<String> subjectNames;
    private long selectedSubjectId;
    private PieData pieData;
    private PieDataSet pieDataSet;
    private ArrayList<PieEntry> entries;

    private ImageView btnBack;
    private EditText edtSemester;
    private EditText edtClass;
    private EditText edtSubject;
    private LinearLayout titleChart;
    private TextView txtSemesterName;
    private TextView txtSubjectName;
    private PieChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_statistical_score);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initStatisticalScoreView();
        handleEventListener();
    }

    private void initStatisticalScoreView() {
        btnBack = findViewById(R.id.btnBack);
        edtSemester = findViewById(R.id.edtSemester);
        edtClass = findViewById(R.id.edtClass);
        edtSubject = findViewById(R.id.edtSubject);
        titleChart = findViewById(R.id.titleChart);
        txtSemesterName = findViewById(R.id.txtSemesterName);
        txtSubjectName = findViewById(R.id.txtSubjectName);
        chart = findViewById(R.id.chart);

        titleChart.setVisibility(View.GONE);

        semesters = new ArrayList<>(AppDatabase.getInstance(this).semesterDAO().getAll());
        semesterNames = new ArrayList<>(semesters.size() + 1);
        semesterNames.add(0, "--- Chọn học kỳ ---");
        for (int i = 0; i < semesters.size(); i++) {
            String startDate = Utils.formatDate("MM/yyyy").format(semesters.get(i).getStartDate());
            String endDate = Utils.formatDate("MM/yyyy").format(semesters.get(i).getEndDate());
            String semesterName = String.format("%s (%s - %s)", semesters.get(i).getName(), startDate, endDate);
            semesterNames.add(semesterName);
        }

        entries = new ArrayList<>();

        pieDataSet = new PieDataSet(entries, "Thống kê điểm số của sinh viên");
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setValueTextSize(14f);

        pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter(chart));
        pieData.setValueTextSize(14f);

        chart.setData(pieData);
        chart.getDescription().setEnabled(false);
        chart.setUsePercentValues(true);
        chart.animateY(100);
        chart.getLegend().setEnabled(false);
        chart.invalidate();
    }

    private void handleEventListener() {
        btnBack.setOnClickListener(v -> finish());

        edtSemester.setOnClickListener(v -> showSelectionDialog("Chọn học kỳ", semesterNames, (dialog, which) -> {
            if (which == 0) {
                resetSelections(edtSemester, edtClass, edtSubject);
            } else {
                selectedSemesterId = semesters.get(which - 1).getId();
                edtSemester.setText(semesterNames.get(which));
                resetSelections(edtSubject);
            }
        }));

        edtClass.setOnClickListener(v -> {
            if (edtSemester.getText().toString().isEmpty()) {
                Utils.showToast(this, "Chưa chọn học kỳ");
                return;
            }

            classes = new ArrayList<>(AppDatabase.getInstance(this).classDAO().getBySemester(selectedSemesterId));
            classNames = new ArrayList<>(classes.size() + 1);
            classNames.add(0, "--- Chọn lớp ---");
            for (int i = 0; i < classes.size(); i++) {
                classNames.add(classes.get(i).getClazz().getName());
            }

            showSelectionDialog("Chọn lớp", classNames, (dialog, which) -> {
                if (which == 0) {
                    resetSelections(edtClass, edtSubject);
                } else {
                    selectedClassId = classes.get(which - 1).getClazz().getId();
                    edtClass.setText(classNames.get(which));
                    resetSelections(edtSubject);
                }
            });
        });

        edtSubject.setOnClickListener(v -> {
            if (edtSemester.getText().toString().isEmpty()) {
                Utils.showToast(this, "Chưa chọn học kỳ");
                return;
            }

            if (edtClass.getText().toString().isEmpty()) {
                Utils.showToast(this, "Chưa chọn lớp");
                return;
            }

            subjects = new ArrayList<>(AppDatabase.getInstance(this)
                    .subjectDAO().getBySemesterClass(selectedSemesterId, selectedClassId));
            subjectNames = new ArrayList<>(subjects.size() + 1);
            subjectNames.add(0, "--- Chọn môn học ---");
            for (int i = 0; i < subjects.size(); i++) {
                String subjectName = String.format("%s - %s", subjects.get(i).getSubject().getName(), subjects.get(i).getClazz().getName());
                subjectNames.add(subjectName);
            }

            showSelectionDialog("Chọn môn học", subjectNames, (dialog, which) -> {
                if (which == 0) {
                    resetSelections(edtSubject);
                } else {
                    selectedSubjectId = subjects.get(which - 1).getSubject().getId();
                    edtSubject.setText(subjectNames.get(which));
                    titleChart.setVisibility(View.VISIBLE);
                    txtSemesterName.setText(edtSemester.getText().toString());
                    txtSubjectName.setText(subjectNames.get(which));

                    ScoreDistribution scoreDistribution = AppDatabase.getInstance(this)
                            .statisticalDAO().getStatisticalBySemesterSubject(selectedSemesterId, selectedSubjectId);

                    entries.clear();
                    if (scoreDistribution.getExcellent() > 0) {
                        entries.add(new PieEntry(scoreDistribution.getExcellent(), "Xuất sắc"));
                    }
                    if (scoreDistribution.getGood() > 0) {
                        entries.add(new PieEntry(scoreDistribution.getGood(), "Giỏi"));
                    }
                    if (scoreDistribution.getFair() > 0) {
                        entries.add(new PieEntry(scoreDistribution.getFair(), "Khá"));
                    }
                    if (scoreDistribution.getAverage() > 0) {
                        entries.add(new PieEntry(scoreDistribution.getAverage(), "Trung bình"));
                    }

                    updateChart(entries);
                }
            });
        });
    }

    private void updateChart(ArrayList<PieEntry> entries) {
        pieDataSet.setValues(entries);
        pieData.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    private void showSelectionDialog(String title, @NonNull List<String> options, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setItems(options.toArray(new CharSequence[0]), listener);
        builder.show();
    }

    private void resetSelections(@NonNull EditText edtSemester, EditText edtClass, EditText edtSubject) {
        titleChart.setVisibility(View.GONE);
        selectedSemesterId = 0;
        edtSemester.setText("");
        resetSelections(edtClass, edtSubject);
    }

    private void resetSelections(@NonNull EditText edtClass, EditText edtSubject) {
        titleChart.setVisibility(View.GONE);
        selectedClassId = 0;
        edtClass.setText("");
        resetSelections(edtSubject);
    }

    private void resetSelections(@NonNull EditText edtSubject) {
        titleChart.setVisibility(View.GONE);
        selectedSubjectId = 0;
        edtSubject.setText("");
        updateChart(new ArrayList<>());
    }
}