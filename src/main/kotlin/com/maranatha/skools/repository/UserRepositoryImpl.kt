package com.maranatha.skools.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import com.maranatha.skools.dto.*
import com.maranatha.skools.exceptions.UserAlreadyExistsException
import com.maranatha.skools.exceptions.UserNotFoundException
import com.maranatha.skools.models.UsersTable
import com.maranatha.skools.security.JwtConfig
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserRepositoryImpl : UserRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun getAllUsers(): List<UserResponse> = dbQuery {
        UsersTable.selectAll().map { row ->
            UserResponse(
                id = row[UsersTable.id],
                fullName = row[UsersTable.fullName],
                email = row[UsersTable.email],
                role = row[UsersTable.role]
            )
        }
    }

    override suspend fun getUserById(id: Int): UserResponse = dbQuery {
        UsersTable.select { UsersTable.id eq id }
            .map { row ->
                UserResponse(
                    id = row[UsersTable.id],
                    fullName = row[UsersTable.fullName],
                    email = row[UsersTable.email],
                    role = row[UsersTable.role]
                )
            }
            .singleOrNull() ?: throw UserNotFoundException(id)
    }

    override suspend fun registerUser(request: RegisterRequest): AuthResponse = dbQuery {
        val hashedPassword = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())

        val newId = try {
            UsersTable.insert { row ->
                row[fullName] = request.fullName
                row[email] = request.email
                row[passwordHash] = hashedPassword
                row[role] = request.role
            } get UsersTable.id
        } catch (e: ExposedSQLException) {
            throw UserAlreadyExistsException(request.email)
        }

        val token = JwtConfig.generateToken(newId, request.email, request.role)
        val userResponse = UserResponse(newId, request.fullName, request.email, request.role)

        AuthResponse(token, userResponse)
    }

    override suspend fun loginUser(request: LoginRequest): AuthResponse = dbQuery {
        val row = UsersTable.select { UsersTable.email eq request.email }
            .singleOrNull()
            ?: throw IllegalArgumentException("Invalid email or password.")

        val storedHash = row[UsersTable.passwordHash]
        val result = BCrypt.verifyer().verify(request.password.toCharArray(), storedHash)

        if (!result.verified) {
            throw IllegalArgumentException("Invalid email or password.")
        }

        val userId = row[UsersTable.id]
        val role = row[UsersTable.role]
        val token = JwtConfig.generateToken(userId, request.email, role)
        val userResponse = UserResponse(userId, row[UsersTable.fullName], request.email, role)

        AuthResponse(token, userResponse)
    }
}