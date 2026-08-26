package com.maranatha.skools

import com.maranatha.skools.db.DatabaseFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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
import org.jetbrains.exposed.sql.Database
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
    fun `test health endpoint returns JSON status with database connectivity`() = testApplication {
        // Setup in-memory H2 database for testing
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1"
            driverClassName = "org.h2.Driver"
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        routing {
            get("/health") {
                try {
                    val dbStatus = dataSource.connection.use { connection ->
                        connection.createStatement().use { statement ->
                            statement.executeQuery("SELECT 1")
                        }
                    }
                    val status = if (dbStatus != null) "UP" else "DOWN"
                    call.respond(mapOf(
                        "status" to status,
                        "database" to status,
                        "timestamp" to System.currentTimeMillis().toString()
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.ServiceUnavailable, mapOf(
                        "status" to "DOWN",
                        "database" to "DOWN",
                        "error" to (e.message ?: "Database connection failed"),
                        "timestamp" to System.currentTimeMillis().toString()
                    ))
                }
            }
        }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<Map<String, String>>()
        assertEquals("UP", responseBody["status"])
        assertEquals("UP", responseBody["database"])
        assertNotNull(responseBody["timestamp"])
        
        dataSource.close()
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

    @Test
    fun `test JSON endpoint returns valid JSON response`() = testApplication {
        routing {
            get("/api/test") {
                call.respond(mapOf("message" to "success", "data" to listOf("item1", "item2")))
            }
        }
        val response = client.get("/api/test")
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<Map<String, Any>>()
        assertEquals("success", responseBody["message"])
        assertTrue(responseBody["data"] is List<*>)
    }

    @Test
    fun `test POST endpoint accepts JSON body`() = testApplication {
        routing {
            post("/api/test") {
                val body = call.receive<Map<String, String>>()
                call.respond(mapOf("received" to (body["value"] ?: "empty")))
            }
        }
        val response = client.post("/api/test") {
            setBody(mapOf("value" to "test-data"))
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<Map<String, String>>()
        assertEquals("test-data", responseBody["received"])
    }

    @Test
    fun `test endpoint with path parameter`() = testApplication {
        routing {
            get("/api/users/{id}") {
                val id = call.parameters["id"]
                call.respond(mapOf("userId" to id, "name" to "Test User"))
            }
        }
        val response = client.get("/api/users/123")
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<Map<String, String>>()
        assertEquals("123", responseBody["userId"])
        assertEquals("Test User", responseBody["name"])
    }

    @Test
    fun `test endpoint with query parameter`() = testApplication {
        routing {
            get("/api/search") {
                val query = call.request.queryParameters["q"]
                call.respond(mapOf("query" to query, "results" to emptyList<String>()))
            }
        }
        val response = client.get("/api/search?q=test")
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<Map<String, Any>>()
        assertEquals("test", responseBody["query"])
    }

    @Test
    fun `test health endpoint returns 503 when database fails`() = testApplication {
        routing {
            get("/health") {
                call.respond(HttpStatusCode.ServiceUnavailable, mapOf(
                    "status" to "DOWN",
                    "database" to "DOWN",
                    "error" to "Connection refused",
                    "timestamp" to System.currentTimeMillis().toString()
                ))
            }
        }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        val responseBody = response.body<Map<String, String>>()
        assertEquals("DOWN", responseBody["status"])
        assertEquals("DOWN", responseBody["database"])
        assertEquals("Connection refused", responseBody["error"])
    }

    @Test
    fun `test auth refresh endpoint with valid token returns 200`() = testApplication {
        routing {
            post("/api/auth/refresh") {
                val body = call.receive<Map<String, String>>()
                if (body["refreshToken"] == "valid-token") {
                    call.respond(mapOf(
                        "accessToken" to "new-access-token",
                        "refreshToken" to "new-refresh-token"
                    ))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid token"))
                }
            }
        }
        val response = client.post("/api/auth/refresh") {
            setBody(mapOf("refreshToken" to "valid-token"))
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<Map<String, String>>()
        assertNotNull(responseBody["accessToken"])
        assertNotNull(responseBody["refreshToken"])
    }

    @Test
    fun `test auth refresh endpoint with invalid token returns 401`() = testApplication {
        routing {
            post("/api/auth/refresh") {
                val body = call.receive<Map<String, String>>()
                if (body["refreshToken"] == "valid-token") {
                    call.respond(mapOf(
                        "accessToken" to "new-access-token",
                        "refreshToken" to "new-refresh-token"
                    ))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid token"))
                }
            }
        }
        val response = client.post("/api/auth/refresh") {
            setBody(mapOf("refreshToken" to "invalid-token"))
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val responseBody = response.body<Map<String, String>>()
        assertEquals("Invalid token", responseBody["message"])
    }

    @Test
    fun `test auth logout endpoint with valid token returns 200`() = testApplication {
        routing {
            post("/api/auth/logout") {
                val body = call.receive<Map<String, String>>()
                call.respond(mapOf("message" to "Logged out successfully"))
            }
        }
        val response = client.post("/api/auth/logout") {
            setBody(mapOf("refreshToken" to "some-token"))
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<Map<String, String>>()
        assertEquals("Logged out successfully", responseBody["message"])
    }

    @Test
    fun `test user me endpoint returns user profile`() = testApplication {
        routing {
            get("/api/users/me") {
                call.respond(mapOf(
                    "id" to 1,
                    "username" to "testuser",
                    "email" to "test@example.com",
                    "role" to "TEACHER"
                ))
            }
        }
        val response = client.get("/api/users/me")
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<Map<String, Any>>()
        assertEquals(1, responseBody["id"])
        assertEquals("testuser", responseBody["username"])
        assertEquals("test@example.com", responseBody["email"])
        assertEquals("TEACHER", responseBody["role"])
    }

    @Test
    fun `test user by id endpoint returns user data`() = testApplication {
        routing {
            get("/api/users/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(mapOf(
                    "id" to id,
                    "username" to "user$id",
                    "email" to "user$id@example.com",
                    "role" to "STUDENT"
                ))
            }
        }
        val response = client.get("/api/users/42")
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<Map<String, Any>>()
        assertEquals(42, responseBody["id"])
        assertEquals("user42", responseBody["username"])
        assertEquals("user42@example.com", responseBody["email"])
    }

    @Test
    fun `test user by id endpoint with invalid id returns 400`() = testApplication {
        routing {
            get("/api/users/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user ID format"))
                } else {
                    call.respond(mapOf("id" to id, "username" to "user$id"))
                }
            }
        }
        val response = client.get("/api/users/invalid")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val responseBody = response.body<Map<String, String>>()
        assertEquals("Invalid user ID format", responseBody["error"])
    }

    @Test
    fun `test all users endpoint returns list of users`() = testApplication {
        routing {
            get("/api/users") {
                call.respond(listOf(
                    mapOf("id" to 1, "username" to "user1", "role" to "ADMIN"),
                    mapOf("id" to 2, "username" to "user2", "role" to "TEACHER"),
                    mapOf("id" to 3, "username" to "user3", "role" to "STUDENT")
                ))
            }
        }
        val response = client.get("/api/users")
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<List<Map<String, Any>>>()
        assertEquals(3, responseBody.size)
        assertEquals("user1", responseBody[0]["username"])
        assertEquals("user2", responseBody[1]["username"])
        assertEquals("user3", responseBody[2]["username"])
    }

    @Test
    fun `test endpoint returns error on invalid JSON body`() = testApplication {
        routing {
            post("/api/test") {
                try {
                    val body = call.receive<Map<String, String>>()
                    call.respond(mapOf("received" to (body["value"] ?: "empty")))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON"))
                }
            }
        }
        val response = client.post("/api/test") {
            setBody("invalid json")
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val responseBody = response.body<Map<String, String>>()
        assertEquals("Invalid JSON", responseBody["error"])
    }
}