package ma.ensa.projet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ma.ensa.projet.R
import ma.ensa.projet.data.converters.DateConverter
import ma.ensa.projet.data.dao.*
import ma.ensa.projet.data.entities.*
import ma.ensa.projet.utilities.Constants
import ma.ensa.projet.utilities.Utils
import java.util.Calendar

@Database(
    entities = [
        AcademicYear::class,
        Classe::class,
        Lecturer::class,
        Major::class,
        Semester::class,
        Student::class,
        Subject::class,
        User::class,
        LecturerSubjectCrossRef::class,
        StudentSubjectCrossRef::class,
        SubjectSemesterCrossRef::class
    ],
    version = Constants.DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun academicYearDAO(): AcademicYearDAO
    abstract fun classDAO(): ClassDAO
    abstract fun crossRefDAO(): CrossRefDAO
    abstract fun lecturerDAO(): LecturerDAO
    abstract fun majorDAO(): MajorDAO
    abstract fun semesterDAO(): SemesterDAO
    abstract fun studentDAO(): StudentDAO
    abstract fun subjectDAO(): SubjectDAO
    abstract fun userDAO(): UserDAO
    abstract fun subjectSemesterCrossRefDAO(): SubjectSemesterCrossRefDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                Constants.DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()

        fun initializeDatabase(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                // Delete the database first
                context.applicationContext.deleteDatabase(Constants.DATABASE_NAME)
                // Get a new instance which will trigger recreation
                getInstance(context)
                // Insert default values
                insertDefaultValues(context)
            }
        }

        private suspend fun insertDefaultValues(context: Context) {
            getInstance(context).apply {
                insertAcademicYears(context)
                insertMajors(context)
                insertUsers(context)


            }
        }

        private suspend fun insertAcademicYears(context: Context) {
            val db = getInstance(context)
            (2024..2024).forEach { year ->
                AcademicYear().apply {
                    name = "$year"
                    startDate = Calendar.getInstance().apply {
                        set(year, Calendar.SEPTEMBER, 1)
                    }.time
                    endDate = Calendar.getInstance().apply {
                        set(year + 1, Calendar.AUGUST, 31)
                    }.time
                    db.academicYearDAO().insert(this)
                }
            }
        }




        private suspend fun insertMajors(context: Context) {
            val db = getInstance(context)
            val majors = listOf("2ITE", "GI", "G2E", "ISIC", "GC")

            majors.forEach { majorName ->
                // Insert each major
                val major = Major(name = majorName).apply {
                    id = db.majorDAO().insert(this) // Capture inserted ID for this major
                }

                // Assign semesters based on the major name
                val semesterNames = if (majorName == "GI") {
                    listOf("S1", "S2")  // "GI" major has only two semesters
                } else {
                    listOf("S1", "S2", "S3", "S4")  // Other majors have four semesters
                }

                // Insert semesters for the current major
                semesterNames.forEach { semesterName ->
                    Semester().apply {
                        name = semesterName
                        majorId = major.id // Associate with the current major
                        startDate = Calendar.getInstance().apply {
                            set(Calendar.MONTH, Calendar.SEPTEMBER) // Sample start month
                            set(Calendar.DAY_OF_MONTH, 1)
                        }.time
                        endDate = Calendar.getInstance().apply {
                            set(Calendar.MONTH, Calendar.DECEMBER) // Sample end month
                            set(Calendar.DAY_OF_MONTH, 31)
                        }.time
                        db.semesterDAO().insert(this)
                    }
                }
            }
        }



        private suspend fun insertUsers(context: Context) {
            val db = getInstance(context)

            // Insert admin
            User(
                fullName = "Administrator",
                email = "admin@gmail.com",
                password = Utils.hashPassword("admin@123"),
                avatar = Utils.getBytesFromDrawable(context, R.drawable.default_avatar),
                role = Constants.Role.ADMIN
            ).let { db.userDAO().insert(it) }


        }
    }
}