package com.mtaanimation.growthos.backend.services

import com.mtaanimation.growthos.backend.constants.GrowthConstants
import com.mtaanimation.growthos.backend.repositories.PlatformStatsRepository
import com.mtaanimation.growthos.shared.models.PlatformStats
import com.mtaanimation.growthos.shared.models.PlatformType
import com.mtaanimation.growthos.shared.projection.DashboardProjection
import com.mtaanimation.growthos.shared.projection.GrowthStatus
import com.mtaanimation.growthos.shared.projection.PlatformProjection
import com.mtaanimation.growthos.shared.projection.ProjectionPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToLong

/**
 * ProjectionService computes all growth projections dynamically.
 *
 * DESIGN DECISIONS:
 * - The S-Curve is always drawn from the FIXED BASE (July 2026 starting values in GrowthConstants)
 *   to the FIXED TARGET (July 2036 goal values in GrowthConstants). The curve never moves.
 * - The user's actual logged followers are plotted AGAINST this fixed curve.
 * - Delta Analytics: We calculate how far ahead or behind the user is relative to WHERE THE CURVE
 *   EXPECTS THEM TO BE TODAY. This produces actionable metrics like "+5.2% (+2,500 followers)".
 * - Velocity Comparison: We show the user's actual monthly growth rate vs the required rate so
 *   they can see whether they are accelerating or decelerating, even when ahead.
 * - Status: AHEAD if actual > milestone by > 5%, BEHIND if < milestone by > 5%, ON_TRACK otherwise.
 */
class ProjectionService(private val platformStatsRepository: PlatformStatsRepository) {

    companion object {
        /** ±5% variance band around today's milestone before changing status. */
        private const val STATUS_THRESHOLD = 0.05

        /** The fixed start of the 10-year journey */
        private val JOURNEY_START: Instant =
            LocalDate.of(GrowthConstants.START_YEAR, GrowthConstants.START_MONTH, 1)
                .atStartOfDay().toInstant(ZoneOffset.UTC)

        /** The fixed end of the 10-year journey */
        private val JOURNEY_END: Instant =
            LocalDate.of(GrowthConstants.END_YEAR, GrowthConstants.END_MONTH, 1)
                .atStartOfDay().toInstant(ZoneOffset.UTC)
    }

    suspend fun computeDashboard(userId: UUID): DashboardProjection {
        val allStats = platformStatsRepository.getStatsForUser(userId)
        val latestPerPlatform = latestStatsPerPlatform(allStats)
        val nowInstant = Instant.now()

        // Use fixed totals from GrowthConstants for all platforms
        val combinedBase = GrowthConstants.GOALS.values.sumOf { it.base }
        val combinedTarget = GrowthConstants.GOALS.values.sumOf { it.target }

        // Current combined followers across all platforms the user has logged
        val combinedCurrent = if (latestPerPlatform.isEmpty()) combinedBase
        else {
            // For platforms not yet logged, use base value from GrowthConstants
            GrowthConstants.GOALS.entries.sumOf { (platform, goal) ->
                latestPerPlatform[platform]?.currentFollowers ?: goal.base
            }
        }

        val nowZoned = nowInstant.atZone(ZoneOffset.UTC)
        val deadlineZoned = JOURNEY_END.atZone(ZoneOffset.UTC)
        val remainingMonths = ChronoUnit.MONTHS.between(nowZoned, deadlineZoned).coerceAtLeast(1)

        // Months elapsed since journey START (fixed baseline)
        val monthsElapsed = ChronoUnit.MONTHS.between(
            JOURNEY_START.atZone(ZoneOffset.UTC), nowZoned
        ).coerceAtLeast(0)

        val percentageComplete = if (combinedTarget > 0) (combinedCurrent.toDouble() / combinedTarget) * 100.0 else 0.0
        val remainingFollowers = (combinedTarget - combinedCurrent).coerceAtLeast(0)

        // Where the fixed S-curve expects us to be TODAY (combined)
        val combinedMilestoneToday = GrowthConstants.GOALS.entries.sumOf { (platform, goal) ->
            calculateFixedLogisticProjection(
                base = goal.base,
                target = goal.target,
                monthsElapsed = monthsElapsed,
                totalMonths = GrowthConstants.TOTAL_MONTHS
            )
        }

        // Delta Analytics
        val combinedVariance = combinedCurrent - combinedMilestoneToday
        val combinedVariancePct = if (combinedMilestoneToday > 0)
            (combinedVariance.toDouble() / combinedMilestoneToday.toDouble()) * 100.0 else 0.0

        // Next month milestone for required gain
        val combinedNextMonthMilestone = GrowthConstants.GOALS.entries.sumOf { (platform, goal) ->
            calculateFixedLogisticProjection(
                base = goal.base,
                target = goal.target,
                monthsElapsed = monthsElapsed + 1,
                totalMonths = GrowthConstants.TOTAL_MONTHS
            )
        }
        val requiredMonthlyGain = (combinedNextMonthMilestone - combinedMilestoneToday).coerceAtLeast(0)
        val requiredDailyGain = (requiredMonthlyGain / 30.44).toLong()

        // Actual combined growth rate
        val combinedActualMonthlyRate = computeActualMonthlyRate(allStats)

        val projectedFinishDate = if (combinedActualMonthlyRate != null && combinedActualMonthlyRate > 0) {
            val monthsToFinish = remainingFollowers.toDouble() / combinedActualMonthlyRate
            nowInstant.plus((monthsToFinish * 30.44).roundToLong(), ChronoUnit.DAYS)
        } else null

        // Status based on variance vs today's milestone (not growth rate)
        val dashboardStatus = determineStatusFromVariance(combinedVariancePct)

        val platformProjections = GrowthConstants.GOALS.keys.map { platform ->
            val latestStats = latestPerPlatform[platform]
            buildPlatformProjection(
                platform = platform,
                latestStats = latestStats,
                history = allStats.filter { it.platformType == platform },
                nowInstant = nowInstant,
                monthsElapsed = monthsElapsed
            )
        }

        return DashboardProjection(
            combinedCurrentFollowers = combinedCurrent,
            combinedTarget = combinedTarget,
            percentageComplete = percentageComplete,
            remainingFollowers = remainingFollowers,
            remainingMonths = remainingMonths,
            requiredMonthlyGain = requiredMonthlyGain,
            requiredDailyGain = requiredDailyGain,
            deadlineEpochMillis = JOURNEY_END.toEpochMilli(),
            projectedFinishDateEpochMillis = projectedFinishDate?.toEpochMilli(),
            status = dashboardStatus,
            platformProjections = platformProjections,
            combinedMilestoneTarget = combinedMilestoneToday,
            combinedVarianceFollowers = combinedVariance,
            combinedVariancePercentage = combinedVariancePct,
            combinedActualMonthlyGain = combinedActualMonthlyRate
        )
    }

