package com.maranatha.skools

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureStaticContent() {
    routing {
        // Serve OpenAPI JSON specification
        get("/openapi.json") {
            val openApiFile = Thread.currentThread().contextClassLoader
                .getResourceAsStream("openapi.json")
            if (openApiFile != null) {
                val content = openApiFile.readAllBytes()
                call.respondBytes(content, io.ktor.http.ContentType.Application.Json)
            } else {
                call.respondText("OpenAPI specification not found", status = io.ktor.http.HttpStatusCode.NotFound)
            }
        }
        
        // Serve static files from resources/static
        staticResources("/static", "static")
        
        // Serve Swagger UI at /swagger (index page)
        get("/swagger") {
            val swaggerIndex = Thread.currentThread().contextClassLoader
                .getResourceAsStream("static/swagger/index.html")
            if (swaggerIndex != null) {
                val content = swaggerIndex.readAllBytes()
                call.respondBytes(content, io.ktor.http.ContentType.Text.Html)
            } else {
                call.respondText("Swagger UI not found", status = io.ktor.http.HttpStatusCode.NotFound)
            }
        }
        
        // Serve other Swagger UI static assets
        staticResources("/swagger", "static/swagger")
    }
}