package ma.ensa.projet.data.dto


import androidx.room.Embedded
import androidx.room.Relation
import ma.ensa.projet.data.entities.AcademicYear
import ma.ensa.projet.data.entities.Major
import ma.ensa.projet.data.entities.Student
import ma.ensa.projet.data.entities.Classe
import ma.ensa.projet.data.entities.User


data class StudentWithRelations(
    @Embedded
    var student: Student,
    @Relation(parentColumn = "major_id", entityColumn = "id")
    val major: Major,
    @Relation(parentColumn = "academic_year_id", entityColumn = "id")
    val academicYear: AcademicYear,
    @Relation(parentColumn = "class_id", entityColumn = "id")
    val clazz: Classe?,
    @Relation(parentColumn = "user_id", entityColumn = "id")
    var user: User
)
