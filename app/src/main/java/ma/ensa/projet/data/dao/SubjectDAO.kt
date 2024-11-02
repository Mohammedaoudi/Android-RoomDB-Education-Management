package ma.ensa.projet.data.dao

import ma.ensa.projet.data.dto.SubjectWithRelations
import ma.ensa.projet.data.entities.Subject


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SubjectDAO {

    @Query("SELECT * FROM subjects ORDER BY name")
    fun getAll(): List<Subject>

    @Query("""
        SELECT s.*, ssr.semester_id AS semesterId 
        FROM subjects s 
        JOIN subject_semester_cross_ref ssr ON s.id = ssr.subject_id 
        ORDER BY name
    """)
    fun getAllWithRelations(): List<SubjectWithRelations>


    @Query("""
    SELECT s.*, ssr.semester_id AS semesterId 
    FROM subjects s 
    LEFT JOIN subject_semester_cross_ref ssr ON s.id = ssr.subject_id 
    WHERE s.id = :id 
    ORDER BY s.name
""")
    fun getSubjectWithRelationsById(id: Long): SubjectWithRelations?


    @Query("SELECT * FROM subjects WHERE id = :id")
    fun getById(id: Long): Subject

    @Query("""
        SELECT s.*, ssr.semester_id AS semesterId 
        FROM subjects s 
        JOIN subject_semester_cross_ref ssr ON s.id = ssr.subject_id 
        WHERE ssr.semester_id = :semesterId 
        ORDER BY s.name, s.credits
    """)
    fun getBySemester(semesterId: Long): List<SubjectWithRelations>

    @Query("""
        SELECT s.*, ssr.semester_id AS semesterId 
        FROM subjects s 
        JOIN lecturer_subject_cross_ref lsr ON s.id = lsr.subject_id 
        JOIN subject_semester_cross_ref ssr ON s.id = ssr.subject_id 
        JOIN lecturers l ON lsr.lecturer_id = l.id 
        JOIN users u ON l.user_id = u.id 
        WHERE u.id = :userId AND ssr.semester_id = :semesterId 
        ORDER BY s.name, s.credits
    """)
    fun getByLecturerSemester(userId: Long, semesterId: Long): List<SubjectWithRelations>

    @Query("""
        SELECT s.*, ssr.semester_id AS semesterId 
        FROM subjects s 
        JOIN lecturer_subject_cross_ref lsr ON s.id = lsr.subject_id 
        JOIN subject_semester_cross_ref ssr ON s.id = ssr.subject_id 
        JOIN lecturers l ON lsr.lecturer_id = l.id 
        JOIN users u ON l.user_id = u.id 
        WHERE u.id = :userId AND ssr.semester_id = :semesterId AND s.class_id = :classId 
        ORDER BY s.name, s.credits
    """)
    fun getByLecturerSemesterClass(userId: Long, semesterId: Long, classId: Long): List<SubjectWithRelations>

    @Query("""
        SELECT s.*, ssr.semester_id AS semesterId 
        FROM subjects s 
        JOIN classes c ON s.class_id = c.id 
        JOIN subject_semester_cross_ref ssr ON s.id = ssr.subject_id 
        WHERE ssr.semester_id = :semesterId AND c.id = :classId 
        ORDER BY s.name, c.name, s.credits
    """)
    fun getBySemesterClass(semesterId: Long, classId: Long): List<SubjectWithRelations>

    @Query("SELECT COUNT(*) FROM subjects")
    fun count(): Int

    @Insert
    fun insert(subject: Subject): Long

    @Insert
    fun insert(vararg subjects: Subject)

    @Update
    fun update(subject: Subject)

    @Delete
    fun delete(subject: Subject)
}
