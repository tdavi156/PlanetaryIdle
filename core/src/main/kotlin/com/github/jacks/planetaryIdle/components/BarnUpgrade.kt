package com.github.jacks.planetaryIdle.components

import java.math.BigDecimal

enum class BarnUpgradeCategory { SOIL, EXPERTISE, VALUE, EFFICIENCY, AUTOMATION }

enum class BarnUpgrade(
    val displayName: String,
    val description: String,
    val baseCost: BigDecimal,
    /** Multiplied by this per purchased level to get the next level's cost. */
    val costScaling: BigDecimal,
    val maxLevel: Int,
    val category: BarnUpgradeCategory,
    /** Fixed position on the node canvas (bottom-left origin). */
    val nodeX: Float,
    val nodeY: Float,
) {
    // ── Root ──────────────────────────────────────────────────────────────────
    SOIL(
        "Soil Upgrade",
        "Upgrade the soil to boost crop growth speed by 1.5× and unlock the next crop color.\nResets all owned crops and gold.",
        BigDecimal("1000000"), BigDecimal("5"), 25,
        BarnUpgradeCategory.SOIL,
        80f, 340f
    ),

    // ── Branches from SOIL ────────────────────────────────────────────────────
    KITCHEN(
        "Kitchen",
        "Unlocks the Kitchen tab, allowing research of new crop types and recipes.\nThis upgrade persists through all prestiges.",
        BigDecimal("1000000000000000"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.SOIL,
        80f, 530f
    ),
    OBSERVATORY(
        "Observatory",
        "Unlocks the Observatory, where you can spend Insight to research Discoveries that massively boost production.\nPersists through all prestiges.",
        BigDecimal("1e20"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.SOIL,
        80f, 720f
    ),
    IMPROVED_SEEDS(
        "Improved Seeds",
        "Increases the harvest speed of all crops by 5% per level.",
        BigDecimal("100000000"), BigDecimal("10"), 20,
        BarnUpgradeCategory.EFFICIENCY,
        280f, 530f
    ),
    IMPROVED_SOIL_QUALITY(
        "Improved Soil Quality",
        "Increases the speed bonus of each Soil Upgrade by 5% per level.",
        BigDecimal("100000000"), BigDecimal("10"), 10,
        BarnUpgradeCategory.EFFICIENCY,
        480f, 530f
    ),
    RED_VALUE(
        "Red Value",
        "Increases the payout of all red crops by 10% per level.",
        BigDecimal("500000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        280f, 340f
    ),
    RED_EXPERTISE(
        "Red Expertise",
        "Multiplies the value of red crops by the number of red crop types researched in the Kitchen.",
        BigDecimal("1000000"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        480f, 340f
    ),

    // ── Expertise chain + value branches ────────────────────────────────────
    ORANGE_EXPERTISE(
        "Orange Expertise",
        "Multiplies the value of orange crops by the number of orange crop types researched.",
        BigDecimal("10000000"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        680f, 340f
    ),
    ORANGE_VALUE(
        "Orange Value",
        "Increases the payout of all orange crops by 10% per level.",
        BigDecimal("5000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        680f, 530f
    ),

    YELLOW_EXPERTISE(
        "Yellow Expertise",
        "Multiplies the value of yellow crops by the number of yellow crop types researched.",
        BigDecimal("1000000000"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        880f, 340f
    ),
    YELLOW_VALUE(
        "Yellow Value",
        "Increases the payout of all yellow crops by 10% per level.",
        BigDecimal("50000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        880f, 530f
    ),

    GREEN_EXPERTISE(
        "Green Expertise",
        "Multiplies the value of green crops by the number of green crop types researched.",
        BigDecimal("1000000000000"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        1080f, 340f
    ),
    GREEN_VALUE(
        "Green Value",
        "Increases the payout of all green crops by 10% per level.",
        BigDecimal("500000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        1080f, 530f
    ),

    BLUE_EXPERTISE(
        "Blue Expertise",
        "Multiplies the value of blue crops by the number of blue crop types researched.",
        BigDecimal("1e16"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        1280f, 340f
    ),
    BLUE_VALUE(
        "Blue Value",
        "Increases the payout of all blue crops by 10% per level.",
        BigDecimal("5000000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        1280f, 530f
    ),

    PURPLE_EXPERTISE(
        "Purple Expertise",
        "Multiplies the value of purple crops by the number of purple crop types researched.",
        BigDecimal("1e21"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        1480f, 340f
    ),
    PURPLE_VALUE(
        "Purple Value",
        "Increases the payout of all purple crops by 10% per level.",
        BigDecimal("50000000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        1480f, 530f
    ),

    PINK_EXPERTISE(
        "Pink Expertise",
        "Multiplies the value of pink crops by the number of pink crop types researched.",
        BigDecimal("1e27"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        1680f, 340f
    ),
    PINK_VALUE(
        "Pink Value",
        "Increases the payout of all pink crops by 10% per level.",
        BigDecimal("500000000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        1680f, 530f
    ),

    BROWN_EXPERTISE(
        "Brown Expertise",
        "Multiplies the value of brown crops by the number of brown crop types researched.",
        BigDecimal("1e34"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        1880f, 340f
    ),
    BROWN_VALUE(
        "Brown Value",
        "Increases the payout of all brown crops by 10% per level.",
        BigDecimal("5000000000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        1880f, 530f
    ),

    WHITE_EXPERTISE(
        "White Expertise",
        "Multiplies the value of white crops by the number of white crop types researched.",
        BigDecimal("1e42"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        2080f, 340f
    ),
    WHITE_VALUE(
        "White Value",
        "Increases the payout of all white crops by 10% per level.",
        BigDecimal("50000000000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        2080f, 530f
    ),

    BLACK_EXPERTISE(
        "Black Expertise",
        "Multiplies the value of black crops by the number of black crop types researched.",
        BigDecimal("1e51"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        2280f, 340f
    ),
    BLACK_VALUE(
        "Black Value",
        "Increases the payout of all black crops by 10% per level.",
        BigDecimal("500000000000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        2280f, 530f
    ),

    // ── Automation branch ─────────────────────────────────────────────────────
    AUTOMATION_BASIC(
        "Basic Automation",
        "Unlocks the Automation panel. Automatically purchases enabled crops once per second.",
        BigDecimal("1e10"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        80f, 140f
    ),
    AUTOMATION_SPEED_1(
        "Automation Speed I",
        "Increases automation purchase rate to 4 times per second.",
        BigDecimal("1e13"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        280f, 140f
    ),
    AUTOMATION_SPEED_2(
        "Automation Speed II",
        "Increases automation purchase rate to 10 times per second.",
        BigDecimal("1e15"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        480f, 140f
    ),
    AUTOMATION_SPEED_3(
        "Maximum Automation",
        "Automation purchases happen every frame (60 times per second).",
        BigDecimal("1e18"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        680f, 140f
    ),
    AUTOMATION_KITCHEN(
        "Kitchen Automation",
        "Unlocks auto-research in the Kitchen. Researchers automatically restart with the same inputs on completion.",
        BigDecimal("1e16"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        880f, 140f
    ),
    AUTOMATION_RECIPE(
        "Recipe Optimizer",
        "Unlocks auto best recipe. Automatically assigns the highest-yield non-conflicting recipes.",
        BigDecimal("1e18"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        1080f, 140f
    ),
    AUTOMATION_SOIL(
        "Soil Automation",
        "Automatically purchases Soil upgrades when you can afford them.",
        BigDecimal("1e11"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        80f, 20f
    ),
    AUTOMATION_BULK_1(
        "Bulk Buy I",
        "Each automation tick purchases up to 5 crops at once.",
        BigDecimal("1e14"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        280f, 20f
    ),
    AUTOMATION_BULK_2(
        "Bulk Buy II",
        "Each automation tick purchases up to 10 crops at once.",
        BigDecimal("1e17"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        480f, 20f
    ),
    AUTOMATION_BULK_MAX(
        "Buy Max",
        "Each automation tick purchases the maximum number of crops you can afford.",
        BigDecimal("1e19"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        680f, 20f
    );

    val prefKey: String get() = "barn_upgrade_${name.lowercase()}_level"

    companion object {
        /**
         * Defines which upgrades must have level >= 1 before this upgrade is revealed.
         * Upgrades not in this map are never revealed (should not happen) or always visible (SOIL).
         */
        val prerequisites: Map<BarnUpgrade, List<BarnUpgrade>> = mapOf(
            KITCHEN                to listOf(SOIL),
            OBSERVATORY            to listOf(KITCHEN),
            IMPROVED_SEEDS         to listOf(SOIL),
            IMPROVED_SOIL_QUALITY  to listOf(SOIL),
            RED_VALUE              to listOf(SOIL),
            RED_EXPERTISE          to listOf(SOIL),
            ORANGE_EXPERTISE       to listOf(RED_EXPERTISE),
            ORANGE_VALUE           to listOf(ORANGE_EXPERTISE),
            YELLOW_EXPERTISE       to listOf(ORANGE_EXPERTISE),
            YELLOW_VALUE           to listOf(YELLOW_EXPERTISE),
            GREEN_EXPERTISE        to listOf(YELLOW_EXPERTISE),
            GREEN_VALUE            to listOf(GREEN_EXPERTISE),
            BLUE_EXPERTISE         to listOf(GREEN_EXPERTISE),
            BLUE_VALUE             to listOf(BLUE_EXPERTISE),
            PURPLE_EXPERTISE       to listOf(BLUE_EXPERTISE),
            PURPLE_VALUE           to listOf(PURPLE_EXPERTISE),
            PINK_EXPERTISE         to listOf(PURPLE_EXPERTISE),
            PINK_VALUE             to listOf(PINK_EXPERTISE),
            BROWN_EXPERTISE        to listOf(PINK_EXPERTISE),
            BROWN_VALUE            to listOf(BROWN_EXPERTISE),
            WHITE_EXPERTISE        to listOf(BROWN_EXPERTISE),
            WHITE_VALUE            to listOf(WHITE_EXPERTISE),
            BLACK_EXPERTISE        to listOf(WHITE_EXPERTISE),
            BLACK_VALUE            to listOf(BLACK_EXPERTISE),

            // Automation branch
            AUTOMATION_BASIC    to listOf(SOIL),
            AUTOMATION_SPEED_1  to listOf(AUTOMATION_BASIC),
            AUTOMATION_SPEED_2  to listOf(AUTOMATION_SPEED_1),
            AUTOMATION_SPEED_3  to listOf(AUTOMATION_SPEED_2),
            AUTOMATION_KITCHEN  to listOf(AUTOMATION_BASIC, KITCHEN),
            AUTOMATION_RECIPE   to listOf(AUTOMATION_KITCHEN),
            AUTOMATION_SOIL     to listOf(AUTOMATION_BASIC),
            AUTOMATION_BULK_1   to listOf(AUTOMATION_SPEED_1),
            AUTOMATION_BULK_2   to listOf(AUTOMATION_BULK_1),
            AUTOMATION_BULK_MAX to listOf(AUTOMATION_BULK_2),
        )

        /** Edges used to draw connection lines: each pair is (parent, child). */
        val connections: List<Pair<BarnUpgrade, BarnUpgrade>> by lazy {
            prerequisites.flatMap { (child, parents) -> parents.map { parent -> parent to child } }
        }

        /** Maps a PlanetResources color name to the corresponding value upgrade. */
        val valueUpgradeFor: Map<String, BarnUpgrade> = mapOf(
            "red"    to RED_VALUE,
            "orange" to ORANGE_VALUE,
            "yellow" to YELLOW_VALUE,
            "green"  to GREEN_VALUE,
            "blue"   to BLUE_VALUE,
            "purple" to PURPLE_VALUE,
            "pink"   to PINK_VALUE,
            "brown"  to BROWN_VALUE,
            "white"  to WHITE_VALUE,
            "black"  to BLACK_VALUE,
        )

        /** Maps a PlanetResources color name to the corresponding expertise upgrade. */
        val expertiseUpgradeFor: Map<String, BarnUpgrade> = mapOf(
            "red"    to RED_EXPERTISE,
            "orange" to ORANGE_EXPERTISE,
            "yellow" to YELLOW_EXPERTISE,
            "green"  to GREEN_EXPERTISE,
            "blue"   to BLUE_EXPERTISE,
            "purple" to PURPLE_EXPERTISE,
            "pink"   to PINK_EXPERTISE,
            "brown"  to BROWN_EXPERTISE,
            "white"  to WHITE_EXPERTISE,
            "black"  to BLACK_EXPERTISE,
        )
    }
}
