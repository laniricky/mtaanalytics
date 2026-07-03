package com.mtaanimation.growthos.backend.repositories

import com.mtaanimation.growthos.backend.db.DatabaseFactory.dbQuery
import com.mtaanimation.growthos.backend.database.UploadsTable
import com.mtaanimation.growthos.shared.models.uploads.RecordUploadsRequest
import com.mtaanimation.growthos.shared.models.uploads.UploadsEntryDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import java.util.UUID

class UploadRepository {

    suspend fun recordUploads(userId: UUID, request: RecordUploadsRequest): UploadsEntryDto? = dbQuery {
        val existing = UploadsTable
            .select { (UploadsTable.userId eq userId) and (UploadsTable.weekStartDate eq request.weekStartDate) }
            .singleOrNull()

        if (existing != null) {
            UploadsTable.update({ UploadsTable.id eq existing[UploadsTable.id] }) {
                it[youtubeUploads] = request.youtubeUploads
                it[tiktokUploads] = request.tiktokUploads
                it[facebookUploads] = request.facebookUploads
                it[instagramUploads] = request.instagramUploads
            }
        } else {
            UploadsTable.insert {
                it[id] = UUID.randomUUID()
                it[this.userId] = userId
                it[weekStartDate] = request.weekStartDate
                it[youtubeUploads] = request.youtubeUploads
                it[tiktokUploads] = request.tiktokUploads
                it[facebookUploads] = request.facebookUploads
                it[instagramUploads] = request.instagramUploads
                it[createdAt] = LocalDateTime.now()
            }
        }

        UploadsTable
            .select { (UploadsTable.userId eq userId) and (UploadsTable.weekStartDate eq request.weekStartDate) }
            .map { it.toUploadsEntryDto() }
            .singleOrNull()
    }

    suspend fun getAllUploads(userId: UUID): List<UploadsEntryDto> = dbQuery {
        UploadsTable
            .select { UploadsTable.userId eq userId }
            .orderBy(UploadsTable.weekStartDate to SortOrder.DESC)
            .map { it.toUploadsEntryDto() }
    }

    private fun ResultRow.toUploadsEntryDto(): UploadsEntryDto {
        return UploadsEntryDto(
            id = this[UploadsTable.id].toString(),
            userId = this[UploadsTable.userId].toString(),
            weekStartDate = this[UploadsTable.weekStartDate],
            youtubeUploads = this[UploadsTable.youtubeUploads],
            tiktokUploads = this[UploadsTable.tiktokUploads],
            facebookUploads = this[UploadsTable.facebookUploads],
            instagramUploads = this[UploadsTable.instagramUploads]
        )
    }
}
