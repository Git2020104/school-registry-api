package com.maranatha.skools

import com.maranatha.skools.db.DatabaseFactory
import com.maranatha.skools.plugins.configureSecurity
import com.maranatha.skools.plugins.configureStatusPages
import com.maranatha.skools.repository.UserRepositoryImpl
import com.maranatha.skools.routes.authRoutes
import com.maranatha.skools.routes.userRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    configureSecurity()
    configureStatusPages()
    DatabaseFactory.init()

    val userRepository = UserRepositoryImpl()

    routing {
        get("/") {
            call.respondText("School Registry API is live!")
        }
        authRoutes(userRepository)
        userRoutes(userRepository)
    }
}