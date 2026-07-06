package com.mtaanimation.growthos.backend.database

import org.jetbrains.exposed.sql.Table
import com.mtaanimation.growthos.backend.db.tables.UsersTable

object EpisodesTable : Table("episodes") {
    val id = uuid("id")
    val userId = uuid("user_id").references(UsersTable.id)
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val publishedAt = long("published_at")
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

object EpisodeLinksTable : Table("episode_links") {
    val id = uuid("id")
    val episodeId = uuid("episode_id").references(EpisodesTable.id)
    val platform = varchar("platform", 50)
    val url = text("url").nullable()
    val viewCount = long("view_count").default(0L)
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(id)
    
    init {
        uniqueIndex(episodeId, platform)
    }
}
