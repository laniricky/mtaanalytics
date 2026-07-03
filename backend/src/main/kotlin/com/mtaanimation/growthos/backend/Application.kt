package com.mtaanimation.growthos.backend

import com.mtaanimation.growthos.backend.db.DatabaseFactory
import com.mtaanimation.growthos.backend.plugins.configureRouting
import com.mtaanimation.growthos.backend.plugins.configureSecurity
import com.mtaanimation.growthos.backend.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val dbUrl = environment.config.propertyOrNull("database.url")?.getString() ?: "jdbc:postgresql://localhost:5432/growthos"
    val dbUser = environment.config.propertyOrNull("database.user")?.getString() ?: "mtauser"
    val dbPassword = environment.config.propertyOrNull("database.password")?.getString() ?: "mtapassword"
    
    DatabaseFactory.init(dbUrl, dbUser, dbPassword)

    configureSerialization()
    configureSecurity()
    configureRouting()
}
