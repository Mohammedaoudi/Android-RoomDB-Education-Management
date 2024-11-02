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
import ma.ensa.projet.ui.admin.StudentListActivity
import ma.ensa.projet.utilities.Constants
import ma.ensa.projet.utilities.Utils
import java.util.Locale



class SubjectListRecycleViewAdapter(
    private val context: Context,
    private val originalList: ArrayList<SubjectWithRelations>
) : RecyclerView.Adapter<SubjectListRecycleViewAdapter.SubjectViewHolder>(), Filterable {

    private val bottomSheetDialog = BottomSheetDialog(context)
    private lateinit var classes: List<Classe>
    private lateinit var classNames: Array<String>
    private var majors: List<Major>? = null
    private var majorNames: Array<String>? = null

    private var filteredList: ArrayList<SubjectWithRelations> = originalList
    private var currentFilterText = ""
    private var selectedClass: Classe? = null
    private var selectedMajor: Major? = null


    init {
        fetchClassesAndMajors()
    }

    private fun fetchClassesAndMajors() {
        CoroutineScope(Dispatchers.IO).launch {
            classes = AppDatabase.getInstance(context)?.classDAO()?.getAll() ?: emptyList()
            classNames = Array(classes.size) { classes[it].name.toString() }

            majors = AppDatabase.getInstance(context)?.majorDAO()?.getAll()
            majorNames = majors?.let { Array(it.size) { majors!![it].name.toString() } }

            // Notify the adapter on the main thread
            withContext(Dispatchers.Main) {
                notifyDataSetChanged() // You might want to implement a better way to handle this
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
                    txtSemester.text = semester?.name ?: "N/A" // Handle the case where semester is null
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
    private fun editSubject(position: Int) {
        val subjectWithRelations = filteredList[position]
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(context)?.subjectDAO()?.update(subjectWithRelations.subject)

            // Switch back to the main thread to update the UI
            withContext(Dispatchers.Main) {
                getFilter().filter(currentFilterText)
                notifyItemChanged(position)
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
        val btnEdit: Button = view.findViewById(R.id.btnEditSubject)

        edtSubjectName.setText(subjectWithRelations.subject.name)
        edtSubjectCredits.setText(subjectWithRelations.subject.credits.toString())
        edtClassName.setText(subjectWithRelations.clazz.name)
        edtMajorName.setText(subjectWithRelations.major.name)

        edtClassName.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Select Class")
                .setItems(classNames) { _, which ->
                    selectedClass = classes[which]
                    edtClassName.setText(classNames[which])
                }
                .show()
        }

        edtMajorName.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Select Major")
                .setItems(majorNames) { _, which ->
                    selectedMajor = majors?.get(which)
                    edtMajorName.setText(majorNames?.get(which))
                }
                .show()
        }

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

        subjectWithRelations.subject.apply {
            name = edtSubjectName.text.toString()
            credits = edtSubjectCredits.text.toString().toInt()
            classId = selectedClass?.id!!
            majorId = selectedMajor?.id!!
        }

        subjectWithRelations.clazz = selectedClass!!
        subjectWithRelations.major = selectedMajor!!

        editSubject(originalList.indexOf(subjectWithRelations))
        bottomSheetDialog.dismiss()
        Utils.showToast(context, "Sửa thành công")
    }

    private fun validateInputs(view: View): Boolean {
        return validateNotEmpty(view, R.id.edtSubjectName, "Tên môn không được để trống") &&
                validateNotEmpty(view, R.id.edtSubjectCredits, "Số tín chỉ không được để trống") &&
                validateNotEmpty(view, R.id.edtClass, "Lớp không được để trống") &&
                validateNotEmpty(view, R.id.edtMajor, "Ngành không được để trống")
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