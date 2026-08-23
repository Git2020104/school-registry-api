package com.maranatha.skools.service

enum class UnebGrade(val label: String, val aggregate: Int, val description: String) {
    D1("D1", 1, "Distinction 1"),
    D2("D2", 2, "Distinction 2"),
    C3("C3", 3, "Credit 3"),
    C4("C4", 4, "Credit 4"),
    C5("C5", 5, "Credit 5"),
    C6("C6", 6, "Credit 6"),
    P7("P7", 7, "Pass 7"),
    P8("P8", 8, "Pass 8"),
    F9("F9", 9, "Fail 9")
}

object GradingService {

    fun calculateUnebGrade(score: Double): UnebGrade = when {
        score >= 80.0 -> UnebGrade.D1
        score >= 75.0 -> UnebGrade.D2
        score >= 70.0 -> UnebGrade.C3
        score >= 65.0 -> UnebGrade.C4
        score >= 60.0 -> UnebGrade.C5
        score >= 55.0 -> UnebGrade.C6
        score >= 50.0 -> UnebGrade.P7
        score >= 45.0 -> UnebGrade.P8
        else -> UnebGrade.F9
    }

    fun calculateDivision(best8Aggregate: Int?): String {
        if (best8Aggregate == null) return "N/A"
        return when (best8Aggregate) {
            in 8..32 -> "Division 1"
            in 33..45 -> "Division 2"
            in 46..58 -> "Division 3"
            in 59..68 -> "Division 4"
            else -> "Division U"
        }
    }

    fun generateClassTeacherComment(averageScore: Double): String = when {
        averageScore >= 85.0 -> "An outstanding performance. Keep up the high standard!"
        averageScore >= 75.0 -> "Very good results. Consistent effort demonstrated throughout the term."
        averageScore >= 65.0 -> "Good performance, but has the potential to score higher."
        averageScore >= 50.0 -> "Fair result. Needs more dedication and revision in weak subjects."
        else -> "Below expectations. Immediate academic intervention and hard work required."
    }

    fun generateHeadteacherComment(division: String): String = when (division) {
        "Division 1" -> "Excellent result. Excellent academic standing."
        "Division 2" -> "Good result. Push further to achieve Division 1."
        "Division 3" -> "Satisfactory work. Focus on improving core subjects."
        "Division 4" -> "Passable performance. Special attention needed in next term."
        else -> "Unsatisfactory. Parent/Guardian consultation strongly advised."
    }
}