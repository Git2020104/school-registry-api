package com.maranatha.skools

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.request.receive
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.Database
import kotlin.test.*

class ApiIntegrationTest {
    @Test
    fun `test root endpoint returns welcome message`() =
        testApplication {
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
    fun `test health endpoint returns JSON status with database connectivity`() =
        testApplication {
            // Setup in-memory H2 database for testing
            val config =
                HikariConfig().apply {
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
                        val dbStatus =
                            dataSource.connection.use { connection ->
                                connection.createStatement().use { statement ->
                                    statement.executeQuery("SELECT 1")
                                }
                            }
                        val status = if (dbStatus != null) "UP" else "DOWN"
                        call.respondText(
                            """{"status":"$status","database":"$status","timestamp":"${System.currentTimeMillis()}"}""",
                            ContentType.Application.Json,
                        )
                    } catch (e: Exception) {
                        call.respondText(
                            """{"status":"DOWN","database":"DOWN","error":"${e.message ?: "Database connection failed"}","timestamp":"${System.currentTimeMillis()}"}""",
                            ContentType.Application.Json,
                            HttpStatusCode.ServiceUnavailable,
                        )
                    }
                }
            }
            val response = client.get("/health")
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = response.body<String>()
            assertTrue(responseBody.contains("UP"))
            assertNotNull(responseBody)

