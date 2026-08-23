package com.maranatha.skools

import com.maranatha.skools.repository.RefreshTokenRepository
import com.maranatha.skools.repository.UserRepositoryImpl
import com.maranatha.skools.routes.authRoutes
import com.maranatha.skools.security.JwtService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.module() {
    val userRepository = UserRepositoryImpl()
    val refreshTokenRepository = RefreshTokenRepository()
    val jwtService = JwtService(
        secret = "your-secret-key",
        issuer = "http://0.0.0.0:8080/",
        audience = "http://0.0.0.0:8080/"
    )

    routing {
        authRoutes(userRepository, refreshTokenRepository, jwtService)
    }
}