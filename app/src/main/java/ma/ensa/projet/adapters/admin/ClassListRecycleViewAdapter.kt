package ma.ensa.projet.adapters.admin

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import ma.ensa.projet.data.entities.Major
import ma.ensa.projet.ui.admin.StudentListActivity
import ma.ensa.projet.utilities.Constants
import ma.ensa.projet.utilities.Utils
import java.util.Locale
class ClassListRecycleViewAdapter(
    private val context: Context,
    originalList: ArrayList<ClassWithRelations>,
    private val majors: List<Major>, // Accept majors as a parameter
    private val academicYears: List<AcademicYear> // Accept academicYears as a parameter
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
        classWithRelations.major?.let { major ->
            CoroutineScope(Dispatchers.IO).launch {
                // Get the faculty by ID in the background

                // Update UI elements on the main thread
                withContext(Dispatchers.Main) {
                    holder.txtClassName.text = classWithRelations.clazz.name
                    holder.txtMajorName.text = classWithRelations.major?.name
                    holder.txtAcademicYearName.text = classWithRelations.academicYear?.name
                }
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

    fun addClass(classWithRelations: ClassWithRelations) {
        CoroutineScope(Dispatchers.IO).launch {
            // Insert class into the database
            AppDatabase.getInstance(context)?.classDAO()?.insert(classWithRelations.clazz)

            // Update the UI on the main thread after the insert
            withContext(Dispatchers.Main) {
                originalList.add(0, classWithRelations)
                notifyItemInserted(0)
            }
        }
    }

    private fun editClass(position: Int) {
        val classWithRelations: ClassWithRelations = originalList[position]
        CoroutineScope(Dispatchers.IO).launch {
            // Update class in the database
            AppDatabase.getInstance(context)?.classDAO()?.update(classWithRelations.clazz)

            // Update the UI on the main thread after the update
            withContext(Dispatchers.Main) {
                notifyItemChanged(position)
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

    @SuppressLint("InflateParams")
    private fun showEditClassDialog(classWithRelations: ClassWithRelations) {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.admin_bottom_sheet_edit_class, null)
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

        edtClassName.setText(classWithRelations.clazz.name)
        edtMajorName.setText(classWithRelations.major?.name )
        edtAcademicYearName.setText(classWithRelations.academicYear?.name )

        edtMajorName.setOnClickListener { v: View? ->
            AlertDialog.Builder(context)
                .setTitle("Select Major")
                .setItems(majorNames) { dialog: DialogInterface?, which: Int ->
                    selectedMajor = majors[which]
                    (view.findViewById<View>(R.id.edtMajor) as EditText).setText(majorNames[which])
                }
                .show()
        }

        edtAcademicYearName.setOnClickListener { v: View? ->
            AlertDialog.Builder(context)
                .setTitle("Select Academic Year")
                .setItems(academicYearNames) { dialog: DialogInterface?, which: Int ->
                    selectedAcademicYear = academicYears[which]
                    (view.findViewById<View>(R.id.edtAcademicYear) as EditText).setText(academicYearNames[which])
                }
                .show()
        }

        btnEdit.setOnClickListener { v: View? ->
            AlertDialog.Builder(context)
                .setTitle("Notification")
                .setMessage("Confirm editing class information?")
                .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
                    performEditClass(classWithRelations, view)
                }
                .setNegativeButton("No") { dialog: DialogInterface, which: Int -> dialog.dismiss() }
                .show()
        }
    }

    private fun performEditClass(classWithRelations: ClassWithRelations, view: View) {
        if (!validateInputs(view)) return

        val edtClassName = view.findViewById<EditText>(R.id.edtClassName)
        val edtMajorName = view.findViewById<EditText>(R.id.edtMajor)
        val edtAcademicYearName = view.findViewById<EditText>(R.id.edtAcademicYear)

        // Update the class, major, and academic year names with user input
        classWithRelations.clazz.name = edtClassName.text.toString() // Set class name
        classWithRelations.major?.name = edtMajorName.text.toString() // Set major name
        classWithRelations.academicYear?.name = edtAcademicYearName.text.toString() // Set academic year name

        // Assign the selected major and academic year
        classWithRelations.major = selectedMajor // Ensure selectedMajor is not null before this line
        classWithRelations.academicYear = selectedAcademicYear // Ensure selectedAcademicYear is not null before this line

        // Update the class in the database asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(context)?.classDAO()?.update(classWithRelations.clazz)

            // Update the original list on the main thread
            withContext(Dispatchers.Main) {
                editClass(originalList.indexOf(classWithRelations)) // Update the original list
                bottomSheetDialog.dismiss() // Dismiss the dialog
                Utils.showToast(context, "Edit successful") // Show success message
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
        val txtClassName: TextView = itemView.findViewById<TextView>(R.id.txtClassName)
        val txtMajorName: TextView = itemView.findViewById<TextView>(R.id.txtMajorName)
        val txtAcademicYearName: TextView =
            itemView.findViewById<TextView>(R.id.txtAcademicYearName)
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