package ma.ensa.projet.utilities


object Constants {
    const val DATABASE_NAME: String = "ensaj_management.db"
    const val DATABASE_VERSION: Int = 1

    const val PREFS_NAME: String = "storage"
    const val IS_FIRST_TIME_LAUNCH: String = "isFirstTimeLaunch"
    const val PREF_NOTIFICATION_SWITCH: String = "notification_switch"
    const val PREF_INSERT_DEFAULT_VALUES: String = "insertedDefaultValues"

    const val USER_ID: String = "userId"
    const val SPECIALIST_ID: String = "specialistId"
    const val LECTURER_ID: String = "lecturerId"
    const val STUDENT_ID: String = "studentId"
    const val SEMESTER_ID: String = "semesterId"
    const val CLASS_ID: String = "classId"
    const val SUBJECT_ID: String = "subjectId"

    const val MALE: Boolean = false
    const val FEMALE: Boolean = true

    enum class Role {
        ADMIN, SPECIALIST, LECTURER, STUDENT
    }
}
