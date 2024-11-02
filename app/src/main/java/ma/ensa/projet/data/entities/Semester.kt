package ma.ensa.projet.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "semesters",
    foreignKeys = [
        ForeignKey(
            entity = AcademicYear::class,
            parentColumns = ["id"],
            childColumns = ["academic_year_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Major::class,
            parentColumns = ["id"],
            childColumns = ["major_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Semester(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String? = null,
    @ColumnInfo(name = "start_date")
    var startDate: Date? = null,
    @ColumnInfo(name = "end_date")
    var endDate: Date? = null,
    @ColumnInfo(name = "academic_year_id")
    var academicYearId: Long? = null,
    @ColumnInfo(name = "major_id")
    var majorId: Long? = null  // New foreign key to Major
)
