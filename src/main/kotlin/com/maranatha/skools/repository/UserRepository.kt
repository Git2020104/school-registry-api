package com.maranatha.skools.repository

import com.maranatha.skools.dto.*

interface UserRepository {
    suspend fun getAllUsers(): List<UserResponse>
    suspend fun getUserById(id: Int): UserResponse
    suspend fun registerUser(request: RegisterRequest): AuthResponse
    suspend fun loginUser(request: LoginRequest): AuthResponse
}