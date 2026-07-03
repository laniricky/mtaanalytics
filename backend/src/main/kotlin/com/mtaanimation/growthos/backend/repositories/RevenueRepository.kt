package com.mtaanimation.growthos.backend.repositories

import com.mtaanimation.growthos.backend.db.DatabaseFactory.dbQuery
import com.mtaanimation.growthos.backend.database.RevenueTable
import com.mtaanimation.growthos.shared.models.revenue.RecordRevenueRequest
import com.mtaanimation.growthos.shared.models.revenue.RevenueEntryDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import java.util.UUID

class RevenueRepository {

    suspend fun recordRevenue(userId: UUID, request: RecordRevenueRequest): RevenueEntryDto? = dbQuery {
        val existing = RevenueTable
            .select { (RevenueTable.userId eq userId) and (RevenueTable.monthYear eq request.monthYear) }
            .singleOrNull()

        if (existing != null) {
            RevenueTable.update({ RevenueTable.id eq existing[RevenueTable.id] }) {
                it[youtubeRevenue] = request.youtubeRevenue
                it[tiktokRevenue] = request.tiktokRevenue
                it[facebookRevenue] = request.facebookRevenue
                it[instagramRevenue] = request.instagramRevenue
                it[sponsors] = request.sponsors
                it[merchandise] = request.merchandise
                it[websiteIncome] = request.websiteIncome
                it[otherIncome] = request.otherIncome
            }
        } else {
            RevenueTable.insert {
                it[id] = UUID.randomUUID()
                it[this.userId] = userId
                it[monthYear] = request.monthYear
                it[youtubeRevenue] = request.youtubeRevenue
                it[tiktokRevenue] = request.tiktokRevenue
                it[facebookRevenue] = request.facebookRevenue
                it[instagramRevenue] = request.instagramRevenue
                it[sponsors] = request.sponsors
                it[merchandise] = request.merchandise
                it[websiteIncome] = request.websiteIncome
                it[otherIncome] = request.otherIncome
                it[createdAt] = LocalDateTime.now()
            }
        }

        RevenueTable
            .select { (RevenueTable.userId eq userId) and (RevenueTable.monthYear eq request.monthYear) }
            .map { it.toRevenueEntryDto() }
            .singleOrNull()
    }

    suspend fun getAllRevenue(userId: UUID): List<RevenueEntryDto> = dbQuery {
        RevenueTable
            .select { RevenueTable.userId eq userId }
            .orderBy(RevenueTable.monthYear to SortOrder.DESC)
            .map { it.toRevenueEntryDto() }
    }

    private fun ResultRow.toRevenueEntryDto(): RevenueEntryDto {
        val yt = this[RevenueTable.youtubeRevenue]
        val tt = this[RevenueTable.tiktokRevenue]
        val fb = this[RevenueTable.facebookRevenue]
        val ig = this[RevenueTable.instagramRevenue]
        val sp = this[RevenueTable.sponsors]
        val merch = this[RevenueTable.merchandise]
        val web = this[RevenueTable.websiteIncome]
        val other = this[RevenueTable.otherIncome]
        
        val total = yt + tt + fb + ig + sp + merch + web + other

        return RevenueEntryDto(
            id = this[RevenueTable.id].toString(),
            userId = this[RevenueTable.userId].toString(),
            monthYear = this[RevenueTable.monthYear],
            youtubeRevenue = yt,
            tiktokRevenue = tt,
            facebookRevenue = fb,
            instagramRevenue = ig,
            sponsors = sp,
            merchandise = merch,
            websiteIncome = web,
            otherIncome = other,
            totalRevenue = total
        )
    }
}
