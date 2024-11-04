package ma.ensa.projet.adapters.admin

import android.annotation.SuppressLint
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
import ma.ensa.projet.data.dto.ClassWithRelations
import ma.ensa.projet.data.entities.AcademicYear
import ma.ensa.projet.data.entities.Classe
import ma.ensa.projet.data.entities.Major
import ma.ensa.projet.ui.admin.StudentListActivity
import ma.ensa.projet.utilities.Constants
import ma.ensa.projet.utilities.Utils
import java.util.Locale
class ClassListRecycleViewAdapter(
    private val context: Context,
    originalList: ArrayList<ClassWithRelations>,
    private val majors: List<Major>,
    private val academicYears: List<AcademicYear>
) : RecyclerView.Adapter<ClassListRecycleViewAdapter.ClassViewHolder>(), Filterable {

    private val originalList: ArrayList<ClassWithRelations> = originalList
    private val bottomSheetDialog: BottomSheetDialog = BottomSheetDialog(context)
    private var majorNames: Array<String?> = majors.map { it.name }.toTypedArray()
    private var academicYearNames: Array<String?> = academicYears.map { it.name }.toTypedArray()

    private var currentFilterText = ""
    private var selectedMajor: Major? = null
    private var selectedAcademicYear: AcademicYear? = null
    private var filteredList: ArrayList<ClassWithRelations> = originalList



    init {
        this.filteredList = originalList

        majorNames = majors.mapNotNull { it.name }.toTypedArray() // Populate majorNames
        academicYearNames = academicYears.mapNotNull { it.name }.toTypedArray() // Populate academicYearNames
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ClassListRecycleViewAdapter.ClassViewHolder {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.admin_layout_recycle_view_list_class, parent, false)

        return ClassListRecycleViewAdapter.ClassViewHolder(
            view
        )
    }

    override fun onBindViewHolder(
        holder: ClassListRecycleViewAdapter.ClassViewHolder,
        position: Int
    ) {
        val classWithRelations: ClassWithRelations = filteredList[position]

        // Fetch faculty data in a coroutine

            CoroutineScope(Dispatchers.IO).launch {

                // Update UI elements on the main thread
                withContext(Dispatchers.Main) {
                    holder.txtClassName.text = classWithRelations.clazz.name

                    holder.txtMajorName.text = classWithRelations.major?.name

                    Log.d("EditClassh", "$classWithRelations")
                    holder.txtAcademicYearName.text = classWithRelations.academicYear?.name
                }

        }

        holder.setItemClickListener(ItemClickListener { view, position1, isLongClick ->
            val intent = Intent(context, StudentListActivity::class.java)
            intent.putExtra("isStudentClass", true)
            intent.putExtra(Constants.CLASS_ID, classWithRelations.clazz.id)
            context.startActivity(intent)
        })

        holder.btnEditClass.setOnClickListener {
            showEditClassDialog(classWithRelations)
        }

        holder.btnDeleteClass.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Notification")
                .setMessage("Confirm deletion of the class?")
                .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
                    deleteClass(originalList.indexOf(classWithRelations))
                }
                .setNegativeButton("No") { dialog: DialogInterface, which: Int -> dialog.dismiss() }
                .show()
        }
    }


    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                currentFilterText = charSequence.toString()
                val query: String = Utils.removeVietnameseAccents(
                    charSequence.toString().lowercase(
                        Locale.getDefault()
                    )
                ).toString()

                if (query.isEmpty()) {
                    filteredList = originalList
                } else {
                    val filtered: ArrayList<ClassWithRelations> = ArrayList<ClassWithRelations>()
                    for (classWithRelations in originalList) {
                        val className: String = Utils.removeVietnameseAccents(
                            classWithRelations.clazz.name?.toLowerCase(
                                Locale.getDefault()
                            )
                        ).toString()

                        if (className.contains(query)) {
                            filtered.add(classWithRelations)
                        }
                    }
                    filteredList = filtered
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList

                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filteredList = filterResults.values as ArrayList<ClassWithRelations>
                notifyDataSetChanged()
            }
        }
    }

    fun addClass(name: String, selectedMajor: Major, selectedAcademicYear: AcademicYear) {
        CoroutineScope(Dispatchers.IO).launch {
            // Create a new Classe instance without an ID (Room will auto-generate it)
            val clazz = Classe(
                name = name,
                majorId = selectedMajor.id,
                academicYearId = selectedAcademicYear.id
            )

            // Insert the class and capture the generated ID
            val newId = AppDatabase.getInstance(context)?.classDAO()?.insert(clazz)

            // Create a ClassWithRelations object with the newly inserted class
            val classWithRelations = newId?.let { id ->
                ClassWithRelations(
                    clazz = clazz.copy(id = id),
                    major = selectedMajor,
                    academicYear = selectedAcademicYear
                )
            }

            // Update the UI on the main thread
            withContext(Dispatchers.Main) {
                if (classWithRelations != null) {
                    originalList.add(0, classWithRelations)
                    notifyItemInserted(0)
                }
                // Optional: Show a success message
                Utils.showToast(context, "Added successfully")
                Log.d("ClassListAdapter", "Class added successfully to the adapter with ID: $newId")
            }
        }
    }


    private fun deleteClass(position: Int) {
        val classWithRelations: ClassWithRelations = originalList[position]
        CoroutineScope(Dispatchers.IO).launch {
            // Delete class from the database
            AppDatabase.getInstance(context)?.classDAO()?.delete(classWithRelations.clazz)

            // Update the UI on the main thread after the delete
            withContext(Dispatchers.Main) {
                originalList.remove(classWithRelations)
                notifyItemRemoved(position)
            }
        }
    }


    // Edit Part

    private fun showEditClassDialog(classWithRelations: ClassWithRelations) {
        val view: View = LayoutInflater.from(context).inflate(R.layout.admin_bottom_sheet_edit_class, null)
        bottomSheetDialog.setContentView(view)
        val behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        bottomSheetDialog.show()

        selectedMajor = classWithRelations.major
        selectedAcademicYear = classWithRelations.academicYear

        val edtClassName = view.findViewById<EditText>(R.id.edtClassName)
        val edtMajorName = view.findViewById<EditText>(R.id.edtMajor)
        val edtAcademicYearName = view.findViewById<EditText>(R.id.edtAcademicYear)
        val btnEdit = view.findViewById<Button>(R.id.btnEditClass)

        // Set initial values
        edtClassName.setText(classWithRelations.clazz.name)


        Log.d("hanan", selectedMajor?.name.toString() )

        edtMajorName.setText(classWithRelations.major?.name)
        edtAcademicYearName.setText(classWithRelations.academicYear?.name)

        // Major selection
        edtMajorName.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Select Major")
                .setItems(majorNames) { _, which ->
                    selectedMajor = majors[which]
                    edtMajorName.setText(majorNames[which])
                    Log.d("EditClass", "Selected Major: ${selectedMajor?.name}, ID: ${selectedMajor?.id}")
                }
                .show()
        }

        // Academic Year selection
        edtAcademicYearName.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Select Academic Year")
                .setItems(academicYearNames) { _, which ->
                    selectedAcademicYear = academicYears[which]
                    edtAcademicYearName.setText(academicYearNames[which])
                }
                .show()
        }

        // Edit confirmation
        btnEdit.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Notification")
                .setMessage("Confirm editing class information?")
                .setPositiveButton("Yes") { _, _ -> performEditClass(classWithRelations, view) }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun performEditClass(classWithRelations: ClassWithRelations, view: View) {
        if (!validateInputs(view)) return

        val edtClassName = view.findViewById<EditText>(R.id.edtClassName)

        // Log the original state of the class object
        Log.d("EditClassh", "Original Class: $classWithRelations")

        // Update class entity with new values
        classWithRelations.clazz.name = edtClassName.text.toString()

        // Update majorId and academicYearId
        classWithRelations.clazz.majorId = selectedMajor?.id ?: classWithRelations.clazz.majorId
        Log.d("EditClass", "Selected Major ID: ${selectedMajor?.id}")
        classWithRelations.clazz.academicYearId = selectedAcademicYear?.id ?: classWithRelations.clazz.academicYearId

        // Update references for the UI
        classWithRelations.major = selectedMajor
        classWithRelations.academicYear = selectedAcademicYear

        // Log the updated state of the class object
        Log.d("EditClassh", "Updated Class: $classWithRelations")

        // Update the class in the database asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            val classDao = AppDatabase.getInstance(context)?.classDAO()
            classDao?.update(classWithRelations.clazz) // Ensure this updates the DB correctly

            // Notify the main thread to refresh the UI
            withContext(Dispatchers.Main) {
                // Notify the adapter of the change
                notifyItemChanged(originalList.indexOf(classWithRelations))
                bottomSheetDialog.dismiss()
                Utils.showToast(context, "Edit successful")
            }
        }
    }






    private fun validateInputs(view: View): Boolean {
        return (validateNotEmpty(view, R.id.edtClassName, "Class name cannot be empty") &&
                validateNotEmpty(view, R.id.edtMajor, "Major cannot be empty") &&
                validateNotEmpty(view, R.id.edtAcademicYear, "Academic year cannot be empty"))
    }

    private fun validateNotEmpty(view: View, viewId: Int, errorMessage: String): Boolean {
        val editText = view.findViewById<EditText>(viewId)
        if (editText == null || editText.text.toString().trim { it <= ' ' }.isEmpty()) {
            Utils.showToast(context, errorMessage)
            return false
        }
        return true
    }

    class ClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener,
        OnLongClickListener {
        val txtClassName: TextView = itemView.findViewById(R.id.txtClassName)
        val txtMajorName: TextView = itemView.findViewById(R.id.txtMajorName)

        val txtAcademicYearName: TextView =
            itemView.findViewById(R.id.txtAcademicYearName)
        val btnEditClass: Button = itemView.findViewById<Button>(R.id.btnEditClass)
        val btnDeleteClass: Button = itemView.findViewById<Button>(R.id.btnDeleteClass)
        private var itemClickListener: ItemClickListener? = null

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        fun setItemClickListener(itemClickListener: ItemClickListener?) {
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