package com.mtaanimation.growthos.shared.models.revenue

import kotlinx.serialization.Serializable

@Serializable
data class RevenueEntryDto(
    val id: String,
    val userId: String,
    val monthYear: String, // e.g., "2026-07"
    val youtubeRevenue: Double,
    val tiktokRevenue: Double,
    val facebookRevenue: Double,
    val instagramRevenue: Double,
    val twitterRevenue: Double,
    val sponsors: Double,
    val merchandise: Double,
    val websiteIncome: Double,
    val otherIncome: Double,
    val totalRevenue: Double
)

@Serializable
data class RecordRevenueRequest(
    val monthYear: String,
    val youtubeRevenue: Double = 0.0,
    val tiktokRevenue: Double = 0.0,
    val facebookRevenue: Double = 0.0,
    val instagramRevenue: Double = 0.0,
    val twitterRevenue: Double = 0.0,
    val sponsors: Double = 0.0,
    val merchandise: Double = 0.0,
    val websiteIncome: Double = 0.0,
    val otherIncome: Double = 0.0
)
