package com.mtaanimation.growthos.backend.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseFactory {
    fun init(url: String, user: String, pass: String) {
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            // Neon requires SSL — append if not already present
            jdbcUrl = if (url.contains("sslmode")) url else "$url?sslmode=require"
            username = user
            password = pass
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        val dataSource = HikariDataSource(config)
        runFlyway(dataSource)
        Database.connect(dataSource)
    }

    private fun runFlyway(dataSource: javax.sql.DataSource) {
        val flyway = Flyway.configure().dataSource(dataSource).load()
        try {
            flyway.info()
            flyway.migrate()
        } catch (e: Exception) {
            println("Exception running flyway migration: \${e.message}")
            throw e
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction { block() }
        }
}
