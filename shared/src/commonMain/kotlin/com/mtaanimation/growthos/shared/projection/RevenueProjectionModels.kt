package com.mtaanimation.growthos.shared.projection

import kotlinx.serialization.Serializable

/**
 * The projection snapshot for monthly revenue.
 */
@Serializable
data class RevenueProjection(
    val currentMonthlyRevenue: Double,
    val target2036: Double,
    val percentageComplete: Double,
    /** Revenue needed per month to hit next month's curve point. */
    val requiredMonthlyGrowth: Double,
    /** Epoch ms of target deadline (July 2036). */
    val deadlineEpochMillis: Long,
    val status: GrowthStatus,
    val monthlyProjectionPoints: List<RevenueProjectionPoint>,

    // --- Delta Analytics ---
    /** Where the S-Curve milestone expected revenue to be today. */
    val milestoneTargetRevenue: Double,
    /** Actual revenue minus milestone target. Positive = ahead, negative = behind. */
    val varianceRevenue: Double,
    /** Variance as a percentage of the milestone target. e.g., +5.2 or -2.1 */
    val variancePercentage: Double,
    /** Actual average monthly revenue growth from history. Null if insufficient data. */
    val actualMonthlyGain: Double?
)

/**
 * A single data point on a projected revenue curve.
 */
@Serializable
data class RevenueProjectionPoint(
    /** Epoch milliseconds representing the date of this projection. */
    val dateEpochMillis: Long,
    /** The projected revenue at this date. */
    val projectedValue: Double,
    /** The actual recorded revenue at this date, or null if in the future. */
    val actualValue: Double? = null
)
