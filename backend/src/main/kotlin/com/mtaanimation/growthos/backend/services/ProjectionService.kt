package com.mtaanimation.growthos.backend.services

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
 * - All projections are calculated at request time from raw data. Nothing is hardcoded.
 * - Projection curves follow a Normalized Logistic Function (S-curve) to model realistic 
 *   creator audience growth (slow start, rapid acceleration, tapering near the end).
 * - If two or more historical monthly entries exist, we compute an actual monthly growth rate
 *   via linear regression to project the finish date.
 * - If fewer than 2 entries exist, status is UNKNOWN and projectedFinishDate is null.
 * - The deadline is the configurable target date stored on the user's primary follower Goal
 *   (defaulting to July 1, 2036 if not set), allowing dynamic recalculation.
 * - Status threshold: AHEAD if actual rate >= 110% of required; BEHIND if < 90%; ON_TRACK otherwise.
 */
class ProjectionService(private val platformStatsRepository: PlatformStatsRepository) {

    companion object {
        /** ±10% band around required rate before moving from ON_TRACK. */
        private const val STATUS_THRESHOLD = 0.10
        private val DEFAULT_DEADLINE: Instant =
            LocalDate.of(2036, 7, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
    }

    suspend fun computeDashboard(userId: UUID, deadline: Instant = DEFAULT_DEADLINE): DashboardProjection {
        val allStats = platformStatsRepository.getStatsForUser(userId)
        val latestPerPlatform = latestStatsPerPlatform(allStats)

        val combinedCurrent = latestPerPlatform.values.sumOf { it.currentFollowers }
        val combinedTarget = latestPerPlatform.values.sumOf { it.target2036 }

        val nowInstant = Instant.now()
        val nowZoned = nowInstant.atZone(ZoneOffset.UTC)
        val deadlineZoned = deadline.atZone(ZoneOffset.UTC)
        val remainingMonths = ChronoUnit.MONTHS.between(nowZoned, deadlineZoned).coerceAtLeast(1)
        val remainingDays = ChronoUnit.DAYS.between(nowZoned, deadlineZoned).coerceAtLeast(1)

        val remainingFollowers = (combinedTarget - combinedCurrent).coerceAtLeast(0)
        val percentageComplete = if (combinedTarget > 0) (combinedCurrent.toDouble() / combinedTarget) * 100.0 else 0.0

        val nextMonthProjection = calculateLogisticProjection(
            currentFollowers = combinedCurrent,
            targetFollowers = combinedTarget,
            monthsElapsed = 1L,
            totalMonths = remainingMonths
        )
        val requiredMonthlyGain = nextMonthProjection - combinedCurrent
        val requiredDailyGain = (requiredMonthlyGain / 30.44).toLong()

        // Compute actual combined monthly growth rate using linear regression over history
        val combinedActualMonthlyRate = computeActualMonthlyRate(allStats)

        val projectedFinishDate = if (combinedActualMonthlyRate != null && combinedActualMonthlyRate > 0) {
            val monthsToFinish = remainingFollowers.toDouble() / combinedActualMonthlyRate
            nowInstant.plus((monthsToFinish * 30.44).roundToLong(), ChronoUnit.DAYS)
        } else null

        val dashboardStatus = determineStatus(
            actualRate = combinedActualMonthlyRate,
            requiredRate = requiredMonthlyGain.toDouble()
        )

        val platformProjections = latestPerPlatform.map { (platform, stats) ->
            buildPlatformProjection(
                platform = platform,
                latestStats = stats,
                history = allStats.filter { it.platformType == platform },
                deadline = deadline,
                nowInstant = nowInstant
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
            deadlineEpochMillis = deadline.toEpochMilli(),
            projectedFinishDateEpochMillis = projectedFinishDate?.toEpochMilli(),
            status = dashboardStatus,
            platformProjections = platformProjections
        )
    }

    private fun buildPlatformProjection(
        platform: PlatformType,
        latestStats: PlatformStats,
        history: List<PlatformStats>,
        deadline: Instant,
        nowInstant: Instant
    ): PlatformProjection {
        val nowZoned = nowInstant.atZone(ZoneOffset.UTC)
        val deadlineZoned = deadline.atZone(ZoneOffset.UTC)
        val remainingMonths = ChronoUnit.MONTHS.between(nowZoned, deadlineZoned).coerceAtLeast(1)

        val remaining = (latestStats.target2036 - latestStats.currentFollowers).coerceAtLeast(0)

        val nextMonthProjection = calculateLogisticProjection(
            currentFollowers = latestStats.currentFollowers,
            targetFollowers = latestStats.target2036,
            monthsElapsed = 1L,
            totalMonths = remainingMonths
        )

        val requiredMonthly = nextMonthProjection - latestStats.currentFollowers
        val requiredYearly = requiredMonthly * 12
        val requiredWeekly = (requiredMonthly / 4.33).toLong()
        val requiredDaily = (requiredMonthly / 30.44).toLong()

        val actualMonthlyRate = computeActualMonthlyRate(history)

        val projectedFinish = if (actualMonthlyRate != null && actualMonthlyRate > 0) {
            val monthsToFinish = remaining.toDouble() / actualMonthlyRate
            nowInstant.plus((monthsToFinish * 30.44).roundToLong(), ChronoUnit.DAYS)
        } else null

        val status = determineStatus(actualMonthlyRate, requiredMonthly.toDouble())

        // Build monthly projection points from now to deadline using logistic curve
        val monthlyPoints = buildMonthlyPoints(
            currentFollowers = latestStats.currentFollowers,
            targetFollowers = latestStats.target2036,
            totalMonths = remainingMonths,
            history = history,
            nowInstant = nowInstant,
            deadline = deadline
        )

        return PlatformProjection(
            platformType = platform.name,
            currentFollowers = latestStats.currentFollowers,
            target2036 = latestStats.target2036,
            requiredYearlyGain = requiredYearly,
            requiredMonthlyGain = requiredMonthly,
            requiredWeeklyGain = requiredWeekly,
            requiredDailyGain = requiredDaily,
            projectedFinishDateEpochMillis = projectedFinish?.toEpochMilli(),
            status = status,
            monthlyProjectionPoints = monthlyPoints
        )
    }

    /**
     * Builds a list of monthly ProjectionPoints from the earliest historical entry to the deadline.
     * Each point carries both a projected value (logistic interpolation from current to target)
     * and any actual recorded value for that month.
     */
    private fun buildMonthlyPoints(
        currentFollowers: Long,
        targetFollowers: Long,
        totalMonths: Long,
        history: List<PlatformStats>,
        nowInstant: Instant,
        deadline: Instant
    ): List<ProjectionPoint> {
        val points = mutableListOf<ProjectionPoint>()
        val startDate = nowInstant.atZone(ZoneOffset.UTC).toLocalDate().withDayOfMonth(1)
        val endDate = deadline.atZone(ZoneOffset.UTC).toLocalDate().withDayOfMonth(1)

        // Build a lookup of actual values by year-month
        val actualByYearMonth = history.associate { stat ->
            val ld = Instant.ofEpochMilli(stat.dateRecordedEpochMillis).atZone(ZoneOffset.UTC).toLocalDate()
            "${ld.year}-${ld.monthValue}" to stat.currentFollowers
        }

        var cursor = startDate
        var monthOffset = 0L
        while (!cursor.isAfter(endDate)) {
            val pointInstant = cursor.atStartOfDay().toInstant(ZoneOffset.UTC)
            
            val projectedValue = calculateLogisticProjection(
                currentFollowers = currentFollowers,
                targetFollowers = targetFollowers,
                monthsElapsed = monthOffset,
                totalMonths = totalMonths
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
     * Computes the average monthly growth rate from historical entries using
     * the difference between consecutive sorted entries.
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

            // Normalise to a monthly rate (30.44 average days per month)
            val monthlyRate = followerDiff / daysBetween * 30.44
            deltas.add(monthlyRate)
        }

        return if (deltas.isEmpty()) null else deltas.average()
    }

    /**
     * Determines growth status.
     * AHEAD:    actual >= required * (1 + threshold)
     * ON_TRACK: within ±threshold of required
     * BEHIND:   actual < required * (1 - threshold)
     * UNKNOWN:  no actual rate available
     */
    private fun determineStatus(actualRate: Double?, requiredRate: Double): GrowthStatus {
        if (actualRate == null) return GrowthStatus.UNKNOWN
        return when {
            actualRate >= requiredRate * (1 + STATUS_THRESHOLD) -> GrowthStatus.AHEAD
            actualRate < requiredRate * (1 - STATUS_THRESHOLD) -> GrowthStatus.BEHIND
            else -> GrowthStatus.ON_TRACK
        }
    }

    /** Returns the most recent recorded PlatformStats per platform. */
    private fun latestStatsPerPlatform(stats: List<PlatformStats>): Map<PlatformType, PlatformStats> =
        stats
            .groupBy { it.platformType }
            .mapValues { (_, entries) -> entries.maxByOrNull { it.dateRecordedEpochMillis }!! }

    /**
     * Calculates the projected follower count using a Normalized Logistic Function (S-curve).
     * This models realistic audience growth: slow initially, rapid acceleration in the middle,
     * and tapering off as it approaches the final target.
     */
    private fun calculateLogisticProjection(
        currentFollowers: Long,
        targetFollowers: Long,
        monthsElapsed: Long,
        totalMonths: Long
    ): Long {
        if (totalMonths <= 0) return targetFollowers
        if (monthsElapsed <= 0) return currentFollowers
        if (monthsElapsed >= totalMonths) return targetFollowers

        val t = monthsElapsed.toDouble() / totalMonths.toDouble()
        
        // k defines steepness (acceleration). t0 is the midpoint (inflection).
        val k = 10.0
        val t0 = 0.7 

        fun s(x: Double): Double = 1.0 / (1.0 + exp(-k * (x - t0)))

        val s0 = s(0.0)
        val s1 = s(1.0)
        val st = s(t)

        val sNorm = (st - s0) / (s1 - s0)
        
        val projected = currentFollowers + (targetFollowers - currentFollowers) * sNorm
        return projected.roundToLong()
    }
}
