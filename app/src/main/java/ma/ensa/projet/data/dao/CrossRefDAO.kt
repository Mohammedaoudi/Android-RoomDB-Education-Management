package ma.ensa.projet.data.dao

import ma.ensa.projet.data.entities.LecturerSubjectCrossRef
import ma.ensa.projet.data.entities.StudentSubjectCrossRef
import ma.ensa.projet.data.entities.SubjectSemesterCrossRef
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ma.ensa.projet.data.entities.Semester
import ma.ensa.projet.data.entities.User


@Dao
interface CrossRefDAO {

    @Query("SELECT * FROM lecturer_subject_cross_ref ORDER BY id DESC")
    fun getAllLecturerSubjectCrossRef(): List<LecturerSubjectCrossRef>


    @Query("SELECT * FROM student_subject_cross_ref ORDER BY id DESC")
    fun getAllStudentSubjectCrossRef(): List<StudentSubjectCrossRef>

    @Query("SELECT * FROM subject_semester_cross_ref ")
    fun getAllSubjectSemesterCrossRef(): List<SubjectSemesterCrossRef>


    @Query("SELECT subject_id FROM lecturer_subject_cross_ref WHERE lecturer_id = :lecturerId")
    suspend fun getSubjectsByLecturer(lecturerId: Long): List<Long>


    @Query("""
        SELECT * FROM lecturer_subject_cross_ref 
        WHERE lecturer_id = :lecturerId AND subject_id = :subjectId
    """)
    fun getLecturerSubjectCrossRef(lecturerId: Long, subjectId: Long): LecturerSubjectCrossRef

    @Query("""
        SELECT s.* 
        FROM semesters s
        JOIN subject_semester_cross_ref ssc ON s.id = ssc.semester_id 
        JOIN lecturer_subject_cross_ref lsc ON ssc.subject_id = lsc.subject_id 
        WHERE lsc.lecturer_id = :lecturerId
    """)
    fun getSemestersByLecturerId(lecturerId: Long): List<Semester>

    // Add these debug queries
    @Query("""
    SELECT * FROM lecturer_subject_cross_ref 
    WHERE subject_id = :subjectId
""")
    fun getLecturerSubjects(subjectId: Long): List<LecturerSubjectCrossRef>

    @Query("""
        DELETE FROM lecturer_subject_cross_ref 
        WHERE subject_id = :subjectId
    """)
    suspend fun deleteLecturerSubjectCrossRefBySubjectId(subjectId: Long)

    @Query("""
        SELECT u.id, u.full_name, u.gender, u.date_of_birth, u.address, u.email, u.avatar, u.role 
        FROM users u 
        INNER JOIN lecturers l ON l.user_id = u.id 
        INNER JOIN lecturer_subject_cross_ref ls ON ls.lecturer_id = l.id 
        WHERE ls.subject_id = :subjectId
    """)
    fun getLecturerDetailsForSubject(subjectId: Long): List<User>



    @Query("""
        SELECT * FROM student_subject_cross_ref 
        WHERE student_id = :studentId AND subject_id = :subjectId
    """)
    fun getStudentSubjectCrossRef(studentId: Long, subjectId: Long): StudentSubjectCrossRef

    @Query("""
        SELECT * FROM subject_semester_cross_ref 
        WHERE subject_id = :subjectId AND semester_id = :semesterId
    """)
    fun getSubjectSemesterCrossRef(subjectId: Long, semesterId: Long): SubjectSemesterCrossRef

    @Insert
    fun insertLecturerSubjectCrossRef(lecturerSubjectCrossRef: LecturerSubjectCrossRef)


    @Insert
    fun insertStudentSubjectCrossRef(studentSubjectCrossRef: StudentSubjectCrossRef)

    @Insert
    fun insertSubjectSemesterCrossRef(subjectSemesterCrossRef: SubjectSemesterCrossRef)

    @Update
    fun updateLecturerSubjectCrossRef(lecturerSubjectCrossRef: LecturerSubjectCrossRef)


    @Update
    fun updateStudentSubjectCrossRef(studentSubjectCrossRef: StudentSubjectCrossRef)

    @Update
    fun updateSubjectSemesterCrossRef(subjectSemesterCrossRef: SubjectSemesterCrossRef)

    @Delete
    fun deleteLecturerSubjectCrossRef(lecturerSubjectCrossRef: LecturerSubjectCrossRef)


    @Delete
    fun deleteStudentSubjectCrossRef(studentSubjectCrossRef: StudentSubjectCrossRef)

    @Delete
    fun deleteSubjectSemesterCrossRef(subjectSemesterCrossRef: SubjectSemesterCrossRef)
}
