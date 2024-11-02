package ma.ensa.projet.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ma.ensa.projet.data.dto.LecturerAndUser
import ma.ensa.projet.data.entities.Lecturer


@Dao
interface LecturerDAO {

    @Query("SELECT * FROM lecturers ORDER BY id DESC")
    fun getAll(): List<Lecturer>

    @Query("""
        SELECT l.* FROM lecturers l 
        JOIN users u ON u.id = l.user_id 
        JOIN lecturer_subject_cross_ref lsc ON l.id = lsc.lecturer_id 
        JOIN subject_semester_cross_ref ssc ON lsc.subject_id = ssc.subject_id 
        WHERE ssc.semester_id = :semesterId 
        GROUP BY u.full_name
    """)
    fun getAllLecturerAndUserBySemester(semesterId: Long): List<LecturerAndUser>

    @Query("SELECT * FROM lecturers WHERE id = :id")
    fun getById(id: Long): Lecturer

    @Query("SELECT * FROM lecturers WHERE user_id = :userId")
    fun getByUser(userId: Long): Lecturer

    @Query("SELECT COUNT(*) FROM lecturers")
    fun count(): Int

    @Insert
    fun insert(lecturer: Lecturer): Long

    @Insert
    fun insert(vararg lecturers: Lecturer)

    @Update
    fun update(lecturer: Lecturer)

    @Delete
    fun delete(lecturer: Lecturer)

    @Query("DELETE FROM lecturers WHERE user_id = :userId")
    fun deleteByUser(userId: Long)
}
