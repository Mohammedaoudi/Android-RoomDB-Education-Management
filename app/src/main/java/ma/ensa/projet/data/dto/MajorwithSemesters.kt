package ma.ensa.projet.data.dto

import androidx.room.Embedded
import androidx.room.Relation
import ma.ensa.projet.data.entities.Major
import ma.ensa.projet.data.entities.Semester

data class MajorWithSemesters(
    @Embedded
    val major: Major,

    @Relation(parentColumn = "id", entityColumn = "major_id")
    val semesters: List<Semester>  // List of semesters associated with the major
)
