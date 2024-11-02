package ma.ensa.projet.adapters.admin


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ma.ensa.projet.R
import ma.ensa.projet.data.dto.StatisticalOfLecturer


class LecturerStatisticalRecycleViewAdapter(
    private val context: Context,
    statisticalOfLecturers: ArrayList<StatisticalOfLecturer>
) :
    RecyclerView.Adapter<LecturerStatisticalRecycleViewAdapter.LecturerStatisticalViewHolder>() {
    private val statisticalOfLecturers: ArrayList<StatisticalOfLecturer> =
        statisticalOfLecturers

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LecturerStatisticalViewHolder {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.admin_layout_recycle_view_statistical_lecturer, parent, false)

        return LecturerStatisticalViewHolder(view)
    }

    override fun onBindViewHolder(holder: LecturerStatisticalViewHolder, position: Int) {
        val statisticalOfLecturer: StatisticalOfLecturer = statisticalOfLecturers[position]
        holder.txtLecturerNumber.text = (position + 1).toString()
        holder.txtSubjectName.setText(statisticalOfLecturer.subjectName)
        holder.txtClassName.setText(statisticalOfLecturer.className)
    }

    override fun getItemCount(): Int {
        return statisticalOfLecturers.size
    }

    class LecturerStatisticalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtLecturerNumber: TextView = view.findViewById<TextView>(R.id.txtLecturerNumber)
        val txtSubjectName: TextView = view.findViewById<TextView>(R.id.txtSubjectName)
        val txtClassName: TextView = view.findViewById<TextView>(R.id.txtClassName)
    }
}
