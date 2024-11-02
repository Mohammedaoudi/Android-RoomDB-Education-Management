package ma.ensa.projet.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "lecturer_subject_cross_ref",
    foreignKeys = [
        ForeignKey(
            entity = Lecturer::class,
            parentColumns = ["id"],
            childColumns = ["lecturer_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LecturerSubjectCrossRef(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "lecturer_id")
    var lecturerId: Long,
    @ColumnInfo(name = "subject_id")
    var subjectId: Long
)