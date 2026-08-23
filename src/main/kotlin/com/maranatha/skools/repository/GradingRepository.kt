package com.maranatha.skools.repository

import com.maranatha.skools.db.DatabaseFactory.dbQuery
import com.maranatha.skools.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and

class GradingRepository {

    // Subjects
    suspend fun createSubject(req: CreateSubjectRequest): Subject = dbQuery {
        val id = SubjectsTable.insert {
            it[name] = req.name
            it[code] = req.code
            it[level] = req.level
        } get SubjectsTable.id

        Subject(id, req.name, req.code, req.level)
    }

    suspend fun getAllSubjects(): List<Subject> = dbQuery {
        SubjectsTable.selectAll().map {
            Subject(it[SubjectsTable.id], it[SubjectsTable.name], it[SubjectsTable.code], it[SubjectsTable.level])
        }
    }

    // Papers
    suspend fun createPaper(req: CreatePaperRequest): Paper = dbQuery {
        val id = PapersTable.insert {
            it[subjectId] = req.subjectId
            it[paperNumber] = req.paperNumber
            it[name] = req.name
            it[maxMarks] = req.maxMarks
        } get PapersTable.id

        Paper(id, req.subjectId, req.paperNumber, req.name, req.maxMarks)
    }

    suspend fun getPapersBySubject(subjectId: Int): List<Paper> = dbQuery {
        PapersTable.selectAll().where { PapersTable.subjectId eq subjectId }.map {
            Paper(
                it[PapersTable.id],
                it[PapersTable.subjectId],
                it[PapersTable.paperNumber],
                it[PapersTable.name],
                it[PapersTable.maxMarks]
            )
        }
    }

    // Exam Terms
    suspend fun createExamTerm(req: CreateExamTermRequest): ExamTerm = dbQuery {
        val id = ExamTermsTable.insert {
            it[year] = req.year
            it[term] = req.term
            it[name] = req.name
            it[isActive] = true
        } get ExamTermsTable.id

        ExamTerm(id, req.year, req.term, req.name, true)
    }

    suspend fun getAllExamTerms(): List<ExamTerm> = dbQuery {
        ExamTermsTable.selectAll().map {
            ExamTerm(
                it[ExamTermsTable.id],
                it[ExamTermsTable.year],
                it[ExamTermsTable.term],
                it[ExamTermsTable.name],
                it[ExamTermsTable.isActive]
            )
        }
    }

    // Marks
    suspend fun enterSingleMark(req: EnterMarkRequest): MarkEntry = dbQuery {
        val existingId = MarksTable.selectAll().where {
            (MarksTable.studentId eq req.studentId) and
            (MarksTable.paperId eq req.paperId) and
            (MarksTable.examTermId eq req.examTermId)
        }.map { it[MarksTable.id] }.singleOrNull()

        if (existingId != null) {
            MarksTable.update({ MarksTable.id eq existingId }) {
                it[score] = req.score
                it[remarks] = req.remarks
            }
            MarkEntry(existingId, req.studentId, req.paperId, req.examTermId, req.score, req.remarks)
        } else {
            val newId = MarksTable.insert {
                it[studentId] = req.studentId
                it[paperId] = req.paperId
                it[examTermId] = req.examTermId
                it[score] = req.score
                it[remarks] = req.remarks
            } get MarksTable.id

            MarkEntry(newId, req.studentId, req.paperId, req.examTermId, req.score, req.remarks)
        }
    }

    suspend fun enterBatchMarks(req: BatchMarkEntryRequest): List<MarkEntry> = dbQuery {
        req.entries.map { item ->
            val existingId = MarksTable.selectAll().where {
                (MarksTable.studentId eq item.studentId) and
                (MarksTable.paperId eq req.paperId) and
                (MarksTable.examTermId eq req.examTermId)
            }.map { it[MarksTable.id] }.singleOrNull()

            if (existingId != null) {
                MarksTable.update({ MarksTable.id eq existingId }) {
                    it[score] = item.score
                    it[remarks] = item.remarks
                }
                MarkEntry(existingId, item.studentId, req.paperId, req.examTermId, item.score, item.remarks)
            } else {
                val newId = MarksTable.insert {
                    it[studentId] = item.studentId
                    it[paperId] = req.paperId
                    it[examTermId] = req.examTermId
                    it[score] = item.score
                    it[remarks] = item.remarks
                } get MarksTable.id

                MarkEntry(newId, item.studentId, req.paperId, req.examTermId, item.score, item.remarks)
            }
        }
    }

    suspend fun getMarksByStudent(studentId: Int, examTermId: Int?): List<MarkEntry> = dbQuery {
        val query = if (examTermId != null) {
            MarksTable.selectAll().where { (MarksTable.studentId eq studentId) and (MarksTable.examTermId eq examTermId) }
        } else {
            MarksTable.selectAll().where { MarksTable.studentId eq studentId }
        }

        query.map {
            MarkEntry(
                it[MarksTable.id],
                it[MarksTable.studentId],
                it[MarksTable.paperId],
                it[MarksTable.examTermId],
                it[MarksTable.score],
                it[MarksTable.remarks]
            )
        }
    }
}