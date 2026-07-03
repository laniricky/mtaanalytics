package com.mtaanimation.growthos.backend.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object CustomGoalsTable : Table("custom_goals") {
    val id = uuid("id")
    val userId = uuid("user_id").references(Users.id)
    val title = varchar("title", 255)
    val type = varchar("type", 50)
    val targetValue = double("target_value")
    val currentValue = double("current_value").default(0.0)
    val deadline = datetime("deadline")
    
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
