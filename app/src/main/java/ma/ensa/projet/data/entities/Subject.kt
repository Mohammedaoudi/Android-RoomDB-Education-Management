package ma.ensa.projet.data.entities


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "subjects",
    foreignKeys = [
        ForeignKey(
            entity = Major::class,
            parentColumns = ["id"],
            childColumns = ["major_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Classe::class,
            parentColumns = ["id"],
            childColumns = ["class_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String,
    var credits: Int,
    @ColumnInfo(name = "major_id")
    var majorId: Long,
    @ColumnInfo(name = "class_id")
    var classId: Long
)
