package com.mtaanimation.growthos.backend.repositories

import com.mtaanimation.growthos.backend.db.DatabaseFactory.dbQuery
import com.mtaanimation.growthos.backend.db.tables.UsersTable
import com.mtaanimation.growthos.shared.models.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.util.UUID

class UserRepository {

    suspend fun createUser(username: String, email: String, passwordHash: String): User? = dbQuery {
        val insertStatement = UsersTable.insert {
            it[id] = UUID.randomUUID()
            it[UsersTable.username] = username
            it[UsersTable.email] = email
            it[UsersTable.passwordHash] = passwordHash
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToUser)
    }

    suspend fun getUserByUsername(username: String): User? = dbQuery {
        UsersTable.select { UsersTable.username eq username }
            .map(::resultRowToUser)
            .singleOrNull()
    }
    
    suspend fun getPasswordHashByUsername(username: String): String? = dbQuery {
        UsersTable.select { UsersTable.username eq username }
            .map { it[UsersTable.passwordHash] }
            .singleOrNull()
    }

    private fun resultRowToUser(row: ResultRow): User =
        User(
            id = row[UsersTable.id].toString(),
            username = row[UsersTable.username],
            email = row[UsersTable.email]
        )
}
