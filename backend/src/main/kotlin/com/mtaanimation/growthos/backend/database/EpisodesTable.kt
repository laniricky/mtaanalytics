package com.mtaanimation.growthos.backend.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import com.mtaanimation.growthos.backend.db.tables.UsersTable

object EpisodesTable : Table("episodes") {
    val id = uuid("id")
    val userId = uuid("user_id").references(UsersTable.id)
    val season = integer("season")
    val episode = integer("episode")
    val releaseDate = datetime("release_date")
    val views = long("views").default(0)
    val revenue = double("revenue").default(0.0)
    val watchTimeHours = double("watch_time_hours").default(0.0)
    val shares = long("shares").default(0)
    val comments = long("comments").default(0)
    val likes = long("likes").default(0)
    
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
    
    init {
        uniqueIndex(userId, season, episode) // One entry per user/season/episode
    }
}
