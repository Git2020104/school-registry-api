package com.maranatha.skools.routes

import com.maranatha.skools.dto.LoginRequest
import com.maranatha.skools.dto.RegisterRequest
import com.maranatha.skools.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(userRepository: UserRepository) {
    route("/api/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val response = userRepository.registerUser(request)
            call.respond(HttpStatusCode.Created, response)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val response = userRepository.loginUser(request)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}