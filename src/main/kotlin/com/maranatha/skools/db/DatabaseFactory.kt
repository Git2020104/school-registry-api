package com.maranatha.skools.db

import com.maranatha.skools.models.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        // 1. Connect to local H2 database file
        Database.connect(
            url = "jdbc:h2:file:./build/db/school_db;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )

        // 2. Create tables inside a transaction
        transaction {
            SchemaUtils.create(UsersTable)
        }
    }
}