package com.maranatha.skools

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.maranatha.skools.db.DatabaseFactory
import com.maranatha.skools.repository.AcademicRepository
import com.maranatha.skools.repository.GradingRepository
import com.maranatha.skools.repository.RefreshTokenRepository
import com.maranatha.skools.repository.ReportCardRepository
import com.maranatha.skools.repository.UserRepositoryImpl
import com.maranatha.skools.routes.academicRoutes
import com.maranatha.skools.routes.authRoutes
import com.maranatha.skools.routes.gradingRoutes
import com.maranatha.skools.routes.reportCardRoutes
import com.maranatha.skools.routes.userRoutes
import com.maranatha.skools.security.JwtService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()
    configureStaticContent()

    val jwtSecret = environment.config.propertyOrNull("jwt.secret")?.getString() ?: "skools-super-secret-key-change-in-production"
    val jwtIssuer = environment.config.propertyOrNull("jwt.issuer")?.getString() ?: "maranatha-skools-api"
    val jwtAudience = environment.config.propertyOrNull("jwt.audience")?.getString() ?: "maranatha-skools-users"

    val gradingRepository = GradingRepository()
    val userRepository = UserRepositoryImpl()
    val refreshTokenRepository = RefreshTokenRepository()
    val academicRepository = AcademicRepository()
    val reportCardRepository = ReportCardRepository()
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
        get("/") {
            call.respondText("Maranatha Schools API - Registry System")
        }

        get("/api-docs") {
            call.respondText("""
                # Maranatha Skools API Documentation
                
                ## Authentication
                Most endpoints require JWT authentication. Use `/api/v1/auth/login` to obtain tokens.
                
                ## Endpoints
                
                ### Auth Routes
                - POST `/api/v1/auth/register` - Register new user
                - POST `/api/v1/auth/login` - Login and get JWT tokens
                - POST `/api/v1/auth/refresh` - Refresh access token
                - POST `/api/v1/auth/logout` - Logout (revoke refresh token)
                
                ### Grading & Marks Routes
                - POST `/api/v1/grading/uce/evaluate` - Evaluate UCE student grades
                - GET `/api/v1/subjects` - Get all subjects
                - GET `/api/v1/subjects/{subjectId}/papers` - Get papers for a subject
                - GET `/api/v1/exam-terms` - Get all exam terms
                - POST `/api/v1/marks` - Enter single mark
                - POST `/api/v1/marks/batch` - Enter batch marks
                - GET `/api/v1/students/{studentId}/marks` - Get student marks
                - GET `/api/v1/students/{studentId}/uneb-summary` - Get UNEB summary
                
                ### Reports & Exports Routes
                - GET `/api/v1/report-cards/student/{studentId}` - Get student report card
                - GET `/api/v1/report-cards/student/{studentId}/pdf` - Generate PDF report card
                - GET `/api/v1/report-cards/stream/{streamId}/pdf-zip` - Generate batch PDF reports (ZIP)
                
                ### System Health
                - GET `/health` - Check system and database health
                
                ## Interactive API Documentation
                - GET `/swagger` - Interactive Swagger UI for API testing
                - GET `/openapi.json` - OpenAPI specification (JSON format)
                
                ## Authentication
                Include JWT token in Authorization header: `Bearer <token>`
                
                ## Response Format
                All responses use JSON format with appropriate HTTP status codes.
            """.trimIndent(), io.ktor.http.ContentType.Text.Plain)
        }

        get("/health") {
            try {
                val dbStatus = DatabaseFactory.checkDatabaseConnection()
                val status = if (dbStatus) "UP" else "DOWN"
                call.respond(mapOf(
                    "status" to status,
                    "database" to status,
                    "timestamp" to System.currentTimeMillis().toString()
                ))
            } catch (e: Exception) {
                call.respond(io.ktor.http.HttpStatusCode.ServiceUnavailable, mapOf(
                    "status" to "DOWN",
                    "database" to "DOWN",
                    "error" to (e.message ?: "Database connection failed"),
                    "timestamp" to System.currentTimeMillis().toString()
                ))
            }
        }

        authRoutes(userRepository, refreshTokenRepository, jwtService)
        userRoutes(userRepository)
        academicRoutes(academicRepository)
        gradingRoutes(gradingRepository)
        reportCardRoutes()
    }
}