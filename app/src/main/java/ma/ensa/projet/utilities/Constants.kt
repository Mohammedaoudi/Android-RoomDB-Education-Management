package ma.ensa.projet.utilities


object Constants {
    const val DATABASE_NAME: String = "ensaj_management.db"
    const val DATABASE_VERSION: Int = 1

    const val USER_ID: String = "userId"

    const val SEMESTER_ID: String = "semesterId"
    const val CLASS_ID: String = "classId"
    const val SUBJECT_ID: String = "subjectId"

    const val MALE: Boolean = false
    const val FEMALE: Boolean = true

    enum class Role {
        ADMIN, LECTURER, STUDENT
    }
}
