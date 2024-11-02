package ma.ensa.projet.adapters.admin


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ma.ensa.projet.R
import ma.ensa.projet.data.dto.ClassWithRelations


class ClassStatisticalRecycleViewAdapter(
    private val context: Context,
    classes: ArrayList<ClassWithRelations>
) :
    RecyclerView.Adapter<ClassStatisticalRecycleViewAdapter.ClassStatisticalViewHolder>() {
    private val classes: ArrayList<ClassWithRelations> = classes

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassStatisticalViewHolder {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.admin_layout_recycle_view_statistical_class, parent, false)

        return ClassStatisticalViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassStatisticalViewHolder, position: Int) {
        val aClass: ClassWithRelations = classes[position]
        holder.txtClassNumber.text = (position + 1).toString()
        holder.txtClassName.setText(aClass.clazz.name)
        holder.txtMajorName.setText(aClass.major?.name)
        holder.txtAcademicYearName.setText(aClass.academicYear?.name)
    }

    override fun getItemCount(): Int {
        return classes.size
    }

    class ClassStatisticalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val txtClassNumber: TextView = itemView.findViewById<TextView>(R.id.txtClassNumber)
        val txtClassName: TextView = itemView.findViewById<TextView>(R.id.txtClassName)
        val txtMajorName: TextView = itemView.findViewById<TextView>(R.id.txtMajorName)
        val txtAcademicYearName: TextView =
            itemView.findViewById<TextView>(R.id.txtAcademicYearName)
    }
}
