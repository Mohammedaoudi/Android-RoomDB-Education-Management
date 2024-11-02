package ma.ensa.projet.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ma.ensa.projet.data.entities.AcademicYear


@Dao
interface AcademicYearDAO {

    @Query("SELECT * FROM academic_years ORDER BY id DESC")
    fun getAll(): List<AcademicYear>

    @Query("SELECT * FROM academic_years WHERE id = :id")
    fun getById(id: Long): AcademicYear

    @Query("SELECT COUNT(*) FROM academic_years")
    fun count(): Int

    @Insert
    fun insert(academicYear: AcademicYear): Long

    @Insert
    fun insert(vararg academicYears: AcademicYear)

    @Update
    fun update(academicYear: AcademicYear)

    @Delete
    fun delete(academicYear: AcademicYear)
}