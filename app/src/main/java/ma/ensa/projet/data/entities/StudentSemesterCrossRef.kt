package ma.ensa.projet.data.entities
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "student_semester_cross_ref",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Semester::class,
            parentColumns = ["id"],
            childColumns = ["semester_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StudentSemesterCrossRef(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var gpa: Float = 0f,
    var totalCredits: Float = 0f,
    @ColumnInfo(name = "student_id")
    var studentId: Long,
    @ColumnInfo(name = "semester_id")
    var semesterId: Long
)