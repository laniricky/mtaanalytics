package com.mtaanimation.growthos.backend.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object PlatformStatsTable : Table("platform_stats") {
    val id = uuid("id")
    val userId = reference("user_id", UsersTable.id)
    val platformType = varchar("platform_type", 50)
    val currentFollowers = long("current_followers")
    val target2036 = long("target_2036")
    val dateRecorded = timestamp("date_recorded")
    val createdAt = timestamp("created_at")
    
    override val primaryKey = PrimaryKey(id)
}
