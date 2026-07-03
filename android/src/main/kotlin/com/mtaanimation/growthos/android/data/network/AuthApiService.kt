package com.mtaanimation.growthos.android.data.network

import com.mtaanimation.growthos.android.data.datastore.AuthDataStore
import com.mtaanimation.growthos.shared.models.AuthToken
import com.mtaanimation.growthos.shared.models.LoginRequest
import com.mtaanimation.growthos.shared.models.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles all Authentication API calls.
 * Raw password is hashed with SHA-256 before transmission —
 * providing an additional layer of obfuscation on top of HTTPS.
 */
@Singleton
class AuthApiService @Inject constructor(
    private val client: HttpClient,
    private val authDataStore: AuthDataStore
) {
    companion object {
        const val BASE_URL = "http://10.0.2.2:8080" // Emulator loopback; replace with production URL
    }

    /**
     * Registers a new account. On success, persists the returned JWT token.
     */
    suspend fun register(username: String, email: String, password: String): Result<AuthToken> =
        runCatching {
            val response: AuthToken = client.post("$BASE_URL/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(username, email, password.sha256()))
            }.body()
            authDataStore.saveToken(response.token)
            response
        }

    /**
     * Logs in an existing account. On success, persists the returned JWT token.
     */
    suspend fun login(username: String, password: String): Result<AuthToken> =
        runCatching {
            val response: AuthToken = client.post("$BASE_URL/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password.sha256()))
            }.body()
            authDataStore.saveToken(response.token)
            response
        }

    /**
     * Clears the persisted token, logging the user out locally.
     */
    suspend fun logout() = authDataStore.clearToken()

    private fun String.sha256(): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
