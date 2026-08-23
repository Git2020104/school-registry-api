package com.maranatha.skools.routes

import com.maranatha.skools.exceptions.UserNotFoundException
import com.maranatha.skools.repository.RefreshTokenRepository
import com.maranatha.skools.repository.UserRepository
import com.maranatha.skools.security.JwtService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class TokenResponse(val accessToken: String, val refreshToken: String)

fun Route.authRoutes(
    userRepository: UserRepository,
    refreshTokenRepository: RefreshTokenRepository,
    jwtService: JwtService
) {
    route("/api/auth") {

        post("/refresh") {
            val request = call.receiveNullable<RefreshRequest>()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing refresh token"))

            if (!refreshTokenRepository.isValid(request.refreshToken)) {
                return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or revoked refresh token"))
            }

            val userId = jwtService.getUserIdFromToken(request.refreshToken)
                ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Malformed refresh token"))

            val user = try {
                userRepository.getUserById(userId)
            } catch (e: UserNotFoundException) {
                return@post call.respond(HttpStatusCode.NotFound, mapOf("error" to "User no longer exists"))
            }

            refreshTokenRepository.revokeToken(request.refreshToken)

            val newAccessToken = jwtService.generateAccessToken(user.id, user.role)
            val (newRefreshToken, expiresAt) = jwtService.generateRefreshToken(user.id)
            refreshTokenRepository.saveToken(user.id, newRefreshToken, expiresAt)

            call.respond(TokenResponse(newAccessToken, newRefreshToken))
        }

        post("/logout") {
            val request = call.receiveNullable<RefreshRequest>()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing refresh token"))

            val revoked = refreshTokenRepository.revokeToken(request.refreshToken)
            if (revoked) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Successfully logged out"))
            } else {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Token already invalid or non-existent"))
            }
        }
    }
}