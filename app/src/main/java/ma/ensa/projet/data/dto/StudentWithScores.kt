package ma.ensa.projet.data.dto


data class StudentWithScores(
    var studentId: Long = 0,
    var studentName: String = "",
    var gkScore: Float = 0f,
    var ckScore: Float = 0f,
    var tbScore: Float = 0f
)
