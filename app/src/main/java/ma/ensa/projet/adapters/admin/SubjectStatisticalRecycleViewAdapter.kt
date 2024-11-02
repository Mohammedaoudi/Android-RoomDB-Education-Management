package ma.ensa.projet.adapters.admin


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ma.ensa.projet.R
import ma.ensa.projet.data.dto.StatisticalOfSubject
import java.lang.String
import kotlin.Int

class SubjectStatisticalRecycleViewAdapter(
    private val context: Context,
    statisticalOfSubjects: ArrayList<StatisticalOfSubject>
) :
    RecyclerView.Adapter<SubjectStatisticalRecycleViewAdapter.SubjectStatisticalViewHolder>() {
    private val statisticalOfSubjects: ArrayList<StatisticalOfSubject> =
        statisticalOfSubjects

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubjectStatisticalViewHolder {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.admin_layout_recycle_view_statistical_subject, parent, false)

        return SubjectStatisticalViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectStatisticalViewHolder, position: Int) {
        val statisticalOfSubject: StatisticalOfSubject = statisticalOfSubjects[position]
        holder.txtSubjectNumber.text = (position + 1).toString()
        holder.txtSubjectName.setText(statisticalOfSubject.subjectName)
        holder.txtSubjectCredits.setText(String.valueOf(statisticalOfSubject.subjectCredits))
        holder.txtClassName.setText(statisticalOfSubject.className)
        holder.txtNumberOfStudent.setText(String.valueOf(statisticalOfSubject.numberOfStudents))
    }

    override fun getItemCount(): Int {
        return statisticalOfSubjects.size
    }

    class SubjectStatisticalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtSubjectNumber: TextView = itemView.findViewById<TextView>(R.id.txtSubjectId)
        val txtSubjectName: TextView = itemView.findViewById<TextView>(R.id.txtSubjectName)
        val txtSubjectCredits: TextView = itemView.findViewById<TextView>(R.id.txtSubjectCredits)
        val txtClassName: TextView = itemView.findViewById<TextView>(R.id.txtClassName)
        val txtNumberOfStudent: TextView = itemView.findViewById<TextView>(R.id.txtNumberOfStudent)
    }
}
