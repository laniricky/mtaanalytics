package com.mtaanimation.growthos.backend.repositories

import com.mtaanimation.growthos.backend.db.DatabaseFactory.dbQuery
import com.mtaanimation.growthos.backend.db.tables.GoalsTable
import com.mtaanimation.growthos.shared.models.Goal
import com.mtaanimation.growthos.shared.models.GoalType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.Instant
import java.util.UUID

class GoalRepository {

    suspend fun createGoal(userId: UUID, name: String, targetValue: Long, targetDate: Instant, type: GoalType): Goal? = dbQuery {
        val insertStatement = GoalsTable.insert {
            it[id] = UUID.randomUUID()
            it[GoalsTable.userId] = userId
            it[GoalsTable.name] = name
            it[GoalsTable.targetValue] = targetValue
            it[GoalsTable.targetDate] = targetDate
            it[GoalsTable.goalType] = type.name
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToGoal)
    }

    suspend fun getGoalsForUser(userId: UUID): List<Goal> = dbQuery {
        GoalsTable.select { GoalsTable.userId eq userId }
            .map(::resultRowToGoal)
    }

    private fun resultRowToGoal(row: ResultRow): Goal =
        Goal(
            id = row[GoalsTable.id].toString(),
            name = row[GoalsTable.name],
            targetValue = row[GoalsTable.targetValue],
            currentProgress = row[GoalsTable.currentProgress],
            targetDateEpochMillis = row[GoalsTable.targetDate].toEpochMilli(),
            type = GoalType.valueOf(row[GoalsTable.goalType])
        )
}
