package com.mtaanimation.growthos.android.data.network

import com.mtaanimation.growthos.android.data.datastore.AuthDataStore
import com.mtaanimation.growthos.shared.models.AuthToken
import com.mtaanimation.growthos.shared.models.LoginRequest
import com.mtaanimation.growthos.shared.models.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
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
        const val BASE_URL = "https://mtaanalytics.onrender.com"
    }

    /**
     * Registers a new account. On success, persists the returned JWT token.
     * Checks HTTP status before attempting JSON deserialization to avoid
     * NoTransformationFoundException on error responses.
     */
    suspend fun register(username: String, email: String, password: String): Result<AuthToken> =
        runCatching {
            val response = client.post("$BASE_URL/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(username, email, password.sha256()))
            }
            if (!response.status.isSuccess()) {
                val errorText = response.bodyAsText()
                error("Registration failed (${response.status.value}): $errorText")
            }
            val token: AuthToken = response.body()
            authDataStore.saveToken(token.token)
            token
        }

    /**
     * Logs in an existing account. On success, persists the returned JWT token.
     * Checks HTTP status before attempting JSON deserialization to avoid
     * NoTransformationFoundException on error responses.
     */
    suspend fun login(username: String, password: String): Result<AuthToken> =
        runCatching {
            val response = client.post("$BASE_URL/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password.sha256()))
            }
            if (!response.status.isSuccess()) {
                val errorText = response.bodyAsText()
                error("Login failed (${response.status.value}): $errorText")
            }
            val token: AuthToken = response.body()
            authDataStore.saveToken(token.token)
            token
        }

    /**
     * Clears the persisted token, logging the user out locally.
     */
    suspend fun logout() = authDataStore.clearToken()

    private fun String.sha256(): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
        return bytes.joinToString("") {
            val hex = (it.toInt() and 0xFF).toString(16)
            if (hex.length == 1) "0$hex" else hex
        }
    }
}
