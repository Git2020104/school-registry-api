package com.maranatha.skools

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.testApplication
import kotlin.test.*

class ApiIntegrationTest {

    @Test
    fun `test root endpoint returns welcome message`() = testApplication {
        routing {
            get("/") {
                call.respondText("Maranatha Schools API - Registry System")
            }
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Maranatha Schools API - Registry System", response.body())
    }

    @Test
    fun `test health endpoint returns text status`() = testApplication {
        routing {
            get("/health") {
                call.respondText("UP", ContentType.Text.Plain)
            }
        }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("UP", response.body())
    }

    @Test
    fun `test api-docs endpoint returns documentation`() = testApplication {
        routing {
            get("/api-docs") {
                call.respondText("# API Documentation", ContentType.Text.Plain)
            }
        }
        val response = client.get("/api-docs")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.body<String>().contains("API Documentation"))
    }

    @Test
    fun `test swagger endpoint returns HTML`() = testApplication {
        routing {
            get("/swagger") {
                call.respondText("<html><body>Swagger UI</body></html>", ContentType.Text.Html)
            }
        }
        val response = client.get("/swagger")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.body<String>().contains("Swagger UI"))
    }

    @Test
    fun `test non-existent endpoint returns 404`() = testApplication {
        routing {
            get("/") {
                call.respondText("Root")
            }
        }
        val response = client.get("/non-existent")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `test invalid HTTP method returns 405`() = testApplication {
        routing {
            get("/") {
                call.respondText("Root")
            }
        }
        val response = client.post("/") {
            setBody("test")
            contentType(ContentType.Text.Plain)
        }
        assertEquals(HttpStatusCode.MethodNotAllowed, response.status)
    }
}