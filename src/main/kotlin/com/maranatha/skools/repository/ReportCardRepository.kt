package com.maranatha.skools.repository

import com.maranatha.skools.db.DatabaseFactory.dbQuery
import com.maranatha.skools.models.*
import com.maranatha.skools.service.GradingService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.and

class ReportCardRepository {

    suspend fun generateStudentReportCard(
        studentId: Int,
        examTermId: Int
    ): StudentReportCardResponse = dbQuery {

        // Fetch Student Info
        val studentRow = (StudentsTable innerJoin ClassesTable innerJoin StreamsTable)
            .selectAll()
            .where { StudentsTable.id eq studentId }
            .singleOrNull() ?: throw IllegalArgumentException("Student with ID $studentId not found")

        val streamId = studentRow[StudentsTable.streamId]
        val firstName = studentRow[StudentsTable.firstName]
        val lastName = studentRow[StudentsTable.lastName]
        val adminNo = studentRow[StudentsTable.admissionNumber]
        val className = studentRow[ClassesTable.name]
        val streamName = studentRow[StreamsTable.name]

        // Fetch Exam Term
        val termRow = ExamTermsTable.selectAll()
            .where { ExamTermsTable.id eq examTermId }
            .singleOrNull() ?: throw IllegalArgumentException("Exam Term with ID $examTermId not found")

        val termName = termRow[ExamTermsTable.name]
        val year = termRow[ExamTermsTable.year]
        val term = termRow[ExamTermsTable.term]

        // Fetch Marks joined with Papers & Subjects
        val markRows = (MarksTable innerJoin PapersTable innerJoin SubjectsTable)
            .selectAll()
            .where { (MarksTable.studentId eq studentId) and (MarksTable.examTermId eq examTermId) }
            .toList()

        // Group Marks by Subject
        val subjectGroupMap = markRows.groupBy { it[SubjectsTable.id] }

        val subjectReportItems = subjectGroupMap.map { (subId, rows) ->
            val subName = rows.first()[SubjectsTable.name]
            val subCode = rows.first()[SubjectsTable.code]

            val paperDetails = rows.map { r ->
                PaperScoreDetail(
                    paperNumber = r[PapersTable.paperNumber],
                    paperName = r[PapersTable.name],
                    score = r[MarksTable.score],
                    maxMarks = r[PapersTable.maxMarks]
                )
            }

            val totalScore = paperDetails.sumOf { it.score }
            val totalMax = paperDetails.sumOf { it.maxMarks }
            val subjectAverage = if (totalMax > 0) (totalScore / totalMax) * 100.0 else 0.0

            val unebGrade = GradingService.calculateUnebGrade(subjectAverage)

            SubjectReportItem(
                subjectId = subId,
                subjectName = subName,
                subjectCode = subCode,
                paperScores = paperDetails,
                averageScore = (subjectAverage * 10.0).let { Math.round(it) / 10.0 },
                grade = unebGrade.label,
                aggregateValue = unebGrade.aggregate,
                teacherComment = unebGrade.description
            )
        }

        val totalMarks = subjectReportItems.sumOf { it.averageScore }
        val overallAverage = if (subjectReportItems.isNotEmpty()) totalMarks / subjectReportItems.size else 0.0

        // Calculate Best 8 Aggregates (UNEB O-Level standard)
        val best8Aggregate = if (subjectReportItems.size >= 8) {
            subjectReportItems.map { it.aggregateValue }.sorted().take(8).sum()
        } else if (subjectReportItems.isNotEmpty()) {
            subjectReportItems.sumOf { it.aggregateValue }
        } else null

        val division = GradingService.calculateDivision(best8Aggregate)

        // Stream Position Calculation
        val streamStudentIds = StudentsTable.selectAll()
            .where { StudentsTable.streamId eq streamId }
            .map { it[StudentsTable.id] }

        val studentTotalsInStream = streamStudentIds.map { sId ->
            val scores = (MarksTable innerJoin PapersTable)
                .selectAll()
                .where { (MarksTable.studentId eq sId) and (MarksTable.examTermId eq examTermId) }
                .map { it[MarksTable.score] }
            sId to scores.sum()
        }.sortedByDescending { it.second }

        val rank = studentTotalsInStream.indexOfFirst { it.first == studentId } + 1

        StudentReportCardResponse(
            studentId = studentId,
            fullName = "$firstName $lastName",
            admissionNumber = adminNo,
            className = className,
            streamName = streamName,
            examTermName = termName,
            year = year,
            term = term,
            subjects = subjectReportItems,
            totalMarks = (totalMarks * 10.0).let { Math.round(it) / 10.0 },
            overallAverage = (overallAverage * 10.0).let { Math.round(it) / 10.0 },
            best8Aggregate = best8Aggregate,
            division = division,
            positionInStream = if (rank > 0) rank else streamStudentIds.size,
            totalStudentsInStream = streamStudentIds.size,
            classTeacherComment = GradingService.generateClassTeacherComment(overallAverage),
            headteacherComment = GradingService.generateHeadteacherComment(division)
        )
    }
}