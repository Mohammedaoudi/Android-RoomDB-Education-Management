package ma.ensa.projet.data.dto

import ma.ensa.projet.data.entities.Lecturer
import ma.ensa.projet.data.entities.User


import androidx.room.Embedded
import androidx.room.Relation


data class LecturerAndUser(
    @Embedded
    var lecturer: Lecturer,

    @Relation(parentColumn = "user_id", entityColumn = "id")
    var user: User
)
