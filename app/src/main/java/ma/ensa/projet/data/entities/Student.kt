package ma.ensa.projet.data.entities
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey



@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Major::class,
            parentColumns = ["id"],
            childColumns = ["major_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = AcademicYear::class,
            parentColumns = ["id"],
            childColumns = ["academic_year_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Classe::class,
            parentColumns = ["id"],
            childColumns = ["class_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,


    @ColumnInfo(name = "user_id")
    var userId: Long,
    @ColumnInfo(name = "major_id")
    var majorId: Long? = null,
    @ColumnInfo(name = "class_id")
    var classId: Long? = null,
    @ColumnInfo(name = "academic_year_id")
    var academicYearId: Long? = null
)