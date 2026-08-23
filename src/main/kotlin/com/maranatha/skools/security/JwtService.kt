package com.maranatha.skools.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

class JwtService(
    private val secret: String,
    private val issuer: String,
    private val audience: String
) {
    private val algorithm = Algorithm.HMAC256(secret)

    fun generateAccessToken(userId: Int, role: String): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 minutes
            .sign(algorithm)
    }

    fun generateRefreshToken(userId: Int): Pair<String, Long> {
        val expiresAtMillis = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L // 7 days
        val token = JWT.create()
            .withSubject("Refresh")
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
            .withExpiresAt(Date(expiresAtMillis))
            .sign(algorithm)
        
        return Pair(token, expiresAtMillis)
    }

    fun getUserIdFromToken(token: String): Int? {
        return try {
            val verifier = JWT.require(algorithm).withIssuer(issuer).build()
            val decoded = verifier.verify(token)
            decoded.getClaim("userId").asInt()
        } catch (e: Exception) {
            null
        }
    }
}