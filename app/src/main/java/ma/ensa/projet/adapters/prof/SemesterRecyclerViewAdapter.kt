package ma.ensa.projet.adapters.prof

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensa.projet.R
import ma.ensa.projet.adapters.admin.listener.ItemClickListener
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.data.entities.Semester
import ma.ensa.projet.utilities.Utils
import java.util.Locale

class SemesterRecyclerViewAdapter(
    private val context: Context,
    private val intent: Intent,
    semesters: ArrayList<Semester>,
    private val coroutineScope: CoroutineScope
) : RecyclerView.Adapter<SemesterRecyclerViewAdapter.SemesterViewHolder>(), Filterable {

    private val originalList: ArrayList<Semester> = semesters
    private var filteredList: ArrayList<Semester> = semesters
    private var subjectMap: Map<Long, List<String>> = emptyMap() // Mapping of semester ID to subject names
    private var majorMap: Map<Long, String> = emptyMap() // Mapping of semester ID to major name
    private var classMap: Map<Long, String> = emptyMap() // Mapping of semester ID to class name

    init {
        // Load subjects, major names, and class names for each semester initially
        loadSubjectsAndNames()
    }

    private fun loadSubjectsAndNames() {
        coroutineScope.launch(Dispatchers.IO) {
            val tempSubjectMap = mutableMapOf<Long, List<String>>()
            val tempMajorMap = mutableMapOf<Long, String>()
            val tempClassMap = mutableMapOf<Long, String>()
            val db = AppDatabase.getInstance(context)

            for (semester in originalList) {
                // Fetch subjects for each semester
                val subjects = db.subjectDAO().getBySemester(semester.id)
                val subjectNames = subjects.map { it.subject.name }
                tempSubjectMap[semester.id] = subjectNames

                // Fetch and store major name for each semester's major ID
                val major = semester.majorId?.let { db.majorDAO().getById(it) }
                tempMajorMap[semester.id] = major?.name ?: "Unknown Major"

                // Fetch and store class name for each subject’s class ID in the semester
                val classEntity = subjects.firstOrNull()?.subject?.classId?.let { db.classDAO().getById(it) }
                tempClassMap[semester.id] = classEntity?.name ?: "Unknown Class"
            }
            subjectMap = tempSubjectMap
            majorMap = tempMajorMap
            classMap = tempClassMap

            withContext(Dispatchers.Main) {
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SemesterViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.z_layout_recycle_view_semester, parent, false)
        return SemesterViewHolder(view)
    }

    override fun onBindViewHolder(holder: SemesterViewHolder, position: Int) {
        val semester = filteredList[position]
        val majorName = majorMap[semester.id] ?: "Unknown Major"
        val className = classMap[semester.id] ?: "Unknown Class"

        holder.txtSemesterName.text = "${semester.name} - $majorName : $className"
        holder.txtSubjects.text = subjectMap[semester.id]?.joinToString(", ") ?: "No subjects"
    }

    override fun getItemCount(): Int = filteredList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val query = Utils.removeVietnameseAccents(charSequence.toString().lowercase(Locale.getDefault()))
                val results = FilterResults()

                if (query != null) {
                    filteredList = if (query.isNotEmpty()) {
                        originalList.filter { semester ->
                            val subjects = subjectMap[semester.id] ?: emptyList()
                            subjects.any { subjectName ->
                                query?.let {
                                    Utils.removeVietnameseAccents(subjectName.lowercase(Locale.getDefault()))
                                        ?.contains(it)
                                } == true
                            }
                        } as ArrayList<Semester>
                    } else {
                        originalList
                    }
                }

                results.values = filteredList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filteredList = filterResults.values as ArrayList<Semester>
                notifyDataSetChanged()
            }
        }
    }

    class SemesterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val txtSemesterName: TextView = itemView.findViewById(R.id.txtSemesterName)
        val txtSubjects: TextView = itemView.findViewById(R.id.txtSubjects) // New TextView for subjects
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
