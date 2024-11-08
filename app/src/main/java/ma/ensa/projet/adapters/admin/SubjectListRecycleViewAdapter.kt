package ma.ensa.projet.adapters.admin

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensa.projet.R
import ma.ensa.projet.adapters.admin.listener.ItemClickListener
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.data.dto.SubjectWithRelations
import ma.ensa.projet.data.entities.Classe
import ma.ensa.projet.data.entities.Lecturer
import ma.ensa.projet.data.entities.LecturerSubjectCrossRef
import ma.ensa.projet.data.entities.Major
import ma.ensa.projet.data.entities.Semester
import ma.ensa.projet.data.entities.SubjectSemesterCrossRef
import ma.ensa.projet.ui.admin.OpenClassActivity
import ma.ensa.projet.ui.admin.StudentListActivity
import ma.ensa.projet.utilities.Constants
import ma.ensa.projet.utilities.Utils
import java.util.Locale



class SubjectListRecycleViewAdapter(
    private val context: Context,
    private val originalList: ArrayList<SubjectWithRelations>
) : RecyclerView.Adapter<SubjectListRecycleViewAdapter.SubjectViewHolder>(), Filterable {

    private val bottomSheetDialog = BottomSheetDialog(context)
    private var selectedMajor: Major? = null
    private var majors = ArrayList<Major>()
    private lateinit var majorNames: Array<String>

    private var selectedClass: Classe? = null
    private var classes = ArrayList<Classe>()
    private lateinit var classNames: ArrayList<String>

    private var selectedSemesterId: Long? = null
    private var semesters = ArrayList<Semester>()
    private lateinit var semesterNames: Array<String>


    private var filteredList: ArrayList<SubjectWithRelations> = originalList
    private var currentFilterText = ""

    private var shouldDissociateLecturer = false  // Nouvelle variable pour tracker l'état



    init {
        fetchInitialData()
    }

    private fun fetchInitialData() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)

            // Fetch all data
            majors = ArrayList(db?.majorDAO()?.getAll() ?: listOf())
            classes = ArrayList(db?.classDAO()?.getAll() ?: listOf())
            semesters = ArrayList(db?.semesterDAO()?.getAll() ?: listOf())

            // Initialize the name arrays/lists
            majorNames = majors.mapNotNull { it.name }.toTypedArray()
            classNames = ArrayList(classes.mapNotNull { it.name })
            semesterNames = semesters.mapNotNull { it.name }.toTypedArray()

            Log.d("tesstst", semesters.toString())

            withContext(Dispatchers.Main) {
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.admin_layout_recycle_view_list_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subjectWithRelations = filteredList[position]
        holder.apply {
            txtSubjectName.text = subjectWithRelations.subject.name
            txtClassName.text = subjectWithRelations.clazz.name
            txtMajorName.text = subjectWithRelations.major.name
            txtSubjectCredits.text = subjectWithRelations.subject.credits.toString()

            // Fetch semester name and lecturers using CrossRefDAO
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(context)

                // Fetch semester name
                val semester = subjectWithRelations.semesterId?.let {
                    db?.semesterDAO()?.getById(it)
                }

                // Fetch lecturers associated with the subject
                // Fetch lecturers associated with the subject
                val lecturerUsers = db?.crossRefDAO()?.getLecturerDetailsForSubject(subjectWithRelations.subject.id)

                // Prepare lecturer names
                val lecturerNames = lecturerUsers?.joinToString(", ") { it.fullName.toString() }
                    ?: "Lecturer not assigned yet"

                withContext(Dispatchers.Main) {
                    if (lecturerNames.isNullOrEmpty()) {
                        txtLecturer.text = "Not assigned yet"
                    } else {
                        txtLecturer.text = lecturerNames
                    }
                    txtSemester.text = semester?.name ?: "N/A"
                }
            }

            setItemClickListener(object : ItemClickListener {
                override fun onClick(view: View?, pos: Int, isLongClick: Boolean) {
                    val intent = Intent(context, StudentListActivity::class.java).apply {
                        putExtra("isStudentSubject", true)
                        putExtra(Constants.SEMESTER_ID, subjectWithRelations.semesterId)
                        putExtra(Constants.CLASS_ID, subjectWithRelations.clazz.id)
                        putExtra(Constants.SUBJECT_ID, subjectWithRelations.subject.id)
                    }
                    context.startActivity(intent)
                }
            })

            btnEditSubject.setOnClickListener { showEditSubjectDialog(subjectWithRelations) }

            btnDeleteSubject.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Notification")
                    .setMessage("Confirm deletion of the subject?")
                    .setPositiveButton("Yes") { _, _ ->
                        deleteSubject(originalList.indexOf(subjectWithRelations))
                    }
                    .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }
    }




    private fun showEditSubjectDialog(subjectWithRelations: SubjectWithRelations) {
        val view =
            LayoutInflater.from(context).inflate(R.layout.admin_bottom_sheet_edit_subject, null)
        bottomSheetDialog.setContentView(view)
        BottomSheetBehavior.from(view.parent as View).state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()

        selectedClass = subjectWithRelations.clazz
        selectedMajor = subjectWithRelations.major

        val edtSubjectName: EditText = view.findViewById(R.id.edtSubjectName)
        val edtSubjectCredits: EditText = view.findViewById(R.id.edtSubjectCredits)
        val edtClassName: EditText = view.findViewById(R.id.edtClass)
        val edtMajorName: EditText = view.findViewById(R.id.edtMajor)
        val edtSemesterName: EditText = view.findViewById(R.id.edtSemester)
        val edtLecturer: EditText = view.findViewById(R.id.edtLecturer)

        var selectedLecturerId: MutableSet<Long> = mutableSetOf() // Changé en Set
        val lecturers = mutableListOf<Lecturer>()

// Launching the coroutine to handle lecturer retrieval and UI update
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val lecturerSubjects =
                    db?.crossRefDAO()?.getLecturerSubjects(subjectWithRelations.subject.id)
                val allLecturers = db?.lecturerDAO()?.getAll()?.toMutableList() ?: mutableListOf()
                lecturers.addAll(allLecturers)

                lecturerSubjects?.forEach { crossRef ->
                    val currentLecturer = allLecturers.find { it.id == crossRef.lecturerId }
                    val user = currentLecturer?.let { db?.userDAO()?.getById(it.userId) }
                    if (user != null) {
                        selectedLecturerId.add(crossRef.lecturerId)
                    }
                }

                val lecturerNames = lecturerSubjects?.mapNotNull { crossRef ->
                    allLecturers.find { it.id == crossRef.lecturerId }?.let { lecturer ->
                        db?.userDAO()?.getById(lecturer.userId)?.fullName
                    }
                } ?: listOf()

                val displayText =
                    if (lecturerNames.isEmpty()) "Not assigned" else lecturerNames.joinToString(", ")

                withContext(Dispatchers.Main) {
                    edtLecturer.setText(displayText)
                    val btnDissociateLecturer: Button = view.findViewById(R.id.btnDissociateLecturer)

                    // Set button visibility based on whether lecturers are assigned
                    btnDissociateLecturer.visibility = if (selectedLecturerId.isNotEmpty()) View.VISIBLE else View.GONE

                    if (displayText == "Not assigned") {
                        edtLecturer.isFocusable = false
                        edtLecturer.isClickable = false
                        edtLecturer.setText("Not assigned")
                    } else {
                        edtLecturer.isFocusable = true
                        edtLecturer.isClickable = true
                        edtLecturer.setOnClickListener {
                            showLecturerSelectionDialog(
                                lecturers,
                                subjectWithRelations,
                                selectedLecturerId,
                                edtLecturer,
                                view
                            )
                        }
                    }

                    // Move button click listener here
                    btnDissociateLecturer.setOnClickListener {
                        AlertDialog.Builder(context)
                            .setTitle("Confirm Dissociation")
                            .setMessage("Are you sure you want to dissociate the lecturer from this subject?")
                            .setPositiveButton("Yes") { _, _ ->
                                shouldDissociateLecturer = true
                                selectedLecturerId.clear()
                                edtLecturer.setText("Not assigned")
                                btnDissociateLecturer.visibility = View.GONE
                            }
                            .setNegativeButton("No", null)
                            .show()
                    }
                }            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // Populate other fields as before...
        edtSubjectName.setText(subjectWithRelations.subject.name)
        edtSubjectCredits.setText(subjectWithRelations.subject.credits.toString())
        edtClassName.setText(subjectWithRelations.clazz.name)
        edtMajorName.setText(subjectWithRelations.major.name)
        selectedSemesterId = subjectWithRelations.semesterId
        edtSemesterName.setText(semesters.find { it.id == selectedSemesterId }?.name)

        edtMajorName.setOnClickListener {
            if (majorNames.isNotEmpty()) {
                AlertDialog.Builder(context)
                    .setTitle("Select Major")
                    .setItems(majorNames) { _, which ->
                        selectedMajor = majors[which]
                        edtMajorName.setText(selectedMajor?.name)

                        // Update classes based on the selected major
                        updateClassesForMajor(selectedMajor!!.id, view)

                        // Clear previous selections
                        edtClassName.setText("") // Clear class selection
                        edtSemesterName.setText("") // Clear semester selection
                        edtClassName.isEnabled =
                            true // Enable class selection now that major is selected
                        edtSemesterName.isEnabled = false
                    }
                    .show()
            } else {
                Utils.showToast(context, "No majors available")
            }
        }

        edtClassName.setOnClickListener {
            if (selectedMajor == null) {
                Utils.showToast(context, "Please select a major first")
            } else if (classNames.isNotEmpty()) {
                AlertDialog.Builder(context)
                    .setTitle("Select Class")
                    .setItems(classNames.toTypedArray()) { _, which ->
                        selectedClass = classes.firstOrNull { it.name == classNames[which] }
                        Log.d("selectesggg", selectedClass.toString())
                        edtClassName.setText(selectedClass?.name)
                        // Clear semester selection
                        edtSemesterName.isEnabled = true

                        updateSemestersForMajor(selectedClass!!.majorId, view)
                    }
                    .show()
            } else {
                Utils.showToast(context, "No classes available for selected major")
            }
        }

        // Semester selection
        edtSemesterName.setOnClickListener {
            if (selectedMajor == null) {
                Utils.showToast(context, "Please select a major first")

            } else {
                val majorSemesters = semesters.filter { it.majorId == selectedMajor!!.id }
                val majorSemesterNames = majorSemesters.map { it.name }.toTypedArray()

                if (majorSemesterNames.isEmpty()) {
                    Utils.showToast(context, "No semesters available for the selected major")
                    return@setOnClickListener
                }

                AlertDialog.Builder(context)
                    .setTitle("Select Semester")
                    .setItems(majorSemesterNames) { _, which ->
                        selectedSemesterId = majorSemesters[which].id
                        edtSemesterName.setText(majorSemesterNames[which])
                    }
                    .show()
            }
        }

        val btnDissociateLecturer: Button = view.findViewById(R.id.btnDissociateLecturer)
        btnDissociateLecturer.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Confirm Dissociation")
                .setMessage("Are you sure you want to dissociate the lecturer from this subject?")
                .setPositiveButton("Yes") { _, _ ->
                    // Mettre à jour selectedLecturerId immédiatement
                    selectedLecturerId.clear()
                    edtLecturer.setText("Not assigned")
                    btnDissociateLecturer.visibility = View.GONE

                    // Effectuer la dissociation dans la base de données
                    dissociateLecturerFromSubject(subjectWithRelations.subject.id)
                }
                .setNegativeButton("No", null)
                .show()
        }

        val btnEdit: Button = view.findViewById(R.id.btnEditSubject)
        btnEdit.setOnClickListener {
            // Récupérer les valeurs des champs
            val newName = edtSubjectName.text.toString().trim()
            val newCredits = edtSubjectCredits.text.toString().trim()

            // Validation des champs
            if (newName.isEmpty() || newCredits.isEmpty()) {
                Utils.showToast(context, "Please fill in all required fields")
                return@setOnClickListener
            }

            if (selectedClass == null || selectedMajor == null || selectedSemesterId == -1L) {
                Utils.showToast(context, "Please select class, major and semester")
                return@setOnClickListener
            }

            AlertDialog.Builder(context)
                .setTitle("Notification")
                .setMessage("Confirm edit of subject information?")
                .setPositiveButton("Yes") { _, _ ->
                    try {
                        performEditSubject(
                            subjectWithRelations,
                            view,
                            selectedLecturerId
                        ) // Pass selectedLecturerId here
                        bottomSheetDialog.dismiss()
                    } catch (e: Exception) {
                        Utils.showToast(context, "Error updating subject: ${e.message}")
                    }
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun showLecturerSelectionDialog(
        lecturers: List<Lecturer>,
        subjectWithRelations: SubjectWithRelations,
        selectedLecturerIds: MutableSet<Long>,
        edtLecturer: EditText,
        view: View
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val lecturerNames = lecturers.map { lecturer ->
                val user = AppDatabase.getInstance(context).userDAO().getById(lecturer.userId)
                user?.fullName ?: "Unknown Lecturer"
            }.toMutableList()

            val checkedItems = BooleanArray(lecturers.size) { false }

            lecturers.forEachIndexed { index, lecturer ->
                checkedItems[index] = selectedLecturerIds.contains(lecturer.id)
            }

            withContext(Dispatchers.Main) {
                AlertDialog.Builder(context)
                    .setTitle("Select Lecturers")
                    .setMultiChoiceItems(
                        lecturerNames.toTypedArray(),
                        checkedItems
                    ) { _, which, isChecked ->
                        if (isChecked) {
                            selectedLecturerIds.add(lecturers[which].id)
                        } else {
                            selectedLecturerIds.remove(lecturers[which].id)
                        }

                        val selectedNames = selectedLecturerIds.mapNotNull { id ->
                            lecturers.find { it.id == id }?.let { lecturer ->
                                lecturerNames[lecturers.indexOf(lecturer)]
                            }
                        }
                        edtLecturer.setText(selectedNames.joinToString(", "))

                        // Update dissociate button visibility
                        val btnDissociateLecturer: Button = view.findViewById(R.id.btnDissociateLecturer)
                        btnDissociateLecturer.visibility = if (selectedLecturerIds.isNotEmpty()) View.VISIBLE else View.GONE

                    }
                    .setPositiveButton("OK", null)
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }
        }
    }

    private fun performEditSubject(
        subjectWithRelations: SubjectWithRelations,
        view: View,
        selectedLecturerIds: Set<Long>
    ) {
        if (!validateInputs(view)) return

        val edtSubjectName: EditText = view.findViewById(R.id.edtSubjectName)
        val edtSubjectCredits: EditText = view.findViewById(R.id.edtSubjectCredits)
        val edtLecturer: EditText = view.findViewById(R.id.edtLecturer) // Ajoutez cette ligne

        subjectWithRelations.subject.apply {
            name = edtSubjectName.text.toString()
            credits = edtSubjectCredits.text.toString().toIntOrNull() ?: 0
            classId = selectedClass?.id!!
            majorId = selectedMajor?.id!!
        }

        subjectWithRelations.clazz = selectedClass!!
        subjectWithRelations.major = selectedMajor!!
        subjectWithRelations.semesterId = selectedSemesterId

        val position = filteredList.indexOfFirst { it.subject.id == subjectWithRelations.subject.id }
        if (position != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)

                    // Update subject
                    db?.subjectDAO()?.update(subjectWithRelations.subject)

                    // Update semester association
                    db?.subjectSemesterCrossRefDAO()?.deleteBySubjectId(subjectWithRelations.subject.id)
                    selectedSemesterId?.let { semesterId ->
                        val crossRef = SubjectSemesterCrossRef(
                            subjectId = subjectWithRelations.subject.id,
                            semesterId = semesterId
                        )
                        db?.subjectSemesterCrossRefDAO()?.insert(crossRef)
                    }

                    // Vérifier si "Not assigned" est sélectionné
                    if (edtLecturer.text.toString() == "Not assigned") {
                        // Dissocier tous les professeurs
                        db?.crossRefDAO()?.deleteLecturerSubjectCrossRefBySubjectId(subjectWithRelations.subject.id)
                    } else if (selectedLecturerIds.isNotEmpty()) {
                        // Mettre à jour les associations normalement
                        db?.crossRefDAO()?.deleteLecturerSubjectCrossRefBySubjectId(subjectWithRelations.subject.id)
                        selectedLecturerIds.forEach { lecturerId ->
                            val lecturerCrossRef = LecturerSubjectCrossRef(
                                lecturerId = lecturerId,
                                subjectId = subjectWithRelations.subject.id
                            )
                            db?.crossRefDAO()?.insertLecturerSubjectCrossRef(lecturerCrossRef)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        if (position in filteredList.indices) {
                            filteredList[position] = subjectWithRelations
                            notifyItemChanged(position)
                            bottomSheetDialog.dismiss()
                            Utils.showToast(context, "Successfully edited")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Utils.showToast(context, "Error updating subject: ${e.message}")
                    }
                }
            }
        }
    }










    private fun dissociateLecturerFromSubject(subjectId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                db?.crossRefDAO()?.deleteLecturerSubjectCrossRefBySubjectId(subjectId)

                withContext(Dispatchers.Main) {
                    Utils.showToast(context, "Lecturer dissociated successfully")

                    // Mise à jour de l'interface utilisateur
                    val position = filteredList.indexOfFirst { it.subject.id == subjectId }
                    if (position != -1) {
                        // Mettre à jour l'objet dans la liste
                        val updatedSubject = filteredList[position].copy()
                        filteredList[position] = updatedSubject
                        notifyItemChanged(position)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Utils.showToast(context, "Error dissociating lecturer: ${e.message}")
                }
            }
        }
    }
    private fun updateClassesForMajor(selectedMajorId: Long, view: View) {
        CoroutineScope(Dispatchers.Main).launch {            // Fetch classes asynchronously based on major
            val allClasses = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(context)?.classDAO()?.getAll() ?: emptyList()
            }

            // Filter classes by the selected major ID
            val filteredClasses = allClasses.filter { it.majorId == selectedMajorId }

            // Update classNames with filtered classes
            classNames = ArrayList<String>(filteredClasses.size).apply {
                for (clazz in filteredClasses) {
                    clazz.name?.let { add(it) }
                }
            }

            // Clear the class EditText
            view.findViewById<EditText>(R.id.edtClass).setText("")
        }
    }

    private fun updateSemestersForMajor(majorId: Long?, view: View) {
        if (majorId != null) {
            CoroutineScope(Dispatchers.Main).launch {
                val filteredSemesters = withContext(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(context)
                    db?.semesterDAO()?.getSemestersByMajorId(majorId) ?: emptyList()
                }

                // Update semesters on the main thread
                semesters.clear()
                semesters.addAll(filteredSemesters)
                semesterNames = Array(semesters.size) { semesters[it].name.toString() }
                Log.d("hnaaaaaaa", semesterNames.toString())
            }
        } else {
            semesters.clear()
            semesterNames = emptyArray()
        }
    }



    fun addSubject(subjectWithRelations: SubjectWithRelations) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val subjectId = AppDatabase.getInstance(context)?.subjectDAO()?.insert(subjectWithRelations.subject)
                Log.d("InsertSubject", "Inserted subject ID: $subjectId")
            } catch (e: Exception) {
                Log.e("InsertSubject", "Error inserting subject: ${e.message}", e)
            }

            withContext(Dispatchers.Main) {
                originalList.add(0, subjectWithRelations)
                notifyItemInserted(0)
            }
        }
    }


    private fun deleteSubject(position: Int) {
        val subjectWithRelations = filteredList[position]
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(context)?.subjectDAO()?.delete(subjectWithRelations.subject)

            // Switch back to the main thread to update the UI
            withContext(Dispatchers.Main) {
                originalList.remove(subjectWithRelations)
                filteredList.remove(subjectWithRelations)
                getFilter().filter(currentFilterText)
                notifyItemRemoved(position)
            }
        }
    }

    override fun getItemCount(): Int = filteredList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                currentFilterText = charSequence.toString()
                val query = Utils.removeVietnameseAccents(currentFilterText.lowercase(Locale.getDefault()))

                if (query != null) {
                    filteredList = if (query.isEmpty()) {
                        originalList
                    } else {
                        originalList.filter {
                            Utils.removeVietnameseAccents(it.subject.name.lowercase(Locale.getDefault()))
                                ?.contains(query)!!
                        } as ArrayList<SubjectWithRelations>
                    }
                }

                return FilterResults().apply { values = filteredList }
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filteredList = filterResults.values as ArrayList<SubjectWithRelations>
                notifyDataSetChanged()
            }
        }
    }

    private fun validateInputs(view: View): Boolean {
        return validateNotEmpty(view, R.id.edtSubjectName, "Subject name cannot be empty") &&
                validateNotEmpty(view, R.id.edtSubjectCredits, "Credits cannot be empty") &&
                validateNotEmpty(view, R.id.edtClass, "Class cannot be empty") &&
                validateNotEmpty(view, R.id.edtMajor, "Major cannot be empty")
    }


    private fun validateNotEmpty(view: View, viewId: Int, errorMessage: String): Boolean {
        val editText: EditText = view.findViewById(viewId)
        return if (editText.text.toString().trim().isEmpty()) {
            Utils.showToast(context, errorMessage)
            false
        } else {
            true
        }
    }

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

        val txtSubjectName: TextView = itemView.findViewById(R.id.txtSubjectName)
        val txtClassName: TextView = itemView.findViewById(R.id.txtClassName)
        val txtLecturer: TextView = itemView.findViewById(R.id.txtLecturer)
        val txtMajorName: TextView = itemView.findViewById(R.id.txtMajorName)
        val txtSubjectCredits: TextView = itemView.findViewById(R.id.txtSubjectCredits)
        val txtSemester: TextView = itemView.findViewById(R.id.txtSemester)
        val btnEditSubject: Button = itemView.findViewById(R.id.btnEditSubject)
        val btnDeleteSubject: Button = itemView.findViewById(R.id.btnDeleteSubject)

        private var itemClickListener: ItemClickListener? = null

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        fun setItemClickListener(itemClickListener: ItemClickListener) {
            this.itemClickListener = itemClickListener
        }

        override fun onClick(view: View) {
            itemClickListener?.onClick(view, adapterPosition, false)
        }

        override fun onLongClick(view: View): Boolean {
            itemClickListener?.onClick(view, adapterPosition, true)
            return true
        }
    }


}