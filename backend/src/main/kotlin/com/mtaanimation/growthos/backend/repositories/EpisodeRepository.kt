
package com.mtaanimation.growthos.backend.repositories

import com.mtaanimation.growthos.backend.db.DatabaseFactory.dbQuery
import com.mtaanimation.growthos.backend.database.EpisodesTable
import com.mtaanimation.growthos.backend.database.EpisodeLinksTable
import com.mtaanimation.growthos.shared.models.episodes.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID

class EpisodeRepository {

    suspend fun createEpisode(userId: UUID, request: CreateEpisodeRequest): EpisodeDto? {
        val newId = UUID.randomUUID()
        dbQuery {
            EpisodesTable.insert {
                it[id] = newId
                it[this.userId] = userId
                it[title] = request.title
                it[description] = request.description
                it[publishedAt] = request.publishedAt
                it[createdAt] = Instant.now().toEpochMilli()
            }
        }
        return getEpisode(newId)
    }

    suspend fun upsertLink(episodeId: UUID, request: UpsertEpisodeLinkRequest): EpisodeLinkDto? = dbQuery {
        val existing = EpisodeLinksTable
            .select { (EpisodeLinksTable.episodeId eq episodeId) and (EpisodeLinksTable.platform eq request.platform) }
            .singleOrNull()

        val now = Instant.now().toEpochMilli()

        if (existing != null) {
            EpisodeLinksTable.update({ EpisodeLinksTable.id eq existing[EpisodeLinksTable.id] }) {
                it[url] = request.url
                it[viewCount] = request.viewCount
                it[updatedAt] = now
            }
        } else {
            EpisodeLinksTable.insert {
                it[id] = UUID.randomUUID()
                it[this.episodeId] = episodeId
                it[platform] = request.platform
                it[url] = request.url
                it[viewCount] = request.viewCount
                it[updatedAt] = now
            }
        }

        EpisodeLinksTable
            .select { (EpisodeLinksTable.episodeId eq episodeId) and (EpisodeLinksTable.platform eq request.platform) }
            .singleOrNull()?.toEpisodeLinkDto()
    }

    suspend fun getEpisodesForUser(userId: UUID): List<EpisodeDto> = dbQuery {
        val episodes = EpisodesTable
            .select { EpisodesTable.userId eq userId }
            .orderBy(EpisodesTable.publishedAt to SortOrder.DESC)
            .map { it }

        val episodeIds = episodes.map { it[EpisodesTable.id] }

        val allLinks = if (episodeIds.isNotEmpty()) {
            EpisodeLinksTable.select { EpisodeLinksTable.episodeId inList episodeIds }
                .map { it }
        } else {
            emptyList()
        }

        episodes.map { row ->
            val id = row[EpisodesTable.id]
            val links = allLinks.filter { it[EpisodeLinksTable.episodeId] == id }.map { it.toEpisodeLinkDto() }
            val totalViews = links.sumOf { it.viewCount }
            row.toEpisodeDto(totalViews, links)
        }
    }

    suspend fun deleteEpisode(id: UUID): Boolean = dbQuery {
        val count = EpisodesTable.deleteWhere { EpisodesTable.id eq id }
        count > 0
    }

    private suspend fun getEpisode(id: UUID): EpisodeDto? = dbQuery {
        val row = EpisodesTable.select { EpisodesTable.id eq id }.singleOrNull() ?: return@dbQuery null
        val links = EpisodeLinksTable.select { EpisodeLinksTable.episodeId eq id }.map { it.toEpisodeLinkDto() }
        val totalViews = links.sumOf { it.viewCount }
        row.toEpisodeDto(totalViews, links)
    }

    private fun ResultRow.toEpisodeDto(totalViews: Long, links: List<EpisodeLinkDto>): EpisodeDto {
        return EpisodeDto(
            id = this[EpisodesTable.id].toString(),
            title = this[EpisodesTable.title],
            description = this[EpisodesTable.description],
            publishedAt = this[EpisodesTable.publishedAt],
            totalViews = totalViews,
            links = links
        )
    }

    private fun ResultRow.toEpisodeLinkDto(): EpisodeLinkDto {
        return EpisodeLinkDto(
            id = this[EpisodeLinksTable.id].toString(),
            platform = this[EpisodeLinksTable.platform],
            url = this[EpisodeLinksTable.url],
            viewCount = this[EpisodeLinksTable.viewCount],
            updatedAt = this[EpisodeLinksTable.updatedAt]
        )
    }
}
