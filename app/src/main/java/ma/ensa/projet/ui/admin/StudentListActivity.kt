package ma.ensa.projet.ui.admin

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensa.projet.R
import ma.ensa.projet.adapters.admin.StudentListRecycleViewAdapter
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.data.dto.StudentWithRelations
import ma.ensa.projet.data.dto.SubjectWithRelations
import ma.ensa.projet.data.entities.AcademicYear
import ma.ensa.projet.data.entities.Classe
import ma.ensa.projet.data.entities.Major
import ma.ensa.projet.data.entities.Semester
import ma.ensa.projet.data.entities.Student
import ma.ensa.projet.data.entities.User
import ma.ensa.projet.utilities.Constants
import ma.ensa.projet.utilities.Utils
import ma.ensa.projet.utilities.Validator
import java.text.ParseException
import java.util.Calendar

class StudentListActivity : AppCompatActivity() {
    private var semesters: ArrayList<Semester>? = null
    private var semesterNames: ArrayList<String>? = null
    private var selectedSemesterId: Long = 0
    private var subjectNames: ArrayList<String>? = null
    private var selectedSubjectId: Long = 0
    private var majors: ArrayList<Major>? = null
    private var majorNames: ArrayList<String>? = null
    private var selectedMajorId: Long = 0
    private var classes: ArrayList<Classe>? = null
    private var classNames: ArrayList<String>? = null
    private var selectedClassId: Long = 0
    private var academicYears: ArrayList<AcademicYear>? = null
    private var academicYearNames: ArrayList<String>? = null
    private var selectedAcademicYearId: Long = 0
    private var studentListRecycleViewAdapter: StudentListRecycleViewAdapter? = null

    private var layoutStudent: RelativeLayout? = null
    private var btnBack: ImageView? = null
    private var searchViewStudent: SearchView? = null
    private var btnAddStudent: Button? = null
    private var btnFilter: ImageView? = null

    private var bottomSheetDialog: BottomSheetDialog? = null

