package com.maranatha.skools.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private const val SECRET = "skools-super-secret-key-change-in-production"
    private const val ISSUER = "com.maranatha.skools"
    private const val AUDIENCE = "skools-users"
    const val REALM = "SkoolsRegistry"
    private val ALGORITHM = Algorithm.HMAC256(SECRET)

    fun generateToken(userId: Int, email: String, role: String): String {
        return JWT.create()
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + 86_400_000)) // 24 hours
            .sign(ALGORITHM)
    }

    fun makeVerifier() = JWT.require(ALGORITHM)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()
}