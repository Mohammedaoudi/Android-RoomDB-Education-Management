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
import ma.ensa.projet.ui.admin.SubjectListActivity
import ma.ensa.projet.utilities.Constants
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

    init {
        // Load subjects for each semester initially
        loadSubjects()
    }

    private fun loadSubjects() {
        coroutineScope.launch(Dispatchers.IO) {
            val tempSubjectMap = mutableMapOf<Long, List<String>>()
            for (semester in originalList) {
                val subjects = AppDatabase.getInstance(context).subjectDAO().getBySemester(semester.id)
                val subjectNames = subjects.map { it.subject.name }
                tempSubjectMap[semester.id] = subjectNames
            }
            subjectMap = tempSubjectMap
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SemesterViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.z_layout_recycle_view_semester, parent, false)
        return SemesterViewHolder(view)
    }

    override fun onBindViewHolder(holder: SemesterViewHolder, position: Int) {
        val semester = filteredList[position]
        holder.txtSemesterName.text = semester.name // No need to load major name here
        holder.txtSubjects.text = "Loading subjects..."

        // Set subjects from the pre-loaded map
        holder.txtSubjects.text = "Subject(module): ${subjectMap[semester.id]?.joinToString(", ") ?: "No subjects"}"


    }

    override fun getItemCount(): Int = filteredList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val query = Utils.removeVietnameseAccents(charSequence.toString().lowercase(Locale.getDefault()))
                val results = FilterResults()

                if (query != null) {
                    if (query.isNotEmpty()) {
                        filteredList = originalList.filter { semester ->
                            val subjects = subjectMap[semester.id] ?: emptyList()
                            subjects.any { subjectName ->
                                Utils.removeVietnameseAccents(subjectName.lowercase(Locale.getDefault()))
                                    ?.contains(query)!!
                            }
                        } as ArrayList<Semester>
                    } else {
                        filteredList = originalList
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

