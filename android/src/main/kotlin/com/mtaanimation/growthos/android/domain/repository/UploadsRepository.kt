package com.mtaanimation.growthos.android.domain.repository

import com.mtaanimation.growthos.android.data.network.UploadsApiService
import com.mtaanimation.growthos.shared.models.uploads.RecordUploadsRequest
import com.mtaanimation.growthos.shared.models.uploads.UploadsEntryDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadsRepository @Inject constructor(
    private val apiService: UploadsApiService
) {
    suspend fun getAllUploads(): Result<List<UploadsEntryDto>> = apiService.getAllUploads()

    suspend fun recordUploads(request: RecordUploadsRequest): Result<UploadsEntryDto> =
        apiService.postUploads(request)
}
