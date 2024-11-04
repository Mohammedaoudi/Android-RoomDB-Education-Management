package ma.ensa.projet.data.dto


import androidx.room.Embedded
import androidx.room.Relation
import ma.ensa.projet.data.entities.AcademicYear
import ma.ensa.projet.data.entities.Major
import ma.ensa.projet.data.entities.Classe


data class ClassWithRelations(
    @Embedded
    var clazz: Classe,

    @Relation(parentColumn = "major_id", entityColumn = "id")
    var major: Major?,


    @Relation(parentColumn = "academic_year_id", entityColumn = "id")
    var academicYear: AcademicYear?
)
