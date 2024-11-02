package ma.ensa.projet.data.dto

import androidx.room.Embedded
import androidx.room.Relation
import ma.ensa.projet.data.entities.Major
import ma.ensa.projet.data.entities.Subject
import ma.ensa.projet.data.entities.Classe

data class SubjectWithRelations(
    @Embedded
    var subject: Subject,
    @Relation(parentColumn = "class_id", entityColumn = "id")
    var clazz: Classe,
    @Relation(parentColumn = "major_id", entityColumn = "id")
    var major: Major,
    val semesterId: Long?
)
