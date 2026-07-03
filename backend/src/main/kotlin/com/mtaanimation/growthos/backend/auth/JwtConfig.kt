package com.mtaanimation.growthos.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    private const val secret = "secret-key-change-in-production"
    private const val issuer = "https://jwt-provider-domain/"
    const val audience = "jwt-audience"
    const val realm = "mtaanimation-growthos"

    private const val validityInMs = 36_000_00 * 24 // 24 hours

    fun generateToken(username: String): String = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("username", username)
        .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
        .sign(Algorithm.HMAC256(secret))
        
    val verifier = JWT
        .require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()
}
