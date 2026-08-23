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

@Serializable
data class MessageResponse(val message: String)

fun Route.authRoutes(
    userRepository: UserRepository,
    refreshTokenRepository: RefreshTokenRepository,
    jwtService: JwtService
) {
    route("/api/auth") {

        // Refresh Token Rotation
        post("/refresh") {
            val request = runCatching { call.receive<RefreshRequest>() }.getOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid request body"))

            // 1. Check database for active token
            if (!refreshTokenRepository.isValid(request.refreshToken)) {
                return@post call.respond(HttpStatusCode.Unauthorized, MessageResponse("Invalid, expired, or revoked refresh token"))
            }

            // 2. Decode user ID from token
            val userId = jwtService.getUserIdFromToken(request.refreshToken)
                ?: return@post call.respond(HttpStatusCode.Unauthorized, MessageResponse("Invalid token payload"))

            // 3. Fetch user details to get current role
            val user = try {
                userRepository.getUserById(userId)
            } catch (e: UserNotFoundException) {
                return@post call.respond(HttpStatusCode.NotFound, MessageResponse("User no longer exists"))
            }

            // 4. Revoke used refresh token (Rotation)
            refreshTokenRepository.revokeToken(request.refreshToken)

            // 5. Generate new token pair
            val newAccessToken = jwtService.generateAccessToken(user.id, user.role)
            val (newRefreshToken, expiresAt) = jwtService.generateRefreshToken(user.id)

            // 6. Save new refresh token into DB
            refreshTokenRepository.saveToken(user.id, newRefreshToken, expiresAt)

            call.respond(HttpStatusCode.OK, TokenResponse(newAccessToken, newRefreshToken))
        }

        // Revoke Token Endpoint
        post("/logout") {
            val request = runCatching { call.receive<RefreshRequest>() }.getOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, MessageResponse("Invalid request body"))

            val revoked = refreshTokenRepository.revokeToken(request.refreshToken)
            if (revoked) {
                call.respond(HttpStatusCode.OK, MessageResponse("Logged out successfully"))
            } else {
                call.respond(HttpStatusCode.OK, MessageResponse("Token was already revoked or invalid"))
            }
        }
    }
}