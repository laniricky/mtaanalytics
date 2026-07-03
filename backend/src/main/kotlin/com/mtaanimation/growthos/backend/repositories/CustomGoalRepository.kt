package com.mtaanimation.growthos.backend.repositories

import com.mtaanimation.growthos.backend.database.CustomGoalsTable
import com.mtaanimation.growthos.backend.db.DatabaseFactory.dbQuery
import com.mtaanimation.growthos.shared.models.customgoals.CreateCustomGoalRequest
import com.mtaanimation.growthos.shared.models.customgoals.CustomGoalDto
import com.mtaanimation.growthos.shared.models.customgoals.UpdateCustomGoalProgressRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

class CustomGoalRepository {

    suspend fun createGoal(userId: UUID, request: CreateCustomGoalRequest): CustomGoalDto? = dbQuery {
        val newId = UUID.randomUUID()
        CustomGoalsTable.insert {
            it[id] = newId
            it[this.userId] = userId
            it[title] = request.title
            it[type] = request.type
            it[targetValue] = request.targetValue
            it[currentValue] = request.currentValue
            it[deadline] = Instant.ofEpochMilli(request.deadlineEpochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
            it[createdAt] = LocalDateTime.now()
        }
        getGoalById(newId)
    }

    suspend fun updateProgress(userId: UUID, request: UpdateCustomGoalProgressRequest): CustomGoalDto? = dbQuery {
        val goalId = UUID.fromString(request.id)
        val updated = CustomGoalsTable.update({ (CustomGoalsTable.id eq goalId) and (CustomGoalsTable.userId eq userId) }) {
            it[currentValue] = request.currentValue
        }
        if (updated > 0) getGoalById(goalId) else null
    }

    suspend fun getAllGoals(userId: UUID): List<CustomGoalDto> = dbQuery {
        CustomGoalsTable
            .selectAll()
            .where { CustomGoalsTable.userId eq userId }
            .orderBy(CustomGoalsTable.createdAt to SortOrder.DESC)
            .map { it.toCustomGoalDto() }
    }

    private suspend fun getGoalById(id: UUID): CustomGoalDto? = dbQuery {
        CustomGoalsTable
            .selectAll()
            .where { CustomGoalsTable.id eq id }
            .map { it.toCustomGoalDto() }
            .singleOrNull()
    }

    private fun ResultRow.toCustomGoalDto(): CustomGoalDto {
        return CustomGoalDto(
            id = this[CustomGoalsTable.id].toString(),
            userId = this[CustomGoalsTable.userId].toString(),
            title = this[CustomGoalsTable.title],
            type = this[CustomGoalsTable.type],
            targetValue = this[CustomGoalsTable.targetValue],
            currentValue = this[CustomGoalsTable.currentValue],
            deadlineEpochMillis = this[CustomGoalsTable.deadline].atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
    }
}
