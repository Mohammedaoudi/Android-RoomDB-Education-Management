package ma.ensa.projet.data.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ma.ensa.projet.data.entities.Score

@Dao
interface ScoreDAO {

    @Query("SELECT * FROM scores ORDER BY id DESC")
    fun getAll(): List<Score>

    @Query("SELECT * FROM scores WHERE id = :id")
    fun getById(id: Long): Score

    @Query("""
        SELECT * FROM scores 
        WHERE student_id = :studentId 
        AND subject_id = :subjectId 
        AND semester_id = :semesterId
    """)
    fun getByStudent(semesterId: Long, subjectId: Long, studentId: Long): List<Score>

    @Query("SELECT COUNT(*) FROM scores")
    fun count(): Int

    @Insert
    fun insert(score: Score): Long

    @Insert
    fun insert(vararg scores: Score)

    @Update
    fun update(score: Score)

    @Delete
    fun delete(score: Score)
}
