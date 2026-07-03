package com.mtaanimation.growthos.android.data.network

import com.mtaanimation.growthos.android.data.datastore.AuthDataStore
import com.mtaanimation.growthos.shared.models.PlatformStats
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.coroutines.flow.first
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import io.ktor.http.ContentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsApiService @Inject constructor(
    private val client: HttpClient,
    private val authDataStore: AuthDataStore
) {
    companion object {
        private const val BASE_URL = "https://mtaanalytics.onrender.com"
    }

    suspend fun getAllStats(): Result<List<PlatformStats>> = runCatching {
        val token = authDataStore.tokenFlow.first()
            ?: error("Not authenticated")

        client.get("$BASE_URL/api/stats") {
            bearerAuth(token)
        }.body()
    }

    suspend fun postStats(request: com.mtaanimation.growthos.shared.models.RecordStatsRequest): Result<PlatformStats> = runCatching {
        val token = authDataStore.tokenFlow.first()
            ?: error("Not authenticated")

        client.post("$BASE_URL/api/stats") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(request)
        }.body()
    }
}
