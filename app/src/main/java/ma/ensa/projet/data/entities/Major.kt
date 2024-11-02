package ma.ensa.projet.data.entities


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey




@Entity(
    tableName = "majors",
)
data class Major(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String? = null)