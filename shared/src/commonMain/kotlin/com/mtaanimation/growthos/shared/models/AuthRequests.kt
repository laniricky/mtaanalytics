package com.mtaanimation.growthos.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val passwordHash: String // We will hash on client for extra security or send raw over HTTPS, but here we just accept a string
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val passwordHash: String
)
