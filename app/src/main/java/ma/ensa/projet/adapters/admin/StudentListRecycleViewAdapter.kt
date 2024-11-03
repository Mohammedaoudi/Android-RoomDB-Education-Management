package ma.ensa.projet.adapters.admin

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.database.sqlite.SQLiteConstraintException
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensa.projet.R
import ma.ensa.projet.adapters.admin.listener.ItemClickListener
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.data.dto.StudentWithRelations
import ma.ensa.projet.data.entities.AcademicYear
import ma.ensa.projet.data.entities.Classe
import ma.ensa.projet.data.entities.Major
import ma.ensa.projet.ui.admin.StudentListActivity
import ma.ensa.projet.utilities.Utils
import ma.ensa.projet.utilities.Constants
import java.text.ParseException
import java.util.Calendar
import java.util.Locale
import java.util.ArrayList

class StudentListRecycleViewAdapter(
    private val context: Context,
    originalList: ArrayList<StudentWithRelations>
) : RecyclerView.Adapter<StudentListRecycleViewAdapter.StudentViewHolder>(), Filterable {

    private val originalList: ArrayList<StudentWithRelations> = originalList


    private val bottomSheetDialog: BottomSheetDialog
    private lateinit var majors: ArrayList<Major>
    private lateinit var majorNames: ArrayList<String>
    private lateinit var classes: ArrayList<Classe>
    private lateinit var classNames: ArrayList<String>
    private lateinit var academicYears: ArrayList<AcademicYear>
    private lateinit var academicYearNames: ArrayList<String>


    private var currentFilterText = ""
    private var selectedMajor: Major? = null
    private var selectedClass: Classe? = null
    private var selectedAcademicYear: AcademicYear? = null
    private var filteredList: ArrayList<StudentWithRelations> = originalList
    init {
        bottomSheetDialog = BottomSheetDialog(context)

        // Load data asynchronously
        CoroutineScope(Dispatchers.Main).launch {
            // Load majors asynchronously
            val majors: List<Major> = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(context)?.majorDAO()?.getAll() ?: emptyList()
            }

            val majorNames = ArrayList<String>().apply {
                majors.forEach { it.name?.let { name -> add(name) } }
            }

            // Load classes asynchronously
            val classes: List<Classe> = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(context)?.classDAO()?.getAll() ?: emptyList()
            }

            val classNames = ArrayList<String>().apply {
                classes.forEach { it.name?.let { name -> add(name) } }
            }

            // Load academic years asynchronously
            val academicYears: List<AcademicYear> = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(context)?.academicYearDAO()?.getAll() ?: emptyList()
            }

            val academicYearNames = ArrayList<String>().apply {
                academicYears.forEach { it.name?.let { name -> add(name) } }
            }

            // You can now use majorNames, classNames, and academicYearNames as needed in your adapter
            // For example, update your UI or notify changes to the RecyclerView here
            notifyDataSetChanged() // Call this if data has changed
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun setFilteredList(list: ArrayList<StudentWithRelations>) {
        this.filteredList.clear()
        this.filteredList.addAll(list)
        notifyDataSetChanged()
    }

    fun resetFilteredList() {
        setFilteredList(originalList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.z_layout_recycle_view_student, parent, false)
        return StudentViewHolder(view)
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: StudentViewHolder, position1: Int) {
        val studentWithRelations: StudentWithRelations = filteredList[position1]
        holder.txtStudentName.text = studentWithRelations.user.fullName // Direct access

        holder.setItemClickListener { view, position1, isLongClick ->
            showEditStudentDialog(studentWithRelations)
        }
    }

    fun deleteStudent(position: Int) {
        // Launch a coroutine to perform the deletion asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            val studentWithRelations: StudentWithRelations = filteredList[position]

            // Directly access the student and user properties and perform the deletions
            val studentDAO = AppDatabase.getInstance(context)?.studentDAO()
            val userDAO = AppDatabase.getInstance(context)?.userDAO()

            studentDAO?.delete(studentWithRelations.student)
            userDAO?.delete(studentWithRelations.user)

            // Update the original and filtered lists on the main thread
            withContext(Dispatchers.Main) {
                originalList.remove(studentWithRelations)
                filteredList.remove(studentWithRelations)
                filter.filter(currentFilterText)
                notifyItemRemoved(position)
            }
        }
    }

    override fun getItemCount(): Int = filteredList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                currentFilterText = charSequence.toString()
                val query: String = Utils.removeVietnameseAccents(
                    charSequence.toString().lowercase(Locale.getDefault())
                ).toString()

                filteredList = if (query.isEmpty()) {
                    originalList
                } else {
                    originalList.filter { studentWithRelations ->
                        val studentName: String = Utils.removeVietnameseAccents(
                            studentWithRelations.user.fullName?.lowercase(Locale.getDefault()) // Access property directly
                        ).toString()
                        studentName.contains(query)
                    } as ArrayList<StudentWithRelations>
                }

                return FilterResults().apply { values = filteredList }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filteredList = filterResults.values as ArrayList<StudentWithRelations>
                notifyDataSetChanged()
            }
        }
    }

    fun addStudent(studentWithRelations: StudentWithRelations) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check for existing email
                val existingUser = studentWithRelations.user.email?.let {
                    AppDatabase.getInstance(context)?.userDAO()?.getByEmail(it.trim())
                }

                if (existingUser != null) {
                    // Show toast on the main thread if the email already exists
                    withContext(Dispatchers.Main) {
                        Utils.showToast(context, "Email already exists!")
                    }
                    return@launch // Exit early if email exists
                }

                // Insert user and obtain userId
                val userId = AppDatabase.getInstance(context)?.userDAO()?.insert(studentWithRelations.user)
                    ?: throw IllegalStateException("User insertion failed.")

                // Set the userId in the student entity
                studentWithRelations.student.userId = userId

                // Insert student record
                AppDatabase.getInstance(context)?.studentDAO()?.insert(studentWithRelations.student)

                // Update the UI and show success message on the main thread
                withContext(Dispatchers.Main) {
                    originalList.add(0, studentWithRelations) // Add the new student at the beginning
                    notifyItemInserted(0) // Notify the adapter that a new item was inserted
                    notifyDataSetChanged() // Optionally refresh the entire list
                    Utils.showToast(context, "Successfully added!")
                }

            } catch (ex: SQLiteConstraintException) {
                // Handle constraint violation errors
                withContext(Dispatchers.Main) {
                    Utils.showToast(context, "Failed to add student due to a constraint violation.")
                }
            } catch (ex: Exception) {
                // Handle any other unexpected errors
                withContext(Dispatchers.Main) {
                    Utils.showToast(context, "An unexpected error occurred while adding the student.")
                }
            }
        }
    }







    private suspend fun fetchNames() {
        withContext(Dispatchers.IO) {
            majors = ArrayList(AppDatabase.getInstance(context)?.majorDAO()?.getAll() ?: emptyList())
            classes = ArrayList(AppDatabase.getInstance(context)?.classDAO()?.getAll() ?: emptyList())
            academicYears = ArrayList(AppDatabase.getInstance(context)?.academicYearDAO()?.getAll() ?: emptyList())

            majorNames = ArrayList(majors.mapNotNull { it.name })
            classNames = ArrayList(classes.mapNotNull { it.name })
            academicYearNames = ArrayList(academicYears.mapNotNull { it.name })
        }
    }


    @SuppressLint("InflateParams")
    private fun showEditStudentDialog(studentWithRelations: StudentWithRelations) {
        // Launch a coroutine to fetch names
        CoroutineScope(Dispatchers.Main).launch {
            fetchNames() // Fetch names before proceeding

            val view: View = LayoutInflater.from(context)
                .inflate(R.layout.admin_bottom_sheet_edit_student, null)
            bottomSheetDialog.setContentView(view)
            val behavior = BottomSheetBehavior.from(view.parent as View)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isDraggable = false
            bottomSheetDialog.show()

            selectedMajor = studentWithRelations.major
            selectedClass = studentWithRelations.clazz
            selectedAcademicYear = studentWithRelations.academicYear

            // Initialize views
            val iconCamera = view.findViewById<ImageView>(R.id.iconCamera)
            val avatarImageView = view.findViewById<ImageView>(R.id.avatar)
            val edtEmail = view.findViewById<EditText>(R.id.edtEmail)
            val edtFullName = view.findViewById<EditText>(R.id.edtFullName)
            val radioGroupGender = view.findViewById<RadioGroup>(R.id.radioGroupGender)
            val edtDob = view.findViewById<EditText>(R.id.edtDob)
            val edtAddress = view.findViewById<EditText>(R.id.edtAddress)
            val edtMajor = view.findViewById<EditText>(R.id.edtMajor)
            val edtClass = view.findViewById<EditText>(R.id.edtClass)
            val edtAcademicYear = view.findViewById<EditText>(R.id.edtAcademicYear)
            val btnEdit = view.findViewById<Button>(R.id.btnEditStudent)
            val btnDelete = view.findViewById<Button>(R.id.btnDeleteStudent)

            // Set up image handling
            (context as? StudentListActivity)?.apply {
                isEditMode = true
                currentAvatarImageView = avatarImageView
                tempImageBytes = studentWithRelations.user.avatar
            }

            // Set the avatar image
            studentWithRelations.user.avatar?.let {
                avatarImageView.setImageBitmap(Utils.getBitmapFromBytes(it))
            }

            // Set other fields
            edtEmail.setText(studentWithRelations.user.email)
            edtFullName.setText(studentWithRelations.user.fullName)
            radioGroupGender.check(if (studentWithRelations.user.gender == Constants.MALE) {
                R.id.radioButtonMale
            } else {
                R.id.radioButtonFemale
            })
            edtDob.setText(Utils.formatDate("dd/MM/yyyy").format(studentWithRelations.user.dob))
            edtAddress.setText(studentWithRelations.user.address)

            Log.d("EditStudentDialogCheck", "Selected Major: ${selectedMajor?.name}")
            Log.d("EditStudentDialogCheck", "Selected Class: ${selectedClass?.name}")
            Log.d("EditStudentDialogCheck", "Selected Academic Year: ${selectedAcademicYear?.name}")

            edtMajor.setText(selectedMajor?.name ?: "")
            edtClass.setText(selectedClass?.name ?: "")
            edtAcademicYear.setText(selectedAcademicYear?.name ?: "")

            // Date picker setup
            edtDob.setOnClickListener {
                val calendar: Calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                DatePickerDialog(context, { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                    edtDob.setText(String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear))
                }, year, month, day).show()
            }

            // Major selection
            edtMajor.setOnClickListener {
                AlertDialog.Builder(context)
                    .setItems(majorNames.toTypedArray()) { _, which ->
                        selectedMajor = majors[which]
                        edtMajor.setText(selectedMajor?.name)

                        // Automatically set the academic year based on the selected major

                        // Update classes based on the selected major
                        updateClassesForMajor(selectedMajor?.id ?: 0, view)
                    }
                    .show()
            }

            iconCamera.setOnClickListener {
                (context as? StudentListActivity)?.let { activity ->
                    ImagePicker.with(activity)
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start(1000)
                }
            }


            edtClass.setOnClickListener {
                AlertDialog.Builder(context)
                    .setItems(classNames.toTypedArray()) { _, which ->
                        // Set selectedClass to the Classe object at the selected index
                        selectedClass = filteredClasses[which]
                        edtClass.setText(selectedClass?.name)

                        // Set the academic year based on the selected class's academicYearId
                        selectedAcademicYear =
                            academicYears.find { it.id == selectedClass?.academicYearId }
                        edtAcademicYear.setText(selectedAcademicYear?.name)

                        selectedClass?.name?.let { Log.d("Selected Class Name", it) }
                    }
                    .show()
            }

            btnEdit.setOnClickListener {
                if (edtFullName.text.isEmpty() || edtEmail.text.isEmpty()) {
                    Utils.showToast(context, "Please enter full name and email!")
                    return@setOnClickListener
                }

                // Update studentWithRelations properties
                studentWithRelations.user.apply {
                    fullName = edtFullName.text.toString().trim()
                    email = edtEmail.text.toString().trim()
                    gender = if (radioGroupGender.checkedRadioButtonId == R.id.radioButtonMale) {
                        Constants.MALE
                    } else {
                        Constants.FEMALE
                    }
                    try {
                        dob = Utils.formatDate("dd/MM/yyyy").parse(edtDob.text.toString().trim())
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                    address = edtAddress.text.toString().trim()
                    // Use the temporary image bytes from the activity
                    avatar = (context as? StudentListActivity)?.tempImageBytes ?: avatar
                }


                studentWithRelations.student.majorId = selectedMajor?.id
                studentWithRelations.student.classId = selectedClass?.id
                studentWithRelations.student.academicYearId = selectedAcademicYear?.id

                studentWithRelations.clazz = selectedClass
                studentWithRelations.major = selectedMajor as Major

                Log.d("hnnnanana",selectedMajor?.name + " fffff"+ selectedClass?.name )
                // Find the index and update directly in filteredList
                val index = filteredList.indexOfFirst { it.student.id == studentWithRelations.student.id }
                if (index != -1) {
                    filteredList[index] = studentWithRelations  // Update the specific student object
                    editStudent(index)
                } else {
                    Utils.showToast(context, "Student not found in the list!")
                }

                // Reset temporary variables
                (context as? StudentListActivity)?.apply {
                    tempImageBytes = null
                    currentAvatarImageView = null
                    isEditMode = false
                }

                bottomSheetDialog.dismiss()
            }

            // Delete button
            btnDelete.setOnClickListener {
                deleteStudent(filteredList.indexOf(studentWithRelations))
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.setOnDismissListener {
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                // Reset temporary variables on dismiss
                (context as? StudentListActivity)?.apply {
                    tempImageBytes = null
                    currentAvatarImageView = null
                    isEditMode = false
                }
            }
        }
    }

    private fun editStudent(position: Int) {
        // Use a coroutine scope to perform the database operations off the main thread
        CoroutineScope(Dispatchers.IO).launch {
            val studentWithRelations: StudentWithRelations = filteredList[position]

            // Update student and user properties
            AppDatabase.getInstance(context)?.studentDAO()?.update(studentWithRelations.student)
            AppDatabase.getInstance(context)?.userDAO()?.update(studentWithRelations.user)

            // After updating the database, return to the main thread to update the UI
            withContext(Dispatchers.Main) {
                filter.filter(currentFilterText)
                notifyItemChanged(position)
            }
        }
    }


    private lateinit var filteredClasses: List<Classe>

    private fun updateClassesForMajor(selectedMajorId: Long, view: View) {
        CoroutineScope(Dispatchers.Main).launch {
            // Fetch classes asynchronously based on major
            val allClasses = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(context)?.classDAO()?.getAll() ?: emptyList()
            }

            // Filter classes by the selected major ID and store them in filteredClasses
            filteredClasses = allClasses.filter { it.majorId == selectedMajorId }

            // Update classNames with filtered classes
            classNames = ArrayList<String>(filteredClasses.size).apply {
                for (clazz in filteredClasses) {
                    clazz.name?.let { add(it) }
                }
            }
            Log.d("hnnnnnn", classNames.toString())

            // Clear the class EditText
            view.findViewById<EditText>(R.id.edtClass).setText("")
        }
    }
    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtStudentName: TextView = itemView.findViewById(R.id.txtStudentName)

        fun setItemClickListener(itemClickListener: ItemClickListener) {
            itemView.setOnClickListener { itemClickListener.onClick(itemView, adapterPosition, false) }
            itemView.setOnLongClickListener {
                itemClickListener.onClick(itemView, adapterPosition, true)
                true
            }
        }
    }


    fun handleImageResult(bitmap: Bitmap, imageBytes: ByteArray) {
        var tempImageBytes = imageBytes
    }


}
