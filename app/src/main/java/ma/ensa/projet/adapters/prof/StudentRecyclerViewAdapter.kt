package ma.ensa.projet.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ma.ensa.projet.databinding.LecturerLayoutRecycleViewStudentBinding

class StudentRecyclerViewAdapter : ListAdapter<String, StudentRecyclerViewAdapter.StudentViewHolder>(StudentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = LecturerLayoutRecycleViewStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val studentName = getItem(position)
        holder.bind(studentName)
    }

    class StudentViewHolder(private val binding: LecturerLayoutRecycleViewStudentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(studentName: String) {
            binding.txtStudentName.text = studentName
        }
    }

    class StudentDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem // Adjust this based on your unique identifier for students
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