    var tempImageBytes: ByteArray? = null
    var currentAvatarImageView: ImageView? = null
    var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_list_student)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById<View>(R.id.layoutStudent)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupSwipeToDelete()

        initAddStudentView()
        handleEventListener()
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // Move operation not needed, return false
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                // Show confirmation dialog
                AlertDialog.Builder(this@StudentListActivity).apply {
                    setTitle("Delete Confirmation")
                    setMessage("Are you sure you want to delete this student?")
                    setPositiveButton("Delete") { _, _ ->
                        // If confirmed, delete the student
                        studentListRecycleViewAdapter?.deleteStudent(position)
                    }
                    setNegativeButton("Cancel") { dialog, _ ->
                        // If canceled, restore the item
                        dialog.dismiss()
                        studentListRecycleViewAdapter?.notifyItemChanged(position)
                    }
                    setCancelable(false)
                    create()
                    show()
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    // Draw red background
                    val paint = Paint()
                    paint.color = Color.RED
                    val itemView = viewHolder.itemView
                    if (dX > 0) {
                        // Swiping to the right
                        c.drawRect(
                            itemView.left.toFloat(),
                            itemView.top.toFloat(),
                            itemView.left + dX,
                            itemView.bottom.toFloat(),
                            paint
                        )
                    } else {
                        // Swiping to the left
                        c.drawRect(
                            itemView.right + dX,
                            itemView.top.toFloat(),
                            itemView.right.toFloat(),
                            itemView.bottom.toFloat(),
                            paint
                        )
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        // Attach ItemTouchHelper to RecyclerView
        val rvStudent = findViewById<RecyclerView>(R.id.rvStudent)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvStudent)
    }

    private fun initAddStudentView() {
        layoutStudent = findViewById(R.id.layoutStudent)
        btnBack = findViewById(R.id.btnBack)
        searchViewStudent = findViewById(R.id.searchViewStudent)
        btnAddStudent = findViewById(R.id.btnAddStudent)
        btnFilter = findViewById(R.id.btnFilter)
        bottomSheetDialog = BottomSheetDialog(this)

        lifecycleScope.launch {
            // Fetch semesters asynchronously
            semesters = withContext(Dispatchers.IO) {
                ArrayList<Semester>(AppDatabase.getInstance(this@StudentListActivity)?.semesterDAO()?.getAll() ?: emptyList())
            }

            semesterNames = ArrayList<String>(semesters!!.size + 1).apply {
                for (semester in semesters!!) {
                    val startDate = Utils.formatDate("MM/yyyy").format(semester.startDate)
                    val endDate = Utils.formatDate("MM/yyyy").format(semester.endDate)
                    val semesterName = String.format("%s (%s - %s)", semester.name, startDate, endDate)
                    add(semesterName)
                }
            }

            // Fetch majors asynchronously
            majors = withContext(Dispatchers.IO) {
                ArrayList<Major>(AppDatabase.getInstance(this@StudentListActivity)?.majorDAO()?.getAll() ?: emptyList())
            }


            Log.d("DatabaseCheck", "All Majors: $majors")

            majorNames = ArrayList<String>(majors!!.size + 1).apply {
                for (major in majors!!) {
                    major.name?.let { add(it) }
                }
            }

            // Fetch classes asynchronously
            classes = withContext(Dispatchers.IO) {
                ArrayList<Classe>(AppDatabase.getInstance(this@StudentListActivity)?.classDAO()?.getAll() ?: emptyList())
            }

            classNames = ArrayList<String>(classes!!.size + 1).apply {
                for (clazz in classes!!) {
                    clazz.name?.let { add(it) }
                }
            }

            Log.d("DatabaseCheck", "All classes: $classes")
            // Fetch academic years asynchronously
            academicYears = withContext(Dispatchers.IO) {
                ArrayList<AcademicYear>(AppDatabase.getInstance(this@StudentListActivity)?.academicYearDAO()?.getAll() ?: emptyList())
            }

            academicYearNames = ArrayList<String>(academicYears!!.size + 1).apply {
                for (academicYear in academicYears!!) {
                    academicYear.name?.let { add(it) }
                }
            }

            // Fetch students asynchronously
            val students: ArrayList<StudentWithRelations> = withContext(Dispatchers.IO) {
                ArrayList<StudentWithRelations>(AppDatabase.getInstance(this@StudentListActivity)?.studentDAO()?.getAllWithRelations() ?: emptyList())
            }

            // Set up RecyclerView
            studentListRecycleViewAdapter = StudentListRecycleViewAdapter(this@StudentListActivity, students)
            val rvStudent = findViewById<RecyclerView>(R.id.rvStudent)
            rvStudent.layoutManager = LinearLayoutManager(this@StudentListActivity)
            rvStudent.adapter = studentListRecycleViewAdapter
        }
    }


    @SuppressLint("InflateParams")
    private fun handleEventListener() {
        layoutStudent!!.setOnClickListener { v: View ->
            if (v.id == R.id.layoutStudent) {
                searchViewStudent!!.clearFocus()
            }
        }

        btnBack!!.setOnClickListener { v: View? -> finish() }

        searchViewStudent!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                studentListRecycleViewAdapter?.getFilter()?.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                studentListRecycleViewAdapter?.getFilter()?.filter(newText)
                return false
            }
        })

        btnAddStudent!!.setOnClickListener { v: View? -> showAddStudentDialog() }

        btnFilter!!.setOnClickListener { v: View? -> showFilterStudentDialog() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 1000 && data.data != null) {
                val uri = data.data
                try {
                    // Get the bitmap from URI
                    val bitmap = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    } else {
                        val source = ImageDecoder.createSource(contentResolver, uri!!)
                        ImageDecoder.decodeBitmap(source)
                    }

                    // Update the UI
                    currentAvatarImageView?.setImageBitmap(bitmap)

                    // Store the image bytes
                    tempImageBytes = Utils.getBytesFromBitmap(bitmap)

                    if (isEditMode) {
                        // If in edit mode, update the adapter
                        studentListRecycleViewAdapter?.handleImageResult(bitmap, tempImageBytes!!)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Utils.showToast(this, "Failed to load image")
                }
            }
        }
    }


    private fun updateStudentList() {
        lifecycleScope.launch {
            val students: List<StudentWithRelations?>? = withContext(Dispatchers.IO) {
                when {
                    selectedMajorId > 0 && selectedClassId > 0 -> {
                        Log.d("UpdateStudentList", "Fetching students by Major ID: $selectedMajorId and Class ID: $selectedClassId")
                        AppDatabase.getInstance(this@StudentListActivity)?.studentDAO()
                            ?.getByMajorClass(selectedMajorId, selectedClassId)
                    }
                    selectedMajorId > 0 -> {
                        Log.d("UpdateStudentList", "Fetching students by Major ID: $selectedMajorId")
                        AppDatabase.getInstance(this@StudentListActivity)?.studentDAO()
                            ?.getByMajor(selectedMajorId)
                    }
                    selectedClassId > 0 -> {
                        Log.d("UpdateStudentList", "Fetching students by Class ID: $selectedClassId")
                        AppDatabase.getInstance(this@StudentListActivity)?.studentDAO()
                            ?.getByClass(selectedClassId)
                    }
                    else -> {
                        Log.d("UpdateStudentList", "Fetching all students")
                        AppDatabase.getInstance(this@StudentListActivity)?.studentDAO()?.getAllWithRelations()
                    }
                }
            }

            // Log the fetched students for debugging
            Log.d("UpdateStudentList", "Fetched Students: ${students?.size}")

            // Update the RecyclerView adapter with the filtered list of students
            if (students != null && students.isNotEmpty()) {
                val nonNullStudents = ArrayList(students.filterNotNull())
                Log.d("UpdateStudentList", "Non-null Students: ${nonNullStudents.size}")
                studentListRecycleViewAdapter?.setFilteredList(nonNullStudents)
            } else {
                Log.d("UpdateStudentList", "No students found after filtering")
                studentListRecycleViewAdapter?.resetFilteredList()
            }

            // Clear the search view and dismiss the dialog
            searchViewStudent?.setQuery("", false)
            searchViewStudent?.clearFocus()
            bottomSheetDialog?.dismiss()
        }
    }




    @SuppressLint("InflateParams")
    private fun showAddStudentDialog() {
        val view: View = LayoutInflater.from(this).inflate(R.layout.admin_bottom_sheet_add_student, null)
        bottomSheetDialog!!.setContentView(view)
        val behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.isDraggable = false

        // Initialize image-related variables
        isEditMode = false
        currentAvatarImageView = view.findViewById(R.id.avatar)
        tempImageBytes = null

        view.findViewById<View>(R.id.iconCamera).setOnClickListener { v: View? ->
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(1000) // Add request code 1000
        }

        view.findViewById<View>(R.id.edtDob).setOnClickListener { view: View ->
            this.showDatePickerDialog(view)
        }

        view.findViewById<View>(R.id.edtMajor).setOnClickListener { v: View? ->
            AlertDialog.Builder(this)
                .setTitle("Select Major")
                .setItems(majorNames!!.toTypedArray<CharSequence>()) { dialog: DialogInterface?, which: Int ->
                    selectedMajorId = majors!![which].id
                    (view.findViewById<View>(R.id.edtMajor) as EditText).setText(majorNames!![which])
                    updateClassesForMajor(selectedMajorId, view)
                }
                .show()
        }

        view.findViewById<View>(R.id.edtClass).setOnClickListener { v: View? ->
            // Check if a major is selected
            if (selectedMajorId <= 0) {
                Utils.showToast(this@StudentListActivity, "Please select a major first.")
                return@setOnClickListener // Exit the click listener if no major is selected
            }


            AlertDialog.Builder(this)
                .setTitle("Select Class")
                .setItems(classNames!!.toTypedArray<CharSequence>()) { dialog: DialogInterface?, which: Int ->

                    selectedClassId = classes!!.first { it.name == classNames!![which] }.id

                    (view.findViewById<View>(R.id.edtClass) as EditText).setText(classNames!![which])

                    CoroutineScope(Dispatchers.IO).launch {
                        val selectedClass = AppDatabase.getInstance(this@StudentListActivity)
                            ?.classDAO()?.getById(selectedClassId)

                        if (selectedClass != null) {
                            selectedAcademicYearId = selectedClass.academicYearId!!

                            val academicYearName = AppDatabase.getInstance(this@StudentListActivity)
                                ?.academicYearDAO()?.getById(selectedAcademicYearId)?.name

                            withContext(Dispatchers.Main) {
                                view.findViewById<EditText>(R.id.edtAcademicYear)?.setText(academicYearName ?: "N/A")
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Utils.showToast(this@StudentListActivity, "Failed to load class details.")
                            }
                        }
                    }
                }
                .show()
        }
        view.findViewById<View>(R.id.btnAddStudent).setOnClickListener { v: View? ->
            AlertDialog.Builder(this)
                .setTitle("Notification")
                .setMessage("Add a new student?")
                .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
                    performAddStudent(view)
                }
                .setNegativeButton("No", null)
                .show()
        }

        bottomSheetDialog!!.setOnDismissListener {
            tempImageBytes = null
            currentAvatarImageView = null
            isEditMode = false
        }

        bottomSheetDialog!!.show()
    }

    private fun updateClassesForMajor(selectedMajorId: Long, view: View) {
        // Filter classes based on the selected major
        val filteredClasses = classes!!.filter { it.majorId == selectedMajorId }

        // Create a new ArrayList from the filtered class names
        classNames = ArrayList(filteredClasses.map { it.name ?: "Unknown" }) // Use map to extract names

        // Update the class EditText view
        (view.findViewById<View>(R.id.edtClass) as EditText).setText("") // Clear selection
    }

    private fun performAddStudent(view: View) {
        if (!validateInputs(view)) return

        CoroutineScope(Dispatchers.IO).launch {
            try {

                Log.d("AddStudent", "Selected Major ID: $selectedMajorId")
                Log.d("AddStudent", "Selected  addd Class ID: $selectedClassId")
                Log.d("AddStudent", "Selected Academic Year ID: $selectedAcademicYearId")

                // Check if the major, class, and academic year IDs exist in the database
                val major = AppDatabase.getInstance(this@StudentListActivity)?.majorDAO()?.getById(selectedMajorId)
                val clazz = AppDatabase.getInstance(this@StudentListActivity)?.classDAO()?.getById(selectedClassId)
                if (clazz != null) {
                    Log.d("AddStudent", "Selected  name Class : ${clazz.name}")
                }

                val academicYear = AppDatabase.getInstance(this@StudentListActivity)?.academicYearDAO()?.getById(selectedAcademicYearId)

                // If any related entity is missing, stop and show an error
                if (major == null) {
                    withContext(Dispatchers.Main) {
                        Utils.showToast(this@StudentListActivity, "Selected major does not exist.")
                    }
                    return@launch
                }
                if (clazz == null) {
                    withContext(Dispatchers.Main) {
                        Utils.showToast(this@StudentListActivity, "Selected class does not exist.")
                    }
                    return@launch
                }
                if (academicYear == null) {
                    withContext(Dispatchers.Main) {
                        Utils.showToast(this@StudentListActivity, "Selected academic year does not exist.")
                    }
                    return@launch
                }

                // Create the User instance
                val user = User(
                    avatar = tempImageBytes ?: Utils.getBytesFromBitmap(Utils.getBitmapFromView(view.findViewById(R.id.avatar))),
                    email = view.findViewById<EditText>(R.id.edtEmail).text.toString(),
                    password = Utils.hashPassword("123456"),
                    fullName = view.findViewById<EditText>(R.id.edtFullName).text.toString(),
                    dob = Utils.formatDate("dd/MM/YYYY").parse(view.findViewById<EditText>(R.id.edtDob).text.toString()),
                    address = view.findViewById<EditText>(R.id.edtAddress).text.toString(),
                    role = Constants.Role.STUDENT,
                    gender = if (view.findViewById<RadioGroup>(R.id.radioGroupGender).checkedRadioButtonId == R.id.radioButtonMale)
                        Constants.MALE else Constants.FEMALE,
                    id = 0
                )

                // Check if the major, class, and academic year IDs exist in the database



                // Create the Student instance with foreign key IDs
                val student = Student(
                    userId = user.id,
                    majorId = selectedMajorId,
                    classId = selectedClassId,
                    academicYearId = selectedAcademicYearId
                )

                // Create StudentWithRelations object with the fully populated data
                val studentWithRelations = StudentWithRelations(
                    user = user,
                    student = student,
                    major = major,
                    clazz = clazz,
                    academicYear = academicYear
                )

                // Insert the Student into the database

                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    studentListRecycleViewAdapter?.addStudent(studentWithRelations)
                    bottomSheetDialog?.dismiss()

                    // Reset image-related variables
                    tempImageBytes = null
                    currentAvatarImageView = null
                }

            } catch (e: ParseException) {
                Log.e("AddStudent", "Error parsing date", e)
                withContext(Dispatchers.Main) {
                    Utils.showToast(this@StudentListActivity, "Error parsing date")
                }

            } catch (e: SQLiteConstraintException) {
                Log.e("AddStudent", "Foreign key constraint violation", e)
                withContext(Dispatchers.Main) {
                    Utils.showToast(this@StudentListActivity, "Failed to add student due to a constraint violation.")
                }

            } catch (e: Exception) {
                Log.e("AddStudent", "Error adding student", e)
                withContext(Dispatchers.Main) {
                    Utils.showToast(this@StudentListActivity, "Error adding student")
                }
            }
        }
    }




    private fun showDatePickerDialog(view: View) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { v: DatePicker?, year: Int, month: Int, day: Int ->
                val date = day.toString() + "/" + (month + 1) + "/" + year
                (view as TextView).text = date
            }, calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH]
        ).show()
    }

    private fun validateInputs(view: View): Boolean {
        return (validateNotEmpty(view, R.id.edtEmail, "Email cannot be empty")
                && validateEmail(view, R.id.edtEmail)
                && validateNotEmpty(view, R.id.edtFullName, "Full name cannot be empty")
                && validateNotEmpty(view, R.id.edtDob, "Date of birth cannot be empty")
                && validateNotEmpty(view, R.id.edtAddress, "Address cannot be empty")
                && validateNotEmpty(view, R.id.edtMajor, "Major cannot be empty")
                && validateNotEmpty(view, R.id.edtClass, "Class cannot be empty")
                && validateNotEmpty(view, R.id.edtAcademicYear, "Academic year cannot be empty"))
    }


    private fun validateNotEmpty(view: View, viewId: Int, errorMessage: String): Boolean {
        val editText = view.findViewById<EditText>(viewId)
        if (editText == null || editText.text.toString().trim { it <= ' ' }.isEmpty()) {
            Utils.showToast(this, errorMessage)
            return false
        }
        return true
    }

    private fun validateEmail(view: View, viewId: Int): Boolean {
        val editText = view.findViewById<EditText>(viewId)
        if (editText != null && !Validator.isValidEmail(editText.text.toString())) {
            Utils.showToast(this, "Invalid email")
            return false
        }
        return true
    }

    @SuppressLint("InflateParams")
    private fun showFilterStudentDialog() {
        val view: View = LayoutInflater.from(this).inflate(R.layout.admin_bottom_sheet_filter_student, null)
        bottomSheetDialog!!.setContentView(view)

        // Setup Bottom Sheet
        val behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.isDraggable = false
        bottomSheetDialog!!.show()

        // Get references to EditText fields
        val edtMajor = view.findViewById<EditText>(R.id.edtMajor)
        val edtClass = view.findViewById<EditText>(R.id.edtClass)

        // Load all majors immediately

        // Initially disable the class selection
        edtClass.isEnabled = false

        // Major selection
        edtMajor.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val majorsList = withContext(Dispatchers.IO) {
                        AppDatabase.getInstance(this@StudentListActivity)?.majorDAO()
                            ?.getAll() // Fetching all majors
                    }
                    majors = majorsList?.let { ArrayList(it) } ?: ArrayList()
                    majorNames = majors!!.mapNotNull { it.name } as ArrayList<String>

                    // Show selection dialog for majors
                    showSelectionDialog("Select Major", majorNames!!) { dialog, which ->
                        selectedMajorId = majors!![which].id
                        edtMajor.setText(majorNames!![which])
                        edtClass.isEnabled = true // Enable class selection once a major is selected
                        // Optionally, load classes for the selected major here
                        loadClasses(selectedMajorId) // Load classes based on the selected major
                    }
                } catch (e: Exception) {
                    Log.e("FilterDialog", "Error fetching majors: ${e.message}")
                }
            }
        }


        // Class selection
        edtClass.setOnClickListener {
            if (selectedMajorId == 0L) {
                Utils.showToast(this, "Please select a Major first.")
                return@setOnClickListener
            }
            showSelectionDialog("Select Class", classNames!!) { dialog, which ->
                if (which in classNames!!.indices) {
                    selectedClassId = classes!![which].id
                    edtClass.setText(classNames!![which])
                }
            }
        }

        // Confirm button
        view.findViewById<View>(R.id.btnConfirm).setOnClickListener {
            updateStudentList()
            edtMajor.setText("") // Clear major selection
            edtClass.setText("")
        }
    }


    private fun loadClasses(majorId: Long) {
        lifecycleScope.launch {
            try {
                val classesList = withContext(Dispatchers.IO) {
                    AppDatabase.getInstance(this@StudentListActivity)?.classDAO()?.getByMajor(majorId)

                }
                classes = classesList?.map { it.clazz }?.let { ArrayList(it) }
                classNames = (classes?.mapNotNull { it.name } ?: emptyList()) as ArrayList<String>?
                Log.d("FilterDialog", "fetching classes: ${classNames} + id :  ${majorId}")

            } catch (e: Exception) {
                Log.e("FilterDialog", "Error fetching classes: ${e.message}")
            }
        }
    }



    private fun showSelectionDialog(
        title: String,
        options: List<String>,
        listener: DialogInterface.OnClickListener
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setItems(options.toTypedArray<CharSequence>(), listener)
        builder.show()
    }



    private fun resetSelections(edtSubject: EditText) {
        edtSubject.setText("")
        selectedSubjectId = 0
        subjectNames = null
    }
}