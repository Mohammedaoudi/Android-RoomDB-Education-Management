package ma.ensa.projet.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "academic_years")
data class AcademicYear(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String? = null,
    @ColumnInfo(name = "start_date")
    var startDate: Date? = null,
    @ColumnInfo(name = "end_date")
    var endDate: Date? = null
)