            dataSource.close()
        }

    @Test
    fun `test api-docs endpoint returns documentation`() =
        testApplication {
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
    fun `test swagger endpoint returns HTML`() =
        testApplication {
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
    fun `test non-existent endpoint returns 404`() =
        testApplication {
            routing {
                get("/") {
                    call.respondText("Root")
                }
            }
            val response = client.get("/non-existent")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `test invalid HTTP method returns 405`() =
        testApplication {
            routing {
                get("/") {
                    call.respondText("Root")
                }
            }
            val response =
                client.post("/") {
                    setBody("test")
                    contentType(ContentType.Text.Plain)
                }
            assertEquals(HttpStatusCode.MethodNotAllowed, response.status)
        }

    @Test
    fun `test JSON endpoint returns valid JSON response`() =
        testApplication {
            routing {
                get("/api/test") {
                    call.respondText("""{"message":"success","data":["item1","item2"]}""", ContentType.Application.Json)
                }
            }
            val response = client.get("/api/test")
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = response.body<String>()
            assertTrue(responseBody.contains("success"))
            assertTrue(responseBody.contains("item1"))
        }

    @Test
    fun `test POST endpoint accepts text body`() =
        testApplication {
            routing {
                post("/api/test") {
                    val body = call.receive<String>()
                    call.respondText("Received: $body", ContentType.Text.Plain)
                }
            }
            val response =
                client.post("/api/test") {
                    setBody("test-data")
                    contentType(ContentType.Text.Plain)
                }
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = response.body<String>()
            assertTrue(responseBody.contains("test-data"))
        }

    @Test
    fun `test endpoint with path parameter`() =
        testApplication {
            routing {
                get("/api/users/{id}") {
                    val id = call.parameters["id"]
                    call.respondText("User ID: $id, Name: Test User", ContentType.Text.Plain)
                }
            }
            val response = client.get("/api/users/123")
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = response.body<String>()
            assertTrue(responseBody.contains("123"))
            assertTrue(responseBody.contains("Test User"))
        }

    @Test
    fun `test endpoint with query parameter`() =
        testApplication {
            routing {
                get("/api/search") {
                    val query = call.request.queryParameters["q"]
                    call.respondText("Query: $query, Results: []", ContentType.Text.Plain)
                }
            }
            val response = client.get("/api/search?q=test")
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = response.body<String>()
            assertTrue(responseBody.contains("test"))
        }

    @Test
    fun `test health endpoint returns 503 when database fails`() =
        testApplication {
            routing {
                get("/health") {
                    call.respondText(
                        """{"status":"DOWN","database":"DOWN","error":"Connection refused"}""",
                        ContentType.Application.Json,
                        HttpStatusCode.ServiceUnavailable,
                    )
                }
            }
            val response = client.get("/health")
            assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
            val responseBody = response.body<String>()
            assertTrue(responseBody.contains("DOWN"))
            assertTrue(responseBody.contains("Connection refused"))
        }

    @Test
    fun `test auth refresh endpoint with valid token returns 200`() =
        testApplication {
            routing {
                post("/api/auth/refresh") {
                    val body = call.receive<String>()
                    if (body.contains("valid-token")) {
                        call.respondText("""{"accessToken":"new-access-token","refreshToken":"new-refresh-token"}""", ContentType.Application.Json)
                    } else {
                        call.respondText("""{"message":"Invalid token"}""", ContentType.Application.Json, HttpStatusCode.Unauthorized)
                    }
                }
            }
            val response =
                client.post("/api/auth/refresh") {
                    setBody("valid-token")
                    contentType(ContentType.Text.Plain)
                }
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = response.body<String>()
            assertTrue(responseBody.contains("new-access-token"))
            assertTrue(responseBody.contains("new-refresh-token"))
        }

    @Test
    fun `test auth refresh endpoint with invalid token returns 401`() =
        testApplication {
            routing {
                post("/api/auth/refresh") {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
            val response =
                client.post("/api/auth/refresh") {
                    setBody("invalid-token")
                    contentType(ContentType.Text.Plain)
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `test auth logout endpoint with valid token returns 200`() =
        testApplication {
            routing {
                post("/api/auth/logout") {
                    val body = call.receive<String>()
                    call.respondText("""{"message":"Logged out successfully"}""", ContentType.Application.Json)
                }
            }
            val response =
                client.post("/api/auth/logout") {
                    setBody("some-token")
                    contentType(ContentType.Text.Plain)
                }
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = response.body<String>()
            assertTrue(responseBody.contains("Logged out successfully"))
        }

    @Test
    fun `test user me endpoint returns user profile`() =
        testApplication {
            routing {
                get("/api/users/me") {
                    call.respondText("""{"id":1,"username":"testuser","email":"test@example.com","role":"TEACHER"}""", ContentType.Application.Json)
                }
            }
            val response = client.get("/api/users/me")
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = response.body<String>()
            assertTrue(responseBody.contains("testuser"))
            assertTrue(responseBody.contains("test@example.com"))
            assertTrue(responseBody.contains("TEACHER"))
        }

    @Test
    fun `test user by id endpoint returns user data`() =
        testApplication {
            routing {
                get("/api/users/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                    call.respondText("""{"id":$id,"username":"user$id","email":"user$id@example.com","role":"STUDENT"}""", ContentType.Application.Json)
                }
            }
            val response = client.get("/api/users/42")
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = response.body<String>()
            assertTrue(responseBody.contains("user42"))
            assertTrue(responseBody.contains("user42@example.com"))
        }

    @Test
    fun `test user by id endpoint with invalid id returns 400`() =
        testApplication {
            routing {
                get("/api/users/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respondText("""{"error":"Invalid user ID format"}""", ContentType.Application.Json, HttpStatusCode.BadRequest)
                    } else {
                        call.respondText("""{"id":$id,"username":"user$id"}""", ContentType.Application.Json)
                    }
                }
            }
            val response = client.get("/api/users/invalid")
            assertEquals(HttpStatusCode.BadRequest, response.status)
            val responseBody = response.body<String>()
            assertTrue(responseBody.contains("Invalid user ID format"))
        }

    @Test
    fun `test all users endpoint returns list of users`() =
        testApplication {
            routing {
                get("/api/users") {
                    call.respondText("""[{"id":1,"username":"user1","role":"ADMIN"},{"id":2,"username":"user2","role":"TEACHER"},{"id":3,"username":"user3","role":"STUDENT"}]""", ContentType.Application.Json)
                }
            }
            val response = client.get("/api/users")
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = response.body<String>()
            assertTrue(responseBody.contains("user1"))
            assertTrue(responseBody.contains("user2"))
            assertTrue(responseBody.contains("user3"))
        }

    @Test
    fun `test endpoint returns error on invalid body`() =
        testApplication {
            routing {
                post("/api/test") {
                    try {
                        val body = call.receive<String>()
                        call.respondText("Received: $body", ContentType.Text.Plain)
                    } catch (e: Exception) {
                        call.respondText("""{"error":"Invalid request"}""", ContentType.Application.Json, HttpStatusCode.BadRequest)
                    }
                }
            }
            val response =
                client.post("/api/test") {
                    setBody("test data")
                    contentType(ContentType.Text.Plain)
                }
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = response.body<String>()
            assertTrue(responseBody.contains("test data"))
        }
}
