package com.maranatha.skools.plugins

import com.maranatha.skools.exceptions.UserAlreadyExistsException
import com.maranatha.skools.exceptions.UserNotFoundException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        // Handle 409 Conflict (Duplicate email)
        exception<UserAlreadyExistsException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                mapOf("error" to cause.message)
            )
        }

        // Handle 404 Not Found
        exception<UserNotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to cause.message)
            )
        }

        // Global fallback for unhandled exceptions (500)
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.localizedMessage ?: "An unexpected error occurred."))
            )
        }
    }
}