    private fun buildPlatformProjection(
        platform: PlatformType,
        latestStats: PlatformStats?,
        history: List<PlatformStats>,
        nowInstant: Instant,
        monthsElapsed: Long
    ): PlatformProjection {
        val goal = GrowthConstants.GOALS[platform]!!
        val currentFollowers = latestStats?.currentFollowers ?: goal.base

        val nowZoned = nowInstant.atZone(ZoneOffset.UTC)
        val deadlineZoned = JOURNEY_END.atZone(ZoneOffset.UTC)
        val remainingMonths = ChronoUnit.MONTHS.between(nowZoned, deadlineZoned).coerceAtLeast(1)
        val remaining = (goal.target - currentFollowers).coerceAtLeast(0)

        // Where the fixed curve expects this platform to be TODAY
        val milestoneToday = calculateFixedLogisticProjection(
            base = goal.base,
            target = goal.target,
            monthsElapsed = monthsElapsed,
            totalMonths = GrowthConstants.TOTAL_MONTHS
        )

        // Variance
        val varianceFollowers = currentFollowers - milestoneToday
        val variancePct = if (milestoneToday > 0)
            (varianceFollowers.toDouble() / milestoneToday.toDouble()) * 100.0 else 0.0

        // Next month milestone for required gain
        val nextMonthMilestone = calculateFixedLogisticProjection(
            base = goal.base,
            target = goal.target,
            monthsElapsed = monthsElapsed + 1,
            totalMonths = GrowthConstants.TOTAL_MONTHS
        )
        val requiredMonthly = (nextMonthMilestone - milestoneToday).coerceAtLeast(0)
        val requiredYearly = requiredMonthly * 12
        val requiredWeekly = (requiredMonthly / 4.33).toLong()
        val requiredDaily = (requiredMonthly / 30.44).toLong()

        val actualMonthlyRate = computeActualMonthlyRate(history)

        val projectedFinish = if (actualMonthlyRate != null && actualMonthlyRate > 0) {
            val monthsToFinish = remaining.toDouble() / actualMonthlyRate
            nowInstant.plus((monthsToFinish * 30.44).roundToLong(), ChronoUnit.DAYS)
        } else null

        val status = determineStatusFromVariance(variancePct)

        // Build monthly points using FIXED baseline (base → target over 120 months)
        val monthlyPoints = buildMonthlyPoints(
            base = goal.base,
            target = goal.target,
            history = history,
            nowInstant = nowInstant
        )

        return PlatformProjection(
            platformType = platform.name,
            currentFollowers = currentFollowers,
            target2036 = goal.target,
            requiredYearlyGain = requiredYearly,
            requiredMonthlyGain = requiredMonthly,
            requiredWeeklyGain = requiredWeekly,
            requiredDailyGain = requiredDaily,
            projectedFinishDateEpochMillis = projectedFinish?.toEpochMilli(),
            status = status,
            monthlyProjectionPoints = monthlyPoints,
            milestoneTargetFollowers = milestoneToday,
            varianceFollowers = varianceFollowers,
            variancePercentage = variancePct,
            actualMonthlyGain = actualMonthlyRate
        )
    }

