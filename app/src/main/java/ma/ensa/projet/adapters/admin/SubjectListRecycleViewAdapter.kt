package ma.ensa.projet.adapters.admin

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
import ma.ensa.projet.data.entities.Major
import ma.ensa.projet.data.entities.Semester
import ma.ensa.projet.data.entities.SubjectSemesterCrossRef
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

            // Fetch semester name using DAO
            CoroutineScope(Dispatchers.IO).launch {
                val semester =
                    subjectWithRelations.semesterId?.let {
                        AppDatabase.getInstance(context)?.semesterDAO()?.getById(
                            it
                        )
                    }
                withContext(Dispatchers.Main) {
                    txtSemester.text = semester?.name ?: "N/A"
                    semester?.name?.let { Log.d("EditSemestre", it) }
                Log.d("EditSemestre2", subjectWithRelations.semesterId.toString())// Handle the case where semester is null
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
                    .setPositiveButton("Yes") { _, _ -> deleteSubject(originalList.indexOf(subjectWithRelations)) }
                    .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                    .show()
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


    private fun showEditSubjectDialog(subjectWithRelations: SubjectWithRelations) {
        val view = LayoutInflater.from(context).inflate(R.layout.admin_bottom_sheet_edit_subject, null)
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

        // Populate fields with existing subject data
        edtSubjectName.setText(subjectWithRelations.subject.name)
        edtSubjectCredits.setText(subjectWithRelations.subject.credits.toString())
        edtClassName.setText(subjectWithRelations.clazz.name)
        edtMajorName.setText(subjectWithRelations.major.name)

        selectedSemesterId = subjectWithRelations.semesterId
        edtSemesterName.setText(semesters.find { it.id == selectedSemesterId }?.name)
        Log.d("tatatt", edtSemesterName.text.toString())

        // Initially disable class and semester selection until a major is selected
        edtClassName.isEnabled = true
        edtSemesterName.isEnabled = true  // Enable since we have both major and class

        updateSemestersForMajor(selectedMajor!!.id, view)

        // Major selection
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
                        edtClassName.isEnabled = true // Enable class selection now that major is selected
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
            if (selectedMajor == null ) {
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

        // Edit button action
        val btnEdit: Button = view.findViewById(R.id.btnEditSubject)
        btnEdit.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Notification")
                .setMessage("Confirm edit of subject information?")
                .setPositiveButton("Yes") { _, _ -> performEditSubject(subjectWithRelations, view) }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
    private fun performEditSubject(subjectWithRelations: SubjectWithRelations, view: View) {
        if (!validateInputs(view)) return

        val edtSubjectName: EditText = view.findViewById(R.id.edtSubjectName)
        val edtSubjectCredits: EditText = view.findViewById(R.id.edtSubjectCredits)

        // Update the subjectWithRelations object with new data
        subjectWithRelations.subject.apply {
            name = edtSubjectName.text.toString()
            credits = edtSubjectCredits.text.toString().toIntOrNull() ?: 0
            classId = selectedClass?.id!!
            majorId = selectedMajor?.id!!
        }

        subjectWithRelations.clazz = selectedClass!!
        subjectWithRelations.major = selectedMajor!!
        subjectWithRelations.semesterId = selectedSemesterId  // Update the semesterId

        val position = filteredList.indexOfFirst { it.subject.id == subjectWithRelations.subject.id }
        if (position != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)

                    // Update the subject in the database
                    db?.subjectDAO()?.update(subjectWithRelations.subject)

                    // First delete existing cross reference
                    db?.subjectSemesterCrossRefDAO()?.deleteBySubjectId(subjectWithRelations.subject.id)

                    // Then insert the new cross reference
                    selectedSemesterId?.let { semesterId ->
                        val crossRef = SubjectSemesterCrossRef(
                            subjectId = subjectWithRelations.subject.id,
                            semesterId = semesterId
                        )
                        db?.subjectSemesterCrossRefDAO()?.insert(crossRef)

                        Log.d("openclassTest", "Inserted semesterId: $semesterId for subjectId: ${subjectWithRelations.subject.id}")

                        // Verification step: Retrieve and log to confirm semesterId is saved
                        val verifSub = db?.subjectDAO()?.getSubjectsWithRelationsByClassId(subjectWithRelations.clazz.id)
                        Log.d("openclassTest", "Verified Subject: ${verifSub}")
                    }

                    withContext(Dispatchers.Main) {
                        if (position in filteredList.indices) {
                            filteredList[position] = subjectWithRelations
                            notifyItemChanged(position)
                            bottomSheetDialog.dismiss()
                            Utils.showToast(context, "Successfully edited")
                        } else {
                            Utils.showToast(context, "Position out of bounds for filtered list")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Utils.showToast(context, "Error updating subject: ${e.message}")
                    }
                }
            }
        } else {
            Utils.showToast(context, "Subject not found in the list")
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