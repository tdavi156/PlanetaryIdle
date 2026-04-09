package com.github.jacks.planetaryIdle.components

import java.math.BigDecimal

// ── Achievement bonus types ───────────────────────────────────────────────────

sealed class AchievementBonus(val bonusDesc: String) {
    /** Red crop payout multiplied by [multiplier] in FarmModel. */
    class RedProductionBonus(val multiplier: BigDecimal) : AchievementBonus("Red crop production +15%")
    /** All crop payout multiplied by [multiplier] in FarmModel. */
    class AllProductionBonus(val multiplier: BigDecimal) : AchievementBonus("All crop production +10%")
    /** All gold income multiplied by [multiplier] in FarmModel. */
    class GoldIncomeBonus(val multiplier: BigDecimal) : AchievementBonus("All gold income +5%")
    /** Soil upgrade cost multiplied by [multiplier] in BarnViewModel. */
    class SoilCostDiscount(val multiplier: BigDecimal) : AchievementBonus("Soil upgrades cost 10% less")
    /** Changes soil effectiveness base from x1.5 to x1.75 in BarnViewModel. */
    object PerfectSoilBonus : AchievementBonus("Soil effectiveness: x1.5 → x1.75")
    /** Changes achievement multiplier scale from 1.05 to 1.06 per achievement. */
    object TheEndBonus : AchievementBonus("Achievement bonus: x1.05 → x1.06 per achievement")
    /** Kitchen research completes 20% faster. */
    class ResearchSpeedBonus(val multiplier: Float) : AchievementBonus("Kitchen research speed +20%")
    /** Informational only — no code effect. */
    class DescriptiveOnly(desc: String) : AchievementBonus(desc)
}

// ── Achievement definitions ───────────────────────────────────────────────────

enum class AchievementType(val typeName: String) {
    BASIC_ACHIEVEMENT("basic_achievement")
}

