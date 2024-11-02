package ma.ensa.projet.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import ma.ensa.projet.data.entities.SubjectSemesterCrossRef

@Dao
interface SubjectSemesterCrossRefDAO {
    @Insert
    fun insert(crossRef: SubjectSemesterCrossRef): Long

    @Insert
    fun insertAll(crossRefs: List<SubjectSemesterCrossRef>)

    @Delete
    fun delete(crossRef: SubjectSemesterCrossRef)
}