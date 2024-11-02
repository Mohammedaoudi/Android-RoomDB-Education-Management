package ma.ensa.projet.data.dao



import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ma.ensa.projet.data.entities.Major

@Dao
interface MajorDAO {

    @Query("SELECT * FROM majors ORDER BY id DESC")
    fun getAll(): List<Major>

    @Query("SELECT * FROM majors WHERE id = :id")
    fun getById(id: Long): Major

    @Query("SELECT COUNT(*) FROM majors")
    fun count(): Int

    @Insert
    fun insert(major: Major): Long


    @Query("SELECT * FROM majors WHERE id IN (SELECT major_id FROM classes WHERE academic_year_id = :academicYearId) ORDER BY id DESC")
    fun getByAcademicYear(academicYearId: Long): List<Major>  // Changed from ArrayList to List



    @Insert
    fun insert(vararg majors: Major)

    @Update
    fun update(major: Major)

    @Delete
    fun delete(major: Major)
}
