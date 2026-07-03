package com.mtaanimation.growthos.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String
)

@Serializable
data class AuthToken(
    val token: String
)
