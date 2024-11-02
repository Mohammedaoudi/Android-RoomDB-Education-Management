package ma.ensa.projet.data.dto


data class StatisticalOfSubject(
    var subjectId: Long,
    var subjectName: String,
    var subjectCredits: Int,
    var className: String,
    var numberOfStudents: Int
)
