package com.mtaanimation.growthos.backend.routes

import com.mtaanimation.growthos.backend.db.DatabaseFactory.dbQuery
import com.mtaanimation.growthos.backend.database.EpisodeLinksTable
import com.mtaanimation.growthos.backend.database.EpisodesTable
import com.mtaanimation.growthos.backend.database.RevenueTable
import com.mtaanimation.growthos.backend.db.tables.PlatformStatsTable
import com.mtaanimation.growthos.backend.repositories.UserRepository
import com.mtaanimation.growthos.shared.models.customgoals.MilestoneLiveValues
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

fun Route.milestoneLiveValuesRoute(userRepository: UserRepository) {
    route("/api/milestones") {
        authenticate("auth-jwt") {
            /**
             * GET /api/milestones/live-values
             * Returns aggregated current values computed from existing data sources:
             *   - totalViews     → sum of all episode_links.view_count for this user's episodes
             *   - totalEpisodes  → count of episodes for this user
             *   - totalRevenue   → sum of all revenue columns across all months for this user
             *   - totalFollowers → sum of the LATEST recorded currentFollowers per platform for this user
             */
            get("/live-values") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val user = userRepository.getUserByUsername(username)
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val userId = UUID.fromString(user.id)

                val liveValues = dbQuery {
                    // 1. Episode count
                    val episodeRows = EpisodesTable.select { EpisodesTable.userId eq userId }.toList()
                    val totalEpisodes = episodeRows.size.toLong()

                    // 2. Total views — sum all episode_links for the user's episodes
                    val episodeIds = episodeRows.map { it[EpisodesTable.id] }
                    val totalViews = if (episodeIds.isNotEmpty()) {
                        EpisodeLinksTable
                            .select { EpisodeLinksTable.episodeId inList episodeIds }
                            .sumOf { it[EpisodeLinksTable.viewCount] }
                    } else 0L

                    // 3. Total revenue — sum every income column across all months
                    val totalRevenue = RevenueTable
                        .select { RevenueTable.userId eq userId }
                        .sumOf { row ->
                            row[RevenueTable.youtubeRevenue] +
                            row[RevenueTable.tiktokRevenue] +
                            row[RevenueTable.facebookRevenue] +
                            row[RevenueTable.instagramRevenue] +
                            row[RevenueTable.sponsors] +
                            row[RevenueTable.merchandise] +
                            row[RevenueTable.websiteIncome] +
                            row[RevenueTable.otherIncome]
                        }

                    // 4. Total followers — latest record per platform, then sum
                    val allStats = PlatformStatsTable
                        .select { PlatformStatsTable.userId eq userId }
                        .toList()

                    // Group by platform, take the most-recently-recorded entry for each
                    val latestPerPlatform = allStats
                        .groupBy { it[PlatformStatsTable.platformType] }
                        .mapValues { (_, rows) ->
                            rows.maxByOrNull { it[PlatformStatsTable.dateRecorded] }
                        }

                    val totalFollowers = latestPerPlatform.values.sumOf {
                        it?.get(PlatformStatsTable.currentFollowers) ?: 0L
                    }

                    MilestoneLiveValues(
                        totalViews = totalViews,
                        totalEpisodes = totalEpisodes,
                        totalRevenue = totalRevenue,
                        totalFollowers = totalFollowers
                    )
                }

                call.respond(HttpStatusCode.OK, liveValues)
            }
        }
    }
}
