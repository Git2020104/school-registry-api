package com.maranatha.skools.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init() {
        val config = HikariConfig().apply {
            // AUTO_SERVER=TRUE allows external DB tools (e.g. DBeaver) to connect while the app is running
            jdbcUrl = "jdbc:h2:file:./data/db/school_db;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE"
            driverClassName = "org.h2.Driver"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        val dataSource = HikariDataSource(config)

        // Run Flyway migrations before Exposed connects
        Flyway.configure()
            .dataSource(dataSource)
            .load()
            .migrate()

        // Connect Exposed to the Hikari DataSource
        Database.connect(dataSource)
    }
}