package com.mtaanimation.growthos.backend

import com.mtaanimation.growthos.backend.auth.JwtConfig
import com.mtaanimation.growthos.backend.db.DatabaseFactory
import com.mtaanimation.growthos.backend.plugins.configureRouting
import com.mtaanimation.growthos.backend.plugins.configureSecurity
import com.mtaanimation.growthos.backend.plugins.configureSerialization
import io.ktor.server.application.Application

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    // Read database config from env vars (with local fallbacks)
    val dbUrl = environment.config.propertyOrNull("database.url")?.getString()
        ?: "jdbc:postgresql://localhost:5432/growthos"
    val dbUser = environment.config.propertyOrNull("database.user")?.getString() ?: "mtauser"
    val dbPassword = environment.config.propertyOrNull("database.password")?.getString() ?: "mtapassword"

    // Read JWT config from env vars (with local fallbacks)
    JwtConfig.secret = environment.config.propertyOrNull("jwt.secret")?.getString()
        ?: "secret-key-change-in-production"
    JwtConfig.issuer = environment.config.propertyOrNull("jwt.domain")?.getString()
        ?: "https://mtaanalytics.onrender.com"
    JwtConfig.audience = environment.config.propertyOrNull("jwt.audience")?.getString()
        ?: "mtaanalytics-users"

    DatabaseFactory.init(dbUrl, dbUser, dbPassword)

    configureSerialization()
    configureSecurity()
    configureRouting()
}
