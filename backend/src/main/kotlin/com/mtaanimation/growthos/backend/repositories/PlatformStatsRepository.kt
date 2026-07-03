package com.mtaanimation.growthos.backend.repositories

import com.mtaanimation.growthos.backend.db.DatabaseFactory.dbQuery
import com.mtaanimation.growthos.backend.db.tables.PlatformStatsTable
import com.mtaanimation.growthos.shared.models.PlatformStats
import com.mtaanimation.growthos.shared.models.PlatformType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.Instant
import java.util.UUID

class PlatformStatsRepository {

    suspend fun recordStats(userId: UUID, platformType: PlatformType, currentFollowers: Long, target2036: Long, dateRecorded: Instant): PlatformStats? = dbQuery {
        val insertStatement = PlatformStatsTable.insert {
            it[id] = UUID.randomUUID()
            it[PlatformStatsTable.userId] = userId
            it[PlatformStatsTable.platformType] = platformType.name
            it[PlatformStatsTable.currentFollowers] = currentFollowers
            it[PlatformStatsTable.target2036] = target2036
            it[PlatformStatsTable.dateRecorded] = dateRecorded
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToPlatformStats)
    }

    suspend fun getStatsForUser(userId: UUID): List<PlatformStats> = dbQuery {
        PlatformStatsTable.select { PlatformStatsTable.userId eq userId }
            .map(::resultRowToPlatformStats)
    }

    private fun resultRowToPlatformStats(row: ResultRow): PlatformStats =
        PlatformStats(
            id = row[PlatformStatsTable.id].toString(),
            platformType = PlatformType.valueOf(row[PlatformStatsTable.platformType]),
            currentFollowers = row[PlatformStatsTable.currentFollowers],
            target2036 = row[PlatformStatsTable.target2036],
            dateRecordedEpochMillis = row[PlatformStatsTable.dateRecorded].toEpochMilli()
        )
}
