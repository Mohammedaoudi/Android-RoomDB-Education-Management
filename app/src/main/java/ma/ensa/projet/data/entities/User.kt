package ma.ensa.projet.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ma.ensa.projet.utilities.Constants
import java.util.Date

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)],
)
data class User(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var avatar: ByteArray? = null,

    @ColumnInfo(name = "full_name")
    var fullName: String? = null,

    var gender: Boolean = false,

    @ColumnInfo(name = "date_of_birth")
    var dob: Date? = null,

    var address: String? = null,

    var email: String? = null,

    var password: String? = null,

    var role: Constants.Role? = null,
) {
    // Override equals and hashCode to handle ByteArray correctly
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (!avatar.contentEquals(other.avatar)) return false
        if (fullName != other.fullName) return false
        if (gender != other.gender) return false
        if (dob != other.dob) return false
        if (address != other.address) return false
        if (email != other.email) return false
        if (password != other.password) return false
        if (role != other.role) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + avatar.contentHashCode()
        result = 31 * result + (fullName?.hashCode() ?: 0)
        result = 31 * result + gender.hashCode()
        result = 31 * result + (dob?.hashCode() ?: 0)
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + (password?.hashCode() ?: 0)
        result = 31 * result + (role?.hashCode() ?: 0)
        return result
    }
}
