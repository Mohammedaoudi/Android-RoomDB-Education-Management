package ma.ensa.projet.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "scores",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
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
data class Score(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var type: String,
    @ColumnInfo(defaultValue = "0")
    var point: Float = 0f,
    @ColumnInfo(name = "student_id")
    var studentId: Long,
    @ColumnInfo(name = "subject_id")
    var subjectId: Long,
    @ColumnInfo(name = "semester_id")
    var semesterId: Long
)