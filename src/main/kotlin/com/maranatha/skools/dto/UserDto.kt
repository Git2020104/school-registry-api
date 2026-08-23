package com.maranatha.skools.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserRequest(
    val fullName: String,
    val email: String,
    val role: String
)

@Serializable
data class UserResponse(
    val id: Int,
    val fullName: String,
    val email: String,
    val role: String
)

@Serializable
data class CreateUserResponse(
    val id: Int,
    val message: String
)