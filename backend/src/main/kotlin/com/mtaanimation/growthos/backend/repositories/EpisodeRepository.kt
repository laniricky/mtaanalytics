package com.mtaanimation.growthos.backend.repositories

import com.mtaanimation.growthos.backend.db.DatabaseFactory.dbQuery
import com.mtaanimation.growthos.backend.database.EpisodesTable
import com.mtaanimation.growthos.shared.models.episodes.EpisodeDto
import com.mtaanimation.growthos.shared.models.episodes.RecordEpisodeRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

class EpisodeRepository {

    suspend fun recordEpisode(userId: UUID, request: RecordEpisodeRequest): EpisodeDto? = dbQuery {
        val existing = EpisodesTable
            .selectAll()
            .where { (EpisodesTable.userId eq userId) and (EpisodesTable.season eq request.season) and (EpisodesTable.episode eq request.episode) }
            .singleOrNull()

        if (existing != null) {
            EpisodesTable.update({ EpisodesTable.id eq existing[EpisodesTable.id] }) {
                it[releaseDate] = Instant.ofEpochMilli(request.releaseDateEpochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
                it[views] = request.views
                it[revenue] = request.revenue
                it[watchTimeHours] = request.watchTimeHours
                it[shares] = request.shares
                it[comments] = request.comments
                it[likes] = request.likes
            }
        } else {
            EpisodesTable.insert {
                it[id] = UUID.randomUUID()
                it[this.userId] = userId
                it[season] = request.season
                it[episode] = request.episode
                it[releaseDate] = Instant.ofEpochMilli(request.releaseDateEpochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
                it[views] = request.views
                it[revenue] = request.revenue
                it[watchTimeHours] = request.watchTimeHours
                it[shares] = request.shares
                it[comments] = request.comments
                it[likes] = request.likes
                it[createdAt] = LocalDateTime.now()
            }
        }

        getEpisode(userId, request.season, request.episode)
    }

    suspend fun getAllEpisodes(userId: UUID): List<EpisodeDto> = dbQuery {
        EpisodesTable
            .selectAll()
            .where { EpisodesTable.userId eq userId }
            .orderBy(EpisodesTable.season to SortOrder.DESC, EpisodesTable.episode to SortOrder.DESC)
            .map { it.toEpisodeDto() }
    }

    private suspend fun getEpisode(userId: UUID, season: Int, episode: Int): EpisodeDto? = dbQuery {
        EpisodesTable
            .selectAll()
            .where { (EpisodesTable.userId eq userId) and (EpisodesTable.season eq season) and (EpisodesTable.episode eq episode) }
            .map { it.toEpisodeDto() }
            .singleOrNull()
    }

    private fun ResultRow.toEpisodeDto(): EpisodeDto {
        return EpisodeDto(
            id = this[EpisodesTable.id].toString(),
            userId = this[EpisodesTable.userId].toString(),
            season = this[EpisodesTable.season],
            episode = this[EpisodesTable.episode],
            releaseDateEpochMillis = this[EpisodesTable.releaseDate].atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            views = this[EpisodesTable.views],
            revenue = this[EpisodesTable.revenue],
            watchTimeHours = this[EpisodesTable.watchTimeHours],
            shares = this[EpisodesTable.shares],
            comments = this[EpisodesTable.comments],
            likes = this[EpisodesTable.likes]
        )
    }
}
