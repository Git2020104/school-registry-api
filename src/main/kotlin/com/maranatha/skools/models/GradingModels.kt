package com.maranatha.skools.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

object SubjectsTable : Table("subjects") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val code = varchar("code", 20)
    val level = varchar("level", 20).default("O_LEVEL")

    override val primaryKey = PrimaryKey(id)
}

object PapersTable : Table("papers") {
    val id = integer("id").autoIncrement()
    val subjectId = integer("subject_id").references(SubjectsTable.id)
    val paperNumber = integer("paper_number")
    val name = varchar("name", 100)
    val maxMarks = integer("max_marks").default(100)

    override val primaryKey = PrimaryKey(id)
}

object ExamTermsTable : Table("exam_terms") {
    val id = integer("id").autoIncrement()
    val year = integer("year")
    val term = integer("term")
    val name = varchar("name", 100)
    val isActive = bool("is_active").default(true)

    override val primaryKey = PrimaryKey(id)
}

object MarksTable : Table("marks") {
    val id = integer("id").autoIncrement()
    val studentId = integer("student_id").references(StudentsTable.id)
    val paperId = integer("paper_id").references(PapersTable.id)
    val examTermId = integer("exam_term_id").references(ExamTermsTable.id)
    val score = double("score")
    val remarks = varchar("remarks", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class Subject(val id: Int, val name: String, val code: String, val level: String)

@Serializable
data class CreateSubjectRequest(val name: String, val code: String, val level: String = "O_LEVEL")

@Serializable
data class Paper(val id: Int, val subjectId: Int, val paperNumber: Int, val name: String, val maxMarks: Int)

@Serializable
data class CreatePaperRequest(val subjectId: Int, val paperNumber: Int, val name: String, val maxMarks: Int = 100)

@Serializable
data class ExamTerm(val id: Int, val year: Int, val term: Int, val name: String, val isActive: Boolean)

@Serializable
data class CreateExamTermRequest(val year: Int, val term: Int, val name: String)

@Serializable
data class MarkEntry(val id: Int, val studentId: Int, val paperId: Int, val examTermId: Int, val score: Double, val remarks: String? = null)

@Serializable
data class EnterMarkRequest(val studentId: Int, val paperId: Int, val examTermId: Int, val score: Double, val remarks: String? = null)

@Serializable
data class BatchMarkEntryRequest(val paperId: Int, val examTermId: Int, val entries: List<StudentScoreItem>)

@Serializable
data class StudentScoreItem(val studentId: Int, val score: Double, val remarks: String? = null)