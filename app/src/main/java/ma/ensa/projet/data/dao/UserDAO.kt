package ma.ensa.projet.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ma.ensa.projet.data.entities.User
import ma.ensa.projet.utilities.Constants

@Dao
interface UserDAO {

    @Query("SELECT * FROM users ORDER BY full_name")
    fun getAll(): List<User>

    @Query("SELECT * FROM users WHERE role != :role ORDER BY full_name")
    fun getAllRoleWithoutSelectedRole(role: Constants.Role): List<User>

    @Query("SELECT * FROM users WHERE id = :id")
    fun getById(id: Long): User

    @Query("SELECT * FROM users WHERE email = :email")
    fun getByEmail(email: String): User

    @Query("SELECT * FROM users WHERE role = :role")
    fun getByRole(role: Constants.Role): List<User>

    @Query("SELECT COUNT(*) FROM users")
    fun count(): Int

    @Insert
    fun insert(user: User): Long

    @Insert
    fun insert(vararg users: User)

    @Update
    fun update(user: User)

    @Delete
    fun delete(user: User)
}