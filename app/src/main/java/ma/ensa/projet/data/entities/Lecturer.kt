package ma.ensa.projet.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "lecturers",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Lecturer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var specialization: String? = null,

    @ColumnInfo(name = "user_id")
    var userId: Long
)