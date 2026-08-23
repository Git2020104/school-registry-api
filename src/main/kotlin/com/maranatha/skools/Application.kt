package com.maranatha.skools

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.maranatha.skools.db.DatabaseFactory
import com.maranatha.skools.repository.AcademicRepository
import com.maranatha.skools.repository.GradingRepository
import com.maranatha.skools.repository.RefreshTokenRepository
import com.maranatha.skools.repository.UserRepositoryImpl
import com.maranatha.skools.routes.academicRoutes
import com.maranatha.skools.routes.authRoutes
import com.maranatha.skools.routes.userRoutes
import com.maranatha.skools.routes.gradingRoutes
import com.maranatha.skools.security.JwtService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*


// Inside Application.module():

fun Application.module() {
    DatabaseFactory.init()

    val jwtSecret = "your-secret-key"
    val jwtIssuer = "http://0.0.0.0:8080/"
    val jwtAudience = "http://0.0.0.0:8080/"

    
    val gradingRepository = GradingRepository()
    val userRepository = UserRepositoryImpl()
    val refreshTokenRepository = RefreshTokenRepository()
    val academicRepository = AcademicRepository()
    val jwtService = JwtService(
        secret = jwtSecret,
        issuer = jwtIssuer,
        audience = jwtAudience
    )

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "skools-app"
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .withAudience(jwtAudience)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asInt() != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }

    routing {
        authRoutes(userRepository, refreshTokenRepository, jwtService)
        userRoutes(userRepository)
        academicRoutes(academicRepository)
        gradingRoutes(gradingRepository)
    }
}