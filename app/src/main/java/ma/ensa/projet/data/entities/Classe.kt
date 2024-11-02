package ma.ensa.projet.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "classes",
    foreignKeys = [
        ForeignKey(
            entity = Major::class,
            parentColumns = ["id"],
            childColumns = ["major_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AcademicYear::class,
            parentColumns = ["id"],
            childColumns = ["academic_year_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Classe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String? = null,
    @ColumnInfo(name = "major_id")
    var majorId: Long? = null,
    @ColumnInfo(name = "academic_year_id")
    var academicYearId: Long? = null,
)
