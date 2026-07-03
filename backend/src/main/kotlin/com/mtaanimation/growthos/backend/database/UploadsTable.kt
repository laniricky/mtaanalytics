package com.mtaanimation.growthos.backend.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import com.mtaanimation.growthos.backend.db.tables.UsersTable

object UploadsTable : Table("weekly_uploads") {
    val id = uuid("id")
    val userId = uuid("user_id").references(UsersTable.id)
    val weekStartDate = varchar("week_start_date", 10) // "YYYY-MM-DD"
    val youtubeUploads = integer("youtube_uploads").default(0)
    val tiktokUploads = integer("tiktok_uploads").default(0)
    val facebookUploads = integer("facebook_uploads").default(0)
    val instagramUploads = integer("instagram_uploads").default(0)
    
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
    
    init {
        uniqueIndex(userId, weekStartDate) // One entry per user per week
    }
}
