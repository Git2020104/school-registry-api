package com.maranatha.skools.routes

import com.maranatha.skools.models.CreateClassRequest
import com.maranatha.skools.models.CreateStreamRequest
import com.maranatha.skools.models.RegisterStudentRequest
import com.maranatha.skools.repository.AcademicRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.academicRoutes(repository: AcademicRepository) {
    authenticate("auth-jwt") {
        route("/api/v1") {
            // Classes
            post("/classes") {
                val req = call.receive<CreateClassRequest>()
                val created = repository.createClass(req)
                call.respond(HttpStatusCode.Created, created)
            }

            get("/classes") {
                call.respond(HttpStatusCode.OK, repository.getAllClasses())
            }

            // Streams
            post("/streams") {
                val req = call.receive<CreateStreamRequest>()
                val created = repository.createStream(req)
                call.respond(HttpStatusCode.Created, created)
            }

            get("/classes/{classId}/streams") {
                val classId = call.parameters["classId"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid class ID"))
                call.respond(HttpStatusCode.OK, repository.getStreamsByClass(classId))
            }

            // Students
            post("/students") {
                val req = call.receive<RegisterStudentRequest>()
                val student = repository.registerStudent(req)
                call.respond(HttpStatusCode.Created, student)
            }

            get("/students") {
                val classId = call.request.queryParameters["classId"]?.toIntOrNull()
                val students = if (classId != null) {
                    repository.getStudentsByClass(classId)
                } else {
                    repository.getAllStudents()
                }
                call.respond(HttpStatusCode.OK, students)
            }
        }
    }
}