package com.mtaanimation.growthos.android.data.network

import com.mtaanimation.growthos.android.data.datastore.AuthDataStore
import com.mtaanimation.growthos.shared.models.uploads.RecordUploadsRequest
import com.mtaanimation.growthos.shared.models.uploads.UploadsEntryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadsApiService @Inject constructor(
    private val client: HttpClient,
    private val authDataStore: AuthDataStore
) {
    companion object {
        private const val BASE_URL = "https://mtaanalytics.onrender.com"
    }

    suspend fun getAllUploads(): Result<List<UploadsEntryDto>> = runCatching {
        val token = authDataStore.tokenFlow.first() ?: error("Not authenticated")
        client.get("$BASE_URL/api/uploads") {
            bearerAuth(token)
        }.body()
    }

    suspend fun postUploads(request: RecordUploadsRequest): Result<UploadsEntryDto> = runCatching {
        val token = authDataStore.tokenFlow.first() ?: error("Not authenticated")
        client.post("$BASE_URL/api/uploads") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(request)
        }.body()
    }
}
