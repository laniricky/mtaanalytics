package com.mtaanimation.growthos.backend.constants

import com.mtaanimation.growthos.shared.models.PlatformType

/**
 * Fixed 10-year growth constants.
 *
 * These are the immutable baseline and target values that define the S-Curve for each platform.
 * The curve is always drawn from the BASE value (where we started in July 2026) to the TARGET
 * value (where we aim to be in July 2036). Historical data logged by the user is compared
 * against this fixed curve to determine variance and status.
 *
 * Changing these values changes the goal — so they are intentionally locked here.
 */
object GrowthConstants {

    /** The starting date of the 10-year journey: July 1, 2026 */
    const val START_YEAR = 2026
    const val START_MONTH = 7 // July

    /** The end date of the 10-year journey: July 1, 2036 */
    const val END_YEAR = 2036
    const val END_MONTH = 7 // July

    /** Total journey length in months */
    const val TOTAL_MONTHS = 120L

    /** Financial Goal constants */
    const val REVENUE_BASE_2026 = 500.0
    const val REVENUE_TARGET_2036 = 50_000.0

    data class PlatformGoal(
        val base: Long,   // followers at journey start (July 2026)
        val target: Long  // follower goal (July 2036)
    )

    val GOALS: Map<PlatformType, PlatformGoal> = mapOf(
        PlatformType.YOUTUBE   to PlatformGoal(base = 205L,       target = 12_000_000L),
        PlatformType.TIKTOK    to PlatformGoal(base = 101_200L,   target = 22_000_000L),
        PlatformType.FACEBOOK  to PlatformGoal(base = 13_500L,    target = 8_000_000L),
        PlatformType.INSTAGRAM to PlatformGoal(base = 2_520L,     target = 8_000_000L),
        PlatformType.X         to PlatformGoal(base = 2_999L,     target = 5_000_000L)
    )
}
