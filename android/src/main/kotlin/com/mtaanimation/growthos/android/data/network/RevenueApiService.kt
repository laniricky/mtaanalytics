package com.mtaanimation.growthos.android.data.network

import com.mtaanimation.growthos.android.data.datastore.AuthDataStore
import com.mtaanimation.growthos.shared.models.revenue.RecordRevenueRequest
import com.mtaanimation.growthos.shared.models.revenue.RevenueEntryDto
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
class RevenueApiService @Inject constructor(
    private val client: HttpClient,
    private val authDataStore: AuthDataStore
) {
    companion object {
        private const val BASE_URL = "https://mtaanalytics.onrender.com"
    }

    suspend fun getAllRevenue(): Result<List<RevenueEntryDto>> = runCatching {
        val token = authDataStore.tokenFlow.first() ?: error("Not authenticated")
        client.get("$BASE_URL/api/revenue") {
            bearerAuth(token)
        }.body()
    }

    suspend fun postRevenue(request: RecordRevenueRequest): Result<RevenueEntryDto> = runCatching {
        val token = authDataStore.tokenFlow.first() ?: error("Not authenticated")
        client.post("$BASE_URL/api/revenue") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody(request)
        }.body()
    }
}
