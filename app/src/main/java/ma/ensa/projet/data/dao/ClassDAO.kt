package ma.ensa.projet.data.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ma.ensa.projet.data.dto.ClassWithRelations
import ma.ensa.projet.data.entities.Classe

@Dao
interface ClassDAO {

    @Query("SELECT * FROM classes ORDER BY name")
    fun getAll(): List<Classe>

    @Query("SELECT * FROM classes ORDER BY name")
    fun getAllWithRelations(): List<ClassWithRelations>

    @Query("SELECT * FROM classes WHERE id = :id")
    fun getById(id: Long): Classe

    @Query("""
        SELECT c.* FROM classes c 
        JOIN subjects s ON s.class_id = c.id 
        JOIN subject_semester_cross_ref ssc ON ssc.subject_id = s.id 
        WHERE ssc.semester_id = :semesterId 
        ORDER BY c.name
    """)
    fun getBySemester(semesterId: Long): List<ClassWithRelations>

    @Query("""
        SELECT c.* FROM classes c 
        JOIN subjects s ON s.class_id = c.id 
        JOIN subject_semester_cross_ref ssc ON ssc.subject_id = s.id 
        JOIN lecturer_subject_cross_ref lsc ON lsc.subject_id = s.id 
        JOIN lecturers l ON l.id = lsc.lecturer_id 
        JOIN users u ON u.id = l.user_id 
        WHERE u.id = :userId AND ssc.semester_id = :semesterId 
        ORDER BY c.name
    """)
    fun getByLecturerSemester(userId: Long, semesterId: Long): List<ClassWithRelations>



    @Query("""
        SELECT * FROM classes 
        WHERE major_id = :majorId 
        ORDER BY name
    """)
    fun getByMajor(majorId: Long): List<ClassWithRelations>

    @Query("""
        SELECT * FROM classes 
        WHERE academic_year_id = :academicYearId 
        ORDER BY name
    """)
    fun getByAcademicYear(academicYearId: Long): List<ClassWithRelations>




    @Query("""
        SELECT * FROM classes 
        WHERE major_id = :majorId AND academic_year_id = :academicYearId 
        ORDER BY name
    """)
    fun getByMajorAcademicYear(majorId: Long, academicYearId: Long): List<ClassWithRelations>


    @Query("SELECT COUNT(*) FROM classes")
    fun count(): Int

    @Insert
    fun insert(classEntity: Classe): Long

    @Insert
    fun insert(vararg classes: Classe)

    @Update
    fun update(classEntity: Classe)

    @Delete
    fun delete(classEntity: Classe)
}
