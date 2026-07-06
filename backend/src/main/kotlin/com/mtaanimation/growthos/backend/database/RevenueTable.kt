package com.mtaanimation.growthos.backend.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import com.mtaanimation.growthos.backend.db.tables.UsersTable

object RevenueTable : Table("revenue_entries") {
    val id = uuid("id")
    val userId = uuid("user_id").references(UsersTable.id)
    val monthYear = varchar("month_year", 7) // "YYYY-MM"
    val youtubeRevenue = double("youtube_revenue").default(0.0)
    val tiktokRevenue = double("tiktok_revenue").default(0.0)
    val facebookRevenue = double("facebook_revenue").default(0.0)
    val instagramRevenue = double("instagram_revenue").default(0.0)
    val twitterRevenue = double("twitter_revenue").default(0.0)
    val sponsors = double("sponsors").default(0.0)
    val merchandise = double("merchandise").default(0.0)
    val websiteIncome = double("website_income").default(0.0)
    val otherIncome = double("other_income").default(0.0)
    
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
    
    init {
        uniqueIndex(userId, monthYear) // One entry per user per month
    }
}
