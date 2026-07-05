package com.mtaanimation.growthos.android.data.network

import com.mtaanimation.growthos.android.data.datastore.AuthDataStore
import com.mtaanimation.growthos.shared.projection.DashboardProjection
import com.mtaanimation.growthos.shared.projection.ProjectionRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API service for fetching dashboard projections from the backend.
 * Automatically attaches the persisted JWT bearer token to every request.
 */
@Singleton
class DashboardApiService @Inject constructor(
    private val client: HttpClient,
    private val authDataStore: AuthDataStore
) {
    companion object {
        private const val BASE_URL = "https://mtaanalytics.onrender.com"
    }

    suspend fun getDashboardProjection(
        deadlineEpochMillis: Long? = null
    ): Result<DashboardProjection> = runCatching {
        val token = authDataStore.tokenFlow.first()
            ?: error("Not authenticated")

        val response = client.post("$BASE_URL/api/projections/dashboard") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(ProjectionRequest(deadlineEpochMillis = deadlineEpochMillis))
        }

        if (!response.status.isSuccess()) {
            val errorText = response.bodyAsText()
            error("Dashboard fetch failed (${response.status.value}): $errorText")
        }

        response.body()
    }
}
