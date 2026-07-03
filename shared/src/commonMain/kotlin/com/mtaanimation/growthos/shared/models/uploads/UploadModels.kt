package com.mtaanimation.growthos.shared.models.uploads

import kotlinx.serialization.Serializable

@Serializable
data class UploadsEntryDto(
    val id: String,
    val userId: String,
    val weekStartDate: String, // e.g., "2026-07-01"
    val youtubeUploads: Int,
    val tiktokUploads: Int,
    val facebookUploads: Int,
    val instagramUploads: Int
)

@Serializable
data class RecordUploadsRequest(
    val weekStartDate: String,
    val youtubeUploads: Int = 0,
    val tiktokUploads: Int = 0,
    val facebookUploads: Int = 0,
    val instagramUploads: Int = 0
)
