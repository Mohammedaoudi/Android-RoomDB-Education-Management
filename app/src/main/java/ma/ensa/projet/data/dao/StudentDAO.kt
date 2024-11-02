package ma.ensa.projet.data.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ma.ensa.projet.data.dto.StudentWithRelations
import ma.ensa.projet.data.dto.StudentWithScores
import ma.ensa.projet.data.entities.Student


@Dao
interface StudentDAO {

    @Query("SELECT * FROM students ")
    fun getAll(): List<Student>

    @Query("SELECT * FROM students ")
    fun getAllWithRelations(): List<StudentWithRelations>

    @Query("SELECT * FROM students WHERE class_id != :classId ORDER BY id DESC")
    fun getAllWithRelationsExclusiveClass(classId: Long): List<StudentWithRelations>

    @Query("SELECT * FROM students WHERE id = :id")
    fun getById(id: Long): Student

    @Query("SELECT * FROM students WHERE user_id = :userId")
    fun getByUser(userId: Long): Student


    @Query("""
        SELECT s.* FROM students s 
        JOIN users u ON s.user_id = u.id 
        JOIN student_semester_cross_ref ssc ON s.id = ssc.student_id 
        WHERE ssc.semester_id = :semesterId 
        ORDER BY u.full_name
    """)
    fun getBySemester(semesterId: Long): List<StudentWithRelations>

    @Query("""
        SELECT s.* FROM students s 
        JOIN users u ON s.user_id = u.id 
        WHERE s.class_id = :classId 
        ORDER BY u.full_name
    """)
    fun getByClass(classId: Long): List<StudentWithRelations>

    @Query("""
        SELECT s.* FROM students s 
        JOIN users u ON s.user_id = u.id 
        JOIN student_semester_cross_ref sscr ON s.id = sscr.student_id 
        JOIN student_subject_cross_ref ssur ON s.id = ssur.student_id 
        JOIN subjects subj ON ssur.subject_id= subj.id 
        WHERE sscr.semester_id = :semesterId AND subj.class_id = :classId 
        ORDER BY u.full_name
    """)
    fun getBySemesterClass(semesterId: Long, classId: Long): List<StudentWithRelations>

    @Query("""
        SELECT s.* FROM students s 
        JOIN users u ON s.user_id = u.id 
        JOIN student_semester_cross_ref sscr ON s.id = sscr.student_id 
        JOIN student_subject_cross_ref ssur ON s.id = ssur.student_id 
        JOIN subjects subj ON ssur.subject_id= subj.id 
        WHERE sscr.semester_id = :semesterId AND subj.class_id = :classId AND ssur.subject_id = :subjectId 
        GROUP BY s.id 
        ORDER BY u.full_name
    """)
    fun getBySemesterClassSubject(semesterId: Long, classId: Long, subjectId: Long): List<StudentWithRelations>

    @Query("SELECT COUNT(*) FROM students")
    fun count(): Int

    @Insert
    fun insert(student: Student): Long

    @Insert
    fun insert(vararg students: Student)

    @Update
    fun update(student: Student)

    @Delete
    fun delete(student: Student)

    @Query("""
        SELECT s.* FROM students s 
        JOIN users u ON s.user_id = u.id 
        WHERE s.academic_year_id = :academicYearId 
        ORDER BY u.full_name
    """)
    fun getByAcademicYear(academicYearId: Long): List<StudentWithRelations>

    @Query("""
        SELECT s.* FROM students s 
        JOIN users u ON s.user_id = u.id 
        WHERE s.major_id = :majorId 
        ORDER BY u.full_name
    """)
    fun getByMajor(majorId: Long): List<StudentWithRelations>

    @Query("""
        SELECT s.* FROM students s 
        JOIN users u ON s.user_id = u.id 
        WHERE s.class_id = :classId 
        AND s.academic_year_id = :academicYearId 
        ORDER BY u.full_name
    """)
    fun getByAcademicYearClass(academicYearId: Long, classId: Long): List<StudentWithRelations>

    @Query("""
        SELECT s.* FROM students s 
        JOIN users u ON s.user_id = u.id 
        WHERE s.major_id = :majorId 
        AND s.academic_year_id = :academicYearId 
        ORDER BY u.full_name
    """)
    fun getByAcademicYearMajor(academicYearId: Long, majorId: Long): List<StudentWithRelations>

    @Query("""
        SELECT s.* FROM students s 
        JOIN users u ON s.user_id = u.id 
        WHERE s.major_id = :majorId 
        AND s.class_id = :classId 
        ORDER BY u.full_name
    """)
    fun getByMajorClass(majorId: Long, classId: Long): List<StudentWithRelations>

    @Query("""
        SELECT s.* FROM students s 
        JOIN users u ON s.user_id = u.id 
        WHERE s.academic_year_id = :academicYearId 
        AND s.major_id = :majorId 
        AND s.class_id = :classId 
        ORDER BY u.full_name
    """)
    fun getByAcademicYearMajorClass(academicYearId: Long, majorId: Long, classId: Long): List<StudentWithRelations>

}