    /**
     * Builds monthly projection points for the FIXED S-Curve.
     * The curve always starts from JOURNEY_START (July 2026) with [base] and ends at [target].
     * Points before today show the historical S-Curve target; points after today are future projections.
     * Actual recorded values are overlaid where they exist.
     */
    private fun buildMonthlyPoints(
        base: Long,
        target: Long,
        history: List<PlatformStats>,
        nowInstant: Instant
    ): List<ProjectionPoint> {
        val points = mutableListOf<ProjectionPoint>()
        val startDate = JOURNEY_START.atZone(ZoneOffset.UTC).toLocalDate().withDayOfMonth(1)
        val endDate = JOURNEY_END.atZone(ZoneOffset.UTC).toLocalDate().withDayOfMonth(1)

        val actualByYearMonth = history.associate { stat ->
            val ld = Instant.ofEpochMilli(stat.dateRecordedEpochMillis).atZone(ZoneOffset.UTC).toLocalDate()
            "${ld.year}-${ld.monthValue}" to stat.currentFollowers
        }

        var cursor = startDate
        var monthOffset = 0L
        while (!cursor.isAfter(endDate)) {
            val pointInstant = cursor.atStartOfDay().toInstant(ZoneOffset.UTC)
            val projectedValue = calculateFixedLogisticProjection(
                base = base,
                target = target,
                monthsElapsed = monthOffset,
                totalMonths = GrowthConstants.TOTAL_MONTHS
            )
            val key = "${cursor.year}-${cursor.monthValue}"
            val actualValue = actualByYearMonth[key]

            points.add(
                ProjectionPoint(
                    dateEpochMillis = pointInstant.toEpochMilli(),
                    projectedValue = projectedValue.coerceAtLeast(0),
                    actualValue = actualValue
                )
            )
            cursor = cursor.plusMonths(1)
            monthOffset++
        }
        return points
    }

    /**
     * Computes the average monthly growth rate from historical entries.
     * Returns null if there is insufficient data (fewer than 2 entries).
     */
    private fun computeActualMonthlyRate(stats: List<PlatformStats>): Double? {
        if (stats.size < 2) return null
        val sorted = stats.sortedBy { it.dateRecordedEpochMillis }
        val deltas = mutableListOf<Double>()
        for (i in 1 until sorted.size) {
            val prev = sorted[i - 1]
            val curr = sorted[i]
            val followerDiff = (curr.currentFollowers - prev.currentFollowers).toDouble()
            val prevInstant = Instant.ofEpochMilli(prev.dateRecordedEpochMillis)
            val currInstant = Instant.ofEpochMilli(curr.dateRecordedEpochMillis)
            val daysBetween = ChronoUnit.DAYS.between(prevInstant, currInstant).coerceAtLeast(1)
            deltas.add(followerDiff / daysBetween * 30.44)
        }
        return if (deltas.isEmpty()) null else deltas.average()
    }

    /**
     * Determines growth status based on variance percentage from today's milestone.
     * AHEAD:    variance > +5%
     * ON_TRACK: variance within ±5%
     * BEHIND:   variance < -5%
     */
    private fun determineStatusFromVariance(variancePercentage: Double): GrowthStatus = when {
        variancePercentage > STATUS_THRESHOLD * 100 -> GrowthStatus.AHEAD
        variancePercentage < -STATUS_THRESHOLD * 100 -> GrowthStatus.BEHIND
        else -> GrowthStatus.ON_TRACK
    }

    /**
     * Calculates the S-curve projection from a FIXED base value over the total 120-month journey.
     * Unlike the old method which started from "current followers", this always starts from [base].
     */
    private fun calculateFixedLogisticProjection(
        base: Long,
        target: Long,
        monthsElapsed: Long,
        totalMonths: Long
    ): Long {
        if (totalMonths <= 0) return target
        if (monthsElapsed <= 0) return base
        if (monthsElapsed >= totalMonths) return target

        val t = monthsElapsed.toDouble() / totalMonths.toDouble()
        val k = 10.0
        val t0 = 0.7

        fun s(x: Double): Double = 1.0 / (1.0 + exp(-k * (x - t0)))

        val s0 = s(0.0)
        val s1 = s(1.0)
        val st = s(t)

        val sNorm = (st - s0) / (s1 - s0)
        val projected = base + (target - base) * sNorm
        return projected.roundToLong()
    }

    /** Returns the most recent recorded PlatformStats per platform. */
    private fun latestStatsPerPlatform(stats: List<PlatformStats>): Map<PlatformType, PlatformStats> =
        stats
            .groupBy { it.platformType }
            .mapValues { (_, entries) -> entries.maxByOrNull { it.dateRecordedEpochMillis }!! }
}
