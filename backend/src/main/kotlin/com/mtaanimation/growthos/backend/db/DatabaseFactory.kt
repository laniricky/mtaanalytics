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
        // Neon's pooler (PgBouncer in transaction mode) is incompatible with Flyway DDL.
        // Strip "-pooler" from the host to get a direct connection for migrations.
        val directUrl = url.replace("-pooler", "")
        val sslDirectUrl = if (directUrl.contains("sslmode")) directUrl else "$directUrl?sslmode=require"

        runFlyway(sslDirectUrl, user, pass)

        // HikariCP app pool can use the pooler URL for better connection efficiency
        val appUrl = if (url.contains("sslmode")) url else "$url?sslmode=require"
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = appUrl
            username = user
            password = pass
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        Database.connect(HikariDataSource(config))
    }

    private fun runFlyway(directUrl: String, user: String, pass: String) {
        val flyway = Flyway.configure()
            .dataSource(directUrl, user, pass)
            .load()
        try {
            flyway.migrate()
        } catch (e: Exception) {
            println("Flyway migration error: ${e.message}")
            throw e
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction { block() }
        }
}
