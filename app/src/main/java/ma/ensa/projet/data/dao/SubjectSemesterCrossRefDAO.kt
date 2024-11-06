package ma.ensa.projet.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ma.ensa.projet.data.entities.SubjectSemesterCrossRef

@Dao
interface SubjectSemesterCrossRefDAO {
    @Insert
    fun insert(crossRef: SubjectSemesterCrossRef): Long

    @Update
    fun update(crossRef: SubjectSemesterCrossRef): Int

    @Insert
    fun insertAll(crossRefs: List<SubjectSemesterCrossRef>)

    @Delete
    fun delete(crossRef: SubjectSemesterCrossRef)

    @Query("DELETE FROM subject_semester_cross_ref WHERE subject_id = :subjectId AND semester_id = :semesterId")
    fun deleteBySubjectAndSemester(subjectId: Long, semesterId: Long)

    @Query("DELETE FROM subject_semester_cross_ref WHERE subject_id = :subjectId")
    suspend fun deleteBySubjectId(subjectId: Long)

}