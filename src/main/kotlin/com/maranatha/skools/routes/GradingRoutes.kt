package com.maranatha.skools.routes

import com.maranatha.skools.models.*
import com.maranatha.skools.repository.GradingRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.gradingRoutes(repository: GradingRepository) {
    authenticate("auth-jwt") {
        route("/api/v1") {
            // Subjects
            post("/subjects") {
                val req = call.receive<CreateSubjectRequest>()
                val subject = repository.createSubject(req)
                call.respond(HttpStatusCode.Created, subject)
            }

            get("/subjects") {
                call.respond(HttpStatusCode.OK, repository.getAllSubjects())
            }

            // Papers
            post("/papers") {
                val req = call.receive<CreatePaperRequest>()
                val paper = repository.createPaper(req)
                call.respond(HttpStatusCode.Created, paper)
            }

            get("/subjects/{subjectId}/papers") {
                val subjectId = call.parameters["subjectId"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid subject ID"))
                call.respond(HttpStatusCode.OK, repository.getPapersBySubject(subjectId))
            }

            // Exam Terms
            post("/exam-terms") {
                val req = call.receive<CreateExamTermRequest>()
                val term = repository.createExamTerm(req)
                call.respond(HttpStatusCode.Created, term)
            }

            get("/exam-terms") {
                call.respond(HttpStatusCode.OK, repository.getAllExamTerms())
            }

            // Marks Entry
            post("/marks") {
                val req = call.receive<EnterMarkRequest>()
                val entry = repository.enterSingleMark(req)
                call.respond(HttpStatusCode.Created, entry)
            }

            post("/marks/batch") {
                val req = call.receive<BatchMarkEntryRequest>()
                val entries = repository.enterBatchMarks(req)
                call.respond(HttpStatusCode.Created, entries)
            }

            get("/students/{studentId}/marks") {
                val studentId = call.parameters["studentId"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid student ID"))
                val termId = call.request.queryParameters["termId"]?.toIntOrNull()
                call.respond(HttpStatusCode.OK, repository.getMarksByStudent(studentId, termId))
            }
        }
    }
}