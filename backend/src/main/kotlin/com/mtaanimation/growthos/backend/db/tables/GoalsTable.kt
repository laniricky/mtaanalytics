package com.mtaanimation.growthos.backend.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object GoalsTable : Table("goals") {
    val id = uuid("id")
    val userId = reference("user_id", UsersTable.id)
    val name = varchar("name", 255)
    val targetValue = long("target_value")
    val currentProgress = long("current_progress").default(0L)
    val targetDate = timestamp("target_date")
    val goalType = varchar("goal_type", 50)
    val createdAt = timestamp("created_at")
    
    override val primaryKey = PrimaryKey(id)
}
