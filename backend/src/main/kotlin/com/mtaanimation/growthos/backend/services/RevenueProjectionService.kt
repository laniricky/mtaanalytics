package com.mtaanimation.growthos.backend.services

import com.mtaanimation.growthos.backend.constants.GrowthConstants
import com.mtaanimation.growthos.backend.repositories.RevenueRepository
import com.mtaanimation.growthos.shared.models.revenue.RevenueEntryDto
import com.mtaanimation.growthos.shared.projection.GrowthStatus
import com.mtaanimation.growthos.shared.projection.RevenueProjection
import com.mtaanimation.growthos.shared.projection.RevenueProjectionPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.exp

class RevenueProjectionService(private val revenueRepository: RevenueRepository) {

    companion object {
        private const val STATUS_THRESHOLD = 0.05

        private val JOURNEY_START: Instant =
            LocalDate.of(GrowthConstants.START_YEAR, GrowthConstants.START_MONTH, 1)
                .atStartOfDay().toInstant(ZoneOffset.UTC)

        private val JOURNEY_END: Instant =
            LocalDate.of(GrowthConstants.END_YEAR, GrowthConstants.END_MONTH, 1)
                .atStartOfDay().toInstant(ZoneOffset.UTC)
    }

    suspend fun computeRevenueProjection(userId: UUID): RevenueProjection {
        val allRevenue = revenueRepository.getAllRevenue(userId)
        
        // Find the current month's revenue (or 0 if none yet)
        val now = LocalDate.now(ZoneOffset.UTC)
        val currentMonthYearString = String.format("%04d-%02d", now.year, now.monthValue)
        val currentMonthlyRevenue = allRevenue.find { it.monthYear == currentMonthYearString }?.totalRevenue ?: 0.0
        
        val base = GrowthConstants.REVENUE_BASE_2026
        val target = GrowthConstants.REVENUE_TARGET_2036
        
        val nowInstant = Instant.now()
        val nowZoned = nowInstant.atZone(ZoneOffset.UTC)
        val deadlineZoned = JOURNEY_END.atZone(ZoneOffset.UTC)
        val remainingMonths = ChronoUnit.MONTHS.between(nowZoned, deadlineZoned).coerceAtLeast(1)

        val monthsElapsed = ChronoUnit.MONTHS.between(
            JOURNEY_START.atZone(ZoneOffset.UTC), nowZoned
        ).coerceAtLeast(0)

        val percentageComplete = if (target > 0) (currentMonthlyRevenue / target) * 100.0 else 0.0

        val milestoneToday = calculateFixedLogisticProjection(
            base = base,
            target = target,
            monthsElapsed = monthsElapsed,
            totalMonths = GrowthConstants.TOTAL_MONTHS
        )

        val varianceRevenue = currentMonthlyRevenue - milestoneToday
        val variancePct = if (milestoneToday > 0) (varianceRevenue / milestoneToday) * 100.0 else 0.0

        val nextMonthMilestone = calculateFixedLogisticProjection(
            base = base,
            target = target,
            monthsElapsed = monthsElapsed + 1,
            totalMonths = GrowthConstants.TOTAL_MONTHS
        )
        val requiredMonthlyGrowth = (nextMonthMilestone - milestoneToday).coerceAtLeast(0.0)
        
        val actualMonthlyGain = computeActualMonthlyRate(allRevenue)
        val status = determineStatusFromVariance(variancePct)

        val monthlyPoints = buildMonthlyPoints(
            base = base,
            target = target,
            history = allRevenue
        )

        return RevenueProjection(
            currentMonthlyRevenue = currentMonthlyRevenue,
            target2036 = target,
            percentageComplete = percentageComplete,
            requiredMonthlyGrowth = requiredMonthlyGrowth,
            deadlineEpochMillis = JOURNEY_END.toEpochMilli(),
            status = status,
            monthlyProjectionPoints = monthlyPoints,
            milestoneTargetRevenue = milestoneToday,
            varianceRevenue = varianceRevenue,
            variancePercentage = variancePct,
            actualMonthlyGain = actualMonthlyGain
        )
    }

    private fun buildMonthlyPoints(
        base: Double,
        target: Double,
        history: List<RevenueEntryDto>
    ): List<RevenueProjectionPoint> {
        val points = mutableListOf<RevenueProjectionPoint>()
        val startDate = JOURNEY_START.atZone(ZoneOffset.UTC).toLocalDate().withDayOfMonth(1)
        val endDate = JOURNEY_END.atZone(ZoneOffset.UTC).toLocalDate().withDayOfMonth(1)

        // Convert "YYYY-MM" strings to a map
        val actualByYearMonth = history.associate { stat ->
            // Assume format "2026-07"
            stat.monthYear to stat.totalRevenue
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
            val key = String.format("%04d-%02d", cursor.year, cursor.monthValue)
            val actualValue = actualByYearMonth[key]

            points.add(
                RevenueProjectionPoint(
                    dateEpochMillis = pointInstant.toEpochMilli(),
                    projectedValue = projectedValue.coerceAtLeast(0.0),
                    actualValue = actualValue
                )
            )
            cursor = cursor.plusMonths(1)
            monthOffset++
        }
        return points
    }

    private fun computeActualMonthlyRate(stats: List<RevenueEntryDto>): Double? {
        if (stats.size < 2) return null
        // Sort by parsing the monthYear string
        val sorted = stats.sortedBy { it.monthYear }
        val deltas = mutableListOf<Double>()
        for (i in 1 until sorted.size) {
            val prev = sorted[i - 1]
            val curr = sorted[i]
            val revDiff = curr.totalRevenue - prev.totalRevenue
            
            val prevParts = prev.monthYear.split("-")
            val currParts = curr.monthYear.split("-")
            val prevDate = LocalDate.of(prevParts[0].toInt(), prevParts[1].toInt(), 1)
            val currDate = LocalDate.of(currParts[0].toInt(), currParts[1].toInt(), 1)
            
            val monthsBetween = ChronoUnit.MONTHS.between(prevDate, currDate).coerceAtLeast(1)
            deltas.add(revDiff / monthsBetween)
        }
        return if (deltas.isEmpty()) null else deltas.average()
    }

    private fun determineStatusFromVariance(variancePercentage: Double): GrowthStatus = when {
        variancePercentage > STATUS_THRESHOLD * 100 -> GrowthStatus.AHEAD
        variancePercentage < -STATUS_THRESHOLD * 100 -> GrowthStatus.BEHIND
        else -> GrowthStatus.ON_TRACK
    }

    private fun calculateFixedLogisticProjection(
        base: Double,
        target: Double,
        monthsElapsed: Long,
        totalMonths: Long
    ): Double {
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
        return projected
    }
}
