package com.mtaanimation.growthos.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    // Populated at startup from application.conf / env vars
    lateinit var secret: String
    lateinit var issuer: String
    lateinit var audience: String
    const val realm = "mtaanimation-growthos"

    // 30 days in milliseconds — avoids daily logouts
    private const val validityInMs = 30L * 24 * 60 * 60 * 1000

    fun generateToken(username: String): String = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("username", username)
        .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
        .sign(Algorithm.HMAC256(secret))

    fun buildVerifier() = JWT
        .require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()
}
