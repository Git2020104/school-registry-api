package com.maranatha.skools.routes

import com.maranatha.skools.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userRepository: UserRepository) {
    authenticate("auth-jwt") {
        route("/api/users") {
            get {
                val users = userRepository.getAllUsers()
                call.respond(users)
            }

            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asInt()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val user = userRepository.getUserById(userId)
                call.respond(user)
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user ID format"))
                val user = userRepository.getUserById(id)
                call.respond(user)
            }
        }
    }
}