enum class Achievements(
    val achId: String,
    val achName: String,
    val achDesc: String,
    val bonus: AchievementBonus? = null,
) {
    // ── Red crop milestones ───────────────────────────────────────────────────
    ACH_RED_10(
        "red_10", "Easy Start", "Own 10 Red crops"
    ),
    ACH_RED_50(
        "red_50", "Making Progress", "Own 50 Red crops"
    ),
    ACH_RED_250(
        "red_250", "Red Giant", "Own 250 Red crops",
        AchievementBonus.RedProductionBonus(BigDecimal("1.15"))
    ),
    ACH_RED_500(
        "red_500", "Red Titan", "Own 500 Red crops"
    ),
    ACH_RED_1000(
        "red_1000", "Red Horizon", "Own 1,000 Red crops"
    ),
    ACH_RED_5000(
        "red_5000", "Red Dominion", "Own 5,000 Red crops"
    ),

    // ── Orange crop milestones ────────────────────────────────────────────────
    ACH_ORANGE_10(
        "orange_10", "A Taste of Orange", "Own 10 Orange crops"
    ),
    ACH_ORANGE_100(
        "orange_100", "Orange Rush", "Own 100 Orange crops"
    ),
    ACH_ORANGE_1000(
        "orange_1000", "Orange Empire", "Own 1,000 Orange crops"
    ),

    // ── Yellow crop milestones ────────────────────────────────────────────────
    ACH_YELLOW_10(
        "yellow_10", "Yellow Fever", "Own 10 Yellow crops"
    ),
    ACH_YELLOW_100(
        "yellow_100", "Fields of Gold", "Own 100 Yellow crops"
    ),
    ACH_YELLOW_1000(
        "yellow_1000", "Golden Age", "Own 1,000 Yellow crops"
    ),

    // ── Green crop milestones ─────────────────────────────────────────────────
    ACH_GREEN_10(
        "green_10", "Going Green", "Own 10 Green crops",
        AchievementBonus.DescriptiveOnly("Unlocks the Barn!")
    ),
    ACH_GREEN_100(
        "green_100", "Green Giant", "Own 100 Green crops"
    ),
    ACH_GREEN_1000(
        "green_1000", "Green World", "Own 1,000 Green crops"
    ),

    // ── Blue crop milestones ──────────────────────────────────────────────────
    ACH_BLUE_10(
        "blue_10", "Feelin' Blue", "Own 10 Blue crops"
    ),
    ACH_BLUE_100(
        "blue_100", "Blue Wave", "Own 100 Blue crops"
    ),
    ACH_BLUE_1000(
        "blue_1000", "Blue Ocean", "Own 1,000 Blue crops"
    ),

    // ── Purple crop milestones ────────────────────────────────────────────────
    ACH_PURPLE_10(
        "purple_10", "Purple Patch", "Own 10 Purple crops"
    ),
    ACH_PURPLE_100(
        "purple_100", "Purple Rain", "Own 100 Purple crops"
    ),
    ACH_PURPLE_1000(
        "purple_1000", "Purple Reign", "Own 1,000 Purple crops"
    ),

    // ── Pink crop milestones ──────────────────────────────────────────────────
    ACH_PINK_10(
        "pink_10", "In the Pink", "Own 10 Pink crops"
    ),
    ACH_PINK_100(
        "pink_100", "Pink Parade", "Own 100 Pink crops"
    ),
    ACH_PINK_1000(
        "pink_1000", "Pink Planet", "Own 1,000 Pink crops"
    ),

    // ── Brown crop milestones ─────────────────────────────────────────────────
    ACH_BROWN_10(
        "brown_10", "Muddy Hands", "Own 10 Brown crops"
    ),
    ACH_BROWN_100(
        "brown_100", "Brown Thumb", "Own 100 Brown crops"
    ),
    ACH_BROWN_1000(
        "brown_1000", "Brown Earth", "Own 1,000 Brown crops"
    ),

    // ── White crop milestones ─────────────────────────────────────────────────
    ACH_WHITE_10(
        "white_10", "Bright as Snow", "Own 10 White crops"
    ),
    ACH_WHITE_100(
        "white_100", "White Out", "Own 100 White crops"
    ),
    ACH_WHITE_1000(
        "white_1000", "White World", "Own 1,000 White crops"
    ),

    // ── Black crop milestones ─────────────────────────────────────────────────
    ACH_BLACK_1(
        "black_1", "Into the Void", "Own 1 Black crop"
    ),
    ACH_BLACK_10(
        "black_10", "Dark Matter", "Own 10 Black crops"
    ),
    ACH_BLACK_100(
        "black_100", "Black Hole", "Own 100 Black crops"
    ),

    // ── Combined crop milestone ───────────────────────────────────────────────
    ACH_FULL_SPECTRUM(
        "combined_full_spectrum", "Full Spectrum", "Own 10 of every color",
        AchievementBonus.AllProductionBonus(BigDecimal("1.10"))
    ),

    // ── Gold milestones ───────────────────────────────────────────────────────
    ACH_GOLD_1M(
        "gold_1m", "Millionaire!", "Have 1,000,000 Gold Coins"
    ),
    ACH_GOLD_1B(
        "gold_1b", "Billionaire!", "Have 1,000,000,000 Gold Coins"
    ),
    ACH_GOLD_1T(
        "gold_1t", "Trillionaire!", "Have 1,000,000,000,000 Gold Coins",
        AchievementBonus.GoldIncomeBonus(BigDecimal("1.05"))
    ),
    ACH_GOLD_1Q(
        "gold_1q", "Quadrillionaire!", "Have 1,000,000,000,000,000 Gold Coins"
    ),
    ACH_GOLD_1E33(
        "gold_1e33", "Decillionaire!", "Have 1e33 Gold Coins"
    ),
    ACH_GOLD_1E50(
        "gold_1e50", "The End", "Have 1e50 Gold Coins",
        AchievementBonus.TheEndBonus
    ),

    // ── Soil milestones ───────────────────────────────────────────────────────
    ACH_SOIL_1(
        "soil_1", "Fresh Soil", "Have 1 Soil upgrade"
    ),
    ACH_SOIL_5(
        "soil_5", "Tilled Earth", "Have 5 Soil upgrades",
        AchievementBonus.SoilCostDiscount(BigDecimal("0.90"))
    ),
    ACH_SOIL_10(
        "soil_10", "Seasoned Farmer", "Have 10 Soil upgrades"
    ),
    ACH_SOIL_25(
        "soil_25", "Perfect Soil", "Have 25 Soil upgrades",
        AchievementBonus.PerfectSoilBonus
    ),

    // ── Barn milestones ───────────────────────────────────────────────────────
    ACH_BARN_1(
        "barn_1", "First Nail", "Purchase 1 Barn upgrade"
    ),
    ACH_BARN_5(
        "barn_5", "Handyman", "Purchase 5 Barn upgrades"
    ),
    ACH_BARN_10(
        "barn_10", "Renovator", "Purchase 10 Barn upgrades"
    ),
    ACH_BARN_15(
        "barn_15", "Master Builder", "Purchase 15 Barn upgrades",
        AchievementBonus.ResearchSpeedBonus(1.2f)
    ),

    // ── Kitchen milestones ────────────────────────────────────────────────────
    ACH_KITCHEN_UNLOCK(
        "kitchen_unlock", "Sous Chef", "Unlock the Kitchen"
    ),
    ACH_KITCHEN_CROP_1(
        "kitchen_crop_1", "New Growth", "Discover your first new crop"
    ),
    ACH_KITCHEN_RECIPE_1(
        "kitchen_recipe_1", "First Recipe", "Discover your first recipe"
    ),
    ACH_KITCHEN_CROP_5(
        "kitchen_crop_5", "Budding Chef", "Discover 5 crops"
    ),
    ACH_KITCHEN_CROP_10(
        "kitchen_crop_10", "Growing Garden", "Discover 10 crops"
    ),
    ACH_KITCHEN_CROP_50(
        "kitchen_crop_50", "Full Garden", "Discover 50 crops"
    ),
    ACH_KITCHEN_RECIPE_5(
        "kitchen_recipe_5", "Recipe Book", "Discover 5 recipes"
    ),
    ACH_KITCHEN_RECIPE_10(
        "kitchen_recipe_10", "Culinary Arts", "Discover 10 recipes"
    ),
    ACH_KITCHEN_RECIPE_50(
        "kitchen_recipe_50", "Master Chef", "Discover 50 recipes"
    ),
}

// ── ECS component data classes ────────────────────────────────────────────────

data class AchievementConfiguration(
    val name: String = "",
    val completedAchievements: Set<String> = emptySet(),
    val multiplierScale: BigDecimal = BigDecimal("1.05"),
)

data class AchievementComponent(
    var multiplierScale: BigDecimal = BigDecimal("1.05"),
    var completedAchievements: MutableList<String> = mutableListOf(),
) {
    val achMultiplier: BigDecimal
        get() = if (completedAchievements.isEmpty()) BigDecimal.ONE
                else multiplierScale.pow(completedAchievements.count())
}
