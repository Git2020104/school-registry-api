package com.maranatha.skools.db

import com.maranatha.skools.models.UsersTable
import org.jetbrains.exposed.sql.*

object RefreshTokens : Table("refresh_tokens") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(UsersTable.id)
    val token = varchar("token", 512).uniqueIndex()
    val expiresAt = long("expires_at")
    val isRevoked = bool("is_revoked").default(false)

    override val primaryKey = PrimaryKey(id)
}