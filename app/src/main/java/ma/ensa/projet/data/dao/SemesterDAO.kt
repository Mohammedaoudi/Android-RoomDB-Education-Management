package ma.ensa.projet.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ma.ensa.projet.data.entities.Semester

@Dao
interface SemesterDAO {

    @Query("SELECT * FROM semesters ORDER BY id DESC")
    fun getAll(): List<Semester>

    @Query("SELECT * FROM semesters WHERE id = :id")
    fun getById(id: Long): Semester

    @Query("SELECT * FROM semesters WHERE major_id = :majorId ORDER BY id DESC")
    fun getSemestersByMajor(majorId: Long): List<Semester>  // Added function

    @Query("SELECT COUNT(*) FROM semesters")
    fun count(): Int

    @Query("SELECT * FROM semesters WHERE major_id  = :majorId")
    suspend fun getSemestersByMajorId(majorId: Long?): List<Semester>


    @Insert
    fun insert(semester: Semester): Long

    @Insert
    fun insert(vararg semesters: Semester)

    @Update
    fun update(semester: Semester)

    @Delete
    fun delete(semester: Semester)
}
