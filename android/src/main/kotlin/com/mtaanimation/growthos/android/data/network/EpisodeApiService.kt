package com.mtaanimation.growthos.android.data.network

import com.mtaanimation.growthos.android.data.datastore.AuthDataStore
import com.mtaanimation.growthos.shared.models.episodes.CreateEpisodeRequest
import com.mtaanimation.growthos.shared.models.episodes.EpisodeDto
import com.mtaanimation.growthos.shared.models.episodes.EpisodeLinkDto
import com.mtaanimation.growthos.shared.models.episodes.UpsertEpisodeLinkRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpisodeApiService @Inject constructor(
    private val client: HttpClient,
    private val authDataStore: AuthDataStore
) {
    companion object {
        private const val BASE_URL = "https://mtaanalytics.onrender.com"
    }

    suspend fun getEpisodes(): Result<List<EpisodeDto>> = runCatching {
        val token = authDataStore.tokenFlow.first() ?: error("Not authenticated")

        val response = client.get("$BASE_URL/api/episodes") {
            bearerAuth(token)
        }

        if (!response.status.isSuccess()) {
            error("Failed to fetch episodes: ${response.bodyAsText()}")
        }

        response.body()
    }

    suspend fun createEpisode(request: CreateEpisodeRequest): Result<EpisodeDto> = runCatching {
        val token = authDataStore.tokenFlow.first() ?: error("Not authenticated")

        val response = client.post("$BASE_URL/api/episodes") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(request)
        }

        if (!response.status.isSuccess()) {
            error("Failed to create episode: ${response.bodyAsText()}")
        }

        response.body()
    }

    suspend fun upsertLink(episodeId: String, request: UpsertEpisodeLinkRequest): Result<EpisodeLinkDto> = runCatching {
        val token = authDataStore.tokenFlow.first() ?: error("Not authenticated")

        val response = client.post("$BASE_URL/api/episodes/$episodeId/links") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(request)
        }

        if (!response.status.isSuccess()) {
            error("Failed to upsert link: ${response.bodyAsText()}")
        }

        response.body()
    }

    suspend fun deleteEpisode(episodeId: String): Result<Unit> = runCatching {
        val token = authDataStore.tokenFlow.first() ?: error("Not authenticated")

        val response = client.delete("$BASE_URL/api/episodes/$episodeId") {
            bearerAuth(token)
        }

        if (!response.status.isSuccess()) {
            error("Failed to delete episode: ${response.bodyAsText()}")
        }
    }
}
