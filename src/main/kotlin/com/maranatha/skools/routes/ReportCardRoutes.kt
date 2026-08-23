package com.maranatha.skools.routes

import com.maranatha.skools.repository.ReportCardRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.reportCardRoutes() {
    val repository = ReportCardRepository()

    authenticate("auth-jwt") {
        route("/api/v1/report-cards") {

            get("/student/{studentId}") {
                val studentId = call.parameters["studentId"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid or missing studentId"))

                val termId = call.request.queryParameters["termId"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Query parameter 'termId' is required"))

                try {
                    val reportCard = repository.generateStudentReportCard(studentId, termId)
                    call.respond(HttpStatusCode.OK, reportCard)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to (e.message ?: "Resource not found")))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Failed to generate report card")))
                }
            }
        }
    }
}