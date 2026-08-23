package com.maranatha.skools.models

import kotlinx.serialization.Serializable

@Serializable
data class PaperScoreDetail(
    val paperNumber: Int,
    val paperName: String,
    val score: Double,
    val maxMarks: Int
)

@Serializable
data class SubjectReportItem(
    val subjectId: Int,
    val subjectName: String,
    val subjectCode: String,
    val paperScores: List<PaperScoreDetail>,
    val averageScore: Double,
    val grade: String,
    val aggregateValue: Int,
    val teacherComment: String
)

@Serializable
data class StudentReportCardResponse(
    val studentId: Int,
    val fullName: String,
    val admissionNumber: String,
    val className: String,
    val streamName: String,
    val examTermName: String,
    val year: Int,
    val term: Int,
    val subjects: List<SubjectReportItem>,
    val totalMarks: Double,
    val overallAverage: Double,
    val best8Aggregate: Int?,
    val division: String,
    val positionInStream: Int,
    val totalStudentsInStream: Int,
    val classTeacherComment: String,
    val headteacherComment: String
)