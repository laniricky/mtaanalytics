package com.mtaanimation.growthos.shared.projection

import kotlinx.serialization.Serializable

/**
 * A single data point on a projected growth curve.
 */
@Serializable
data class ProjectionPoint(
    /** Epoch milliseconds representing the date of this projection. */
    val dateEpochMillis: Long,
    /** The projected follower/view count at this date. */
    val projectedValue: Long,
    /** The actual recorded value at this date, or null if in the future. */
    val actualValue: Long? = null
)

/**
 * Per-platform projection broken into multiple time horizons.
 */
@Serializable
data class PlatformProjection(
    val platformType: String,
    val currentFollowers: Long,
    val target2036: Long,
    /** Followers needed per year to hit target. */
    val requiredYearlyGain: Long,
    /** Followers needed per month to hit target. */
    val requiredMonthlyGain: Long,
    /** Followers needed per week to hit target. */
    val requiredWeeklyGain: Long,
    /** Followers needed per day to hit target. */
    val requiredDailyGain: Long,
    /** Projected finish date at current growth rate (epoch ms), null if no history. */
    val projectedFinishDateEpochMillis: Long?,
    /** Status based on actual vs. required growth rate. */
    val status: GrowthStatus,
    /** Monthly data points for the growth chart. */
    val monthlyProjectionPoints: List<ProjectionPoint>
)

/**
 * The full dashboard projection snapshot.
 */
@Serializable
data class DashboardProjection(
    val combinedCurrentFollowers: Long,
    val combinedTarget: Long,
    val percentageComplete: Double,
    val remainingFollowers: Long,
    val remainingMonths: Long,
    val requiredMonthlyGain: Long,
    val requiredDailyGain: Long,
    /** Epoch ms of target deadline (July 2036). */
    val deadlineEpochMillis: Long,
    /** Projected finish date at current rate (epoch ms), or null if insufficient data. */
    val projectedFinishDateEpochMillis: Long?,
    val status: GrowthStatus,
    val platformProjections: List<PlatformProjection>
)

@Serializable
enum class GrowthStatus {
    /** Growing faster than needed. */
    AHEAD,
    /** Growing at approximately the required rate. */
    ON_TRACK,
    /** Growing slower than required. */
    BEHIND,
    /** No historical data to determine status. */
    UNKNOWN
}
