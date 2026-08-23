package com.maranatha.skools.repository

import com.maranatha.skools.db.RefreshTokens
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class RefreshTokenRepository {
    fun saveToken(userId: Int, token: String, expiresAt: Long) = transaction {
        RefreshTokens.insert {
            it[this.userId] = userId
            it[this.token] = token
            it[this.expiresAt] = expiresAt
            it[this.isRevoked] = false
        }
    }

    fun isValid(token: String): Boolean = transaction {
        RefreshTokens.selectAll()
            .where { (RefreshTokens.token eq token) and (RefreshTokens.isRevoked eq false) }
            .singleOrNull()?.let {
                it[RefreshTokens.expiresAt] > System.currentTimeMillis()
            } ?: false
    }

    fun revokeToken(token: String): Boolean = transaction {
        RefreshTokens.update({ RefreshTokens.token eq token }) {
            it[isRevoked] = true
        } > 0
    }

    fun revokeAllUserTokens(userId: Int): Int = transaction {
        RefreshTokens.update({ RefreshTokens.userId eq userId }) {
            it[isRevoked] = true
        }
    }
}