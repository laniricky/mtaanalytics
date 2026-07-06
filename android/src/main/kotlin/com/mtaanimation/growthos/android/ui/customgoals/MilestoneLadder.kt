package com.mtaanimation.growthos.android.ui.customgoals

/**
 * Predefined progressive milestone ladders for each category.
 *
 * Steps are designed around the S-curve growth model:
 * - Early steps are small and frequent (motivation)
 * - Mid steps grow exponentially
 * - Later steps are large milestones aligned with the 2036 targets
 *
 * The ladder is purely computed — no database storage needed.
 * Only the NEXT uncompleted tier in each category is shown as "active".
 */
object MilestoneLadder {

    data class Tier(
        val target: Double,
        val label: String
    )

    data class Category(
        val key: String,       // "FOLLOWERS" | "VIEWS" | "REVENUE" | "EPISODES"
        val displayName: String,
        val emoji: String,
        val tiers: List<Tier>
    )

    /**
     * Combined followers across all platforms.
     * Base: ~120K (July 2026). Target: ~55M (July 2036).
     */
    val FOLLOWERS: Category = Category(
        key = "FOLLOWERS",
        displayName = "Followers",
        emoji = "👥",
        tiers = listOf(
            Tier(1_000.0,       "1K Followers"),
            Tier(2_000.0,       "2K Followers"),
            Tier(5_000.0,       "5K Followers"),
            Tier(10_000.0,      "10K Followers"),
            Tier(25_000.0,      "25K Followers"),
            Tier(50_000.0,      "50K Followers"),
            Tier(100_000.0,     "100K Followers"),
            Tier(150_000.0,     "150K Followers"),
            Tier(200_000.0,     "200K Followers"),
            Tier(250_000.0,     "250K Followers"),
            Tier(500_000.0,     "500K Followers"),
            Tier(750_000.0,     "750K Followers"),
            Tier(1_000_000.0,   "1M Followers"),
            Tier(2_000_000.0,   "2M Followers"),
            Tier(3_000_000.0,   "3M Followers"),
            Tier(5_000_000.0,   "5M Followers"),
            Tier(7_500_000.0,   "7.5M Followers"),
            Tier(10_000_000.0,  "10M Followers"),
            Tier(15_000_000.0,  "15M Followers"),
            Tier(20_000_000.0,  "20M Followers"),
            Tier(30_000_000.0,  "30M Followers"),
            Tier(40_000_000.0,  "40M Followers"),
            Tier(55_000_000.0,  "55M Followers — 2036 Goal 🎯"),
            Tier(100_000_000.0, "100M Followers"),
            Tier(500_000_000.0, "500M Followers"),
            Tier(1_000_000_000.0,"1 Billion Followers")
        )
    )

    /**
     * Total cumulative views across all episodes and all platforms.
     */
    val VIEWS: Category = Category(
        key = "VIEWS",
        displayName = "Total Views",
        emoji = "👁",
        tiers = listOf(
            Tier(1_000.0,         "1K Views"),
            Tier(5_000.0,         "5K Views"),
            Tier(10_000.0,        "10K Views"),
            Tier(25_000.0,        "25K Views"),
            Tier(50_000.0,        "50K Views"),
            Tier(100_000.0,       "100K Views"),
            Tier(250_000.0,       "250K Views"),
            Tier(500_000.0,       "500K Views"),
            Tier(1_000_000.0,     "1M Views"),
            Tier(2_500_000.0,     "2.5M Views"),
            Tier(5_000_000.0,     "5M Views"),
            Tier(10_000_000.0,    "10M Views"),
            Tier(25_000_000.0,    "25M Views"),
            Tier(50_000_000.0,    "50M Views"),
            Tier(100_000_000.0,   "100M Views"),
            Tier(250_000_000.0,   "250M Views"),
            Tier(500_000_000.0,   "500M Views"),
            Tier(1_000_000_000.0, "1 Billion Views 🎯")
        )
    )

    /**
     * Total cumulative revenue across all sources (USD).
     */
    val REVENUE: Category = Category(
        key = "REVENUE",
        displayName = "Revenue",
        emoji = "💰",
        tiers = listOf(
            Tier(100.0,       "\$100"),
            Tier(500.0,       "\$500"),
            Tier(1_000.0,     "\$1K"),
            Tier(5_000.0,     "\$5K"),
            Tier(10_000.0,    "\$10K"),
            Tier(25_000.0,    "\$25K"),
            Tier(50_000.0,    "\$50K"),
            Tier(100_000.0,   "\$100K"),
            Tier(250_000.0,   "\$250K"),
            Tier(500_000.0,   "\$500K"),
            Tier(1_000_000.0, "\$1M Revenue 🎯"),
            Tier(2_500_000.0, "\$2.5M Revenue"),
            Tier(5_000_000.0, "\$5M Revenue")
        )
    )

    /**
     * Total number of episodes created.
     */
    val EPISODES: Category = Category(
        key = "EPISODES",
        displayName = "Episodes",
        emoji = "🎬",
        tiers = listOf(
            Tier(1.0,    "First Episode"),
            Tier(5.0,    "5 Episodes"),
            Tier(10.0,   "10 Episodes"),
            Tier(25.0,   "25 Episodes"),
            Tier(50.0,   "50 Episodes"),
            Tier(100.0,  "100 Episodes"),
            Tier(200.0,  "200 Episodes"),
            Tier(365.0,  "365 Episodes — One a Day for a Year"),
            Tier(500.0,  "500 Episodes 🎯"),
            Tier(1_000.0,"1,000 Episodes"),
            Tier(2_000.0,"2,000 Episodes"),
            Tier(5_000.0,"5,000 Episodes")
        )
    )

    val ALL: List<Category> = listOf(FOLLOWERS, VIEWS, REVENUE, EPISODES)

    /** Returns completed tiers and the current active tier for a category given a current value. */
    fun computeProgress(category: Category, currentValue: Double): CategoryProgress {
        val completed = category.tiers.filter { currentValue >= it.target }
        val active = category.tiers.firstOrNull { currentValue < it.target }
        return CategoryProgress(
            category = category,
            currentValue = currentValue,
            completedTiers = completed,
            activeTier = active
        )
    }
}

data class CategoryProgress(
    val category: MilestoneLadder.Category,
    val currentValue: Double,
    val completedTiers: List<MilestoneLadder.Tier>,
    val activeTier: MilestoneLadder.Tier?  // null means all tiers complete
) {
    val progressFraction: Float
        get() {
            val active = activeTier ?: return 1f
            val prevTarget = completedTiers.lastOrNull()?.target ?: 0.0
            val range = active.target - prevTarget
            return if (range <= 0) 1f else ((currentValue - prevTarget) / range).toFloat().coerceIn(0f, 1f)
        }
}
