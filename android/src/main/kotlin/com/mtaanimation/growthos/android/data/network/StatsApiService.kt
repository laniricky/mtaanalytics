package com.mtaanimation.growthos.android.data.network

import com.mtaanimation.growthos.android.data.datastore.AuthDataStore
import com.mtaanimation.growthos.shared.models.PlatformStatsDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsApiService @Inject constructor(
    private val client: HttpClient,
    private val authDataStore: AuthDataStore
) {
    companion object {
        private const val BASE_URL = "http://10.0.2.2:8080"
    }

    suspend fun getAllStats(): Result<List<PlatformStatsDto>> = runCatching {
        val token = authDataStore.tokenFlow.first()
            ?: error("Not authenticated")

        client.get("$BASE_URL/api/stats") {
            bearerAuth(token)
        }.body()
    }

    suspend fun postStats(request: com.mtaanimation.growthos.shared.models.RecordStatsRequest): Result<PlatformStatsDto> = runCatching {
        val token = authDataStore.tokenFlow.first()
            ?: error("Not authenticated")

        client.post("$BASE_URL/api/stats") {
            io.ktor.http.contentType(io.ktor.http.ContentType.Application.Json)
            bearerAuth(token)
            io.ktor.client.request.setBody(request)
        }.body()
    }
}
