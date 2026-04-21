package com.github.jacks.planetaryIdle.data

import java.math.BigDecimal

enum class BarnUpgradeCategory { SOIL, EXPERTISE, VALUE, EFFICIENCY, AUTOMATION }

// ── Node layout overview ──────────────────────────────────────────────────────
//
//  Canvas: 4000 × 1800  (SOIL is the central hub at x=800, y=1100)
//
//  y=1500  OBSERVATORY
//  y=1300  KITCHEN
//  y=1100  IMP_SOIL_QUAL  IMP_SEEDS  [SOIL]  RED_EXP  ORG_EXP  YLW_EXP  GRN_EXP  BLU_EXP  PRP_EXP  PNK_EXP  BRN_EXP  WHT_EXP  BLK_EXP
//  y= 900                            RED_VAL  ORG_VAL  YLW_VAL  GRN_VAL  BLU_VAL  PRP_VAL  PNK_VAL  BRN_VAL  WHT_VAL  BLK_VAL
//  y= 700                     AUTO_BASIC
//  y= 500  AUTO_SOIL  AUTO_SPEED_1   AUTO_KITCHEN  AUTO_BULK_1
//  y= 300             AUTO_SPEED_2   AUTO_RECIPE   AUTO_BULK_2
//  y= 100             AUTO_SPEED_3               AUTO_BULK_MAX
//
//  x: 400  600        800            1000  1200  1400  1600  1800  2000  2200  2400  2600  2800
// ─────────────────────────────────────────────────────────────────────────────────────────────

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
        800f, 1100f
    ),

    // ── Up from SOIL ──────────────────────────────────────────────────────────
    KITCHEN(
        "Kitchen",
        "Unlocks the Kitchen tab, allowing research of new crop types and recipes.\nThis upgrade persists through all prestiges.",
        BigDecimal("1000000000000000"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.SOIL,
        800f, 1300f
    ),
    OBSERVATORY(
        "Observatory",
        "Unlocks the Observatory, where you can spend Insight to research Discoveries that massively boost production.\nPersists through all prestiges.",
        BigDecimal("1e20"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.SOIL,
        800f, 1500f
    ),

    // ── Left from SOIL ────────────────────────────────────────────────────────
    IMPROVED_SEEDS(
        "Improved Seeds",
        "Increases the harvest speed of all crops by 5% per level.",
        BigDecimal("100000000"), BigDecimal("10"), 20,
        BarnUpgradeCategory.EFFICIENCY,
        600f, 1100f
    ),
    IMPROVED_SOIL_QUALITY(
        "Improved Soil Quality",
        "Increases the speed bonus of each Soil Upgrade by 5% per level.",
        BigDecimal("100000000"), BigDecimal("10"), 10,
        BarnUpgradeCategory.EFFICIENCY,
        400f, 1100f
    ),

    // ── Right from SOIL — expertise chain (y=1100) ────────────────────────────
    RED_EXPERTISE(
        "Red Expertise",
        "Multiplies the value of red crops by the number of red crop types researched in the Kitchen.",
        BigDecimal("1000000"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        1000f, 1100f
    ),
    ORANGE_EXPERTISE(
        "Orange Expertise",
        "Multiplies the value of orange crops by the number of orange crop types researched.",
        BigDecimal("10000000"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        1200f, 1100f
    ),
    YELLOW_EXPERTISE(
        "Yellow Expertise",
        "Multiplies the value of yellow crops by the number of yellow crop types researched.",
        BigDecimal("1000000000"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        1400f, 1100f
    ),
    GREEN_EXPERTISE(
        "Green Expertise",
        "Multiplies the value of green crops by the number of green crop types researched.",
        BigDecimal("1000000000000"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        1600f, 1100f
    ),
    BLUE_EXPERTISE(
        "Blue Expertise",
        "Multiplies the value of blue crops by the number of blue crop types researched.",
        BigDecimal("1e16"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        1800f, 1100f
    ),
    PURPLE_EXPERTISE(
        "Purple Expertise",
        "Multiplies the value of purple crops by the number of purple crop types researched.",
        BigDecimal("1e21"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        2000f, 1100f
    ),
    PINK_EXPERTISE(
        "Pink Expertise",
        "Multiplies the value of pink crops by the number of pink crop types researched.",
        BigDecimal("1e27"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        2200f, 1100f
    ),
    BROWN_EXPERTISE(
        "Brown Expertise",
        "Multiplies the value of brown crops by the number of brown crop types researched.",
        BigDecimal("1e34"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        2400f, 1100f
    ),
    WHITE_EXPERTISE(
        "White Expertise",
        "Multiplies the value of white crops by the number of white crop types researched.",
        BigDecimal("1e42"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        2600f, 1100f
    ),
    BLACK_EXPERTISE(
        "Black Expertise",
        "Multiplies the value of black crops by the number of black crop types researched.",
        BigDecimal("1e51"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.EXPERTISE,
        2800f, 1100f
    ),

    // ── Value chain — one row below expertise (y=900) ─────────────────────────
    RED_VALUE(
        "Red Value",
        "Increases the payout of all red crops by 10% per level.",
        BigDecimal("500000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        1000f, 900f
    ),
    ORANGE_VALUE(
        "Orange Value",
        "Increases the payout of all orange crops by 10% per level.",
        BigDecimal("5000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        1200f, 900f
    ),
    YELLOW_VALUE(
        "Yellow Value",
        "Increases the payout of all yellow crops by 10% per level.",
        BigDecimal("50000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        1400f, 900f
    ),
    GREEN_VALUE(
        "Green Value",
        "Increases the payout of all green crops by 10% per level.",
        BigDecimal("500000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        1600f, 900f
    ),
    BLUE_VALUE(
        "Blue Value",
        "Increases the payout of all blue crops by 10% per level.",
        BigDecimal("5000000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        1800f, 900f
    ),
    PURPLE_VALUE(
        "Purple Value",
        "Increases the payout of all purple crops by 10% per level.",
        BigDecimal("50000000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        2000f, 900f
    ),
    PINK_VALUE(
        "Pink Value",
        "Increases the payout of all pink crops by 10% per level.",
        BigDecimal("500000000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        2200f, 900f
    ),
    BROWN_VALUE(
        "Brown Value",
        "Increases the payout of all brown crops by 10% per level.",
        BigDecimal("5000000000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        2400f, 900f
    ),
    WHITE_VALUE(
        "White Value",
        "Increases the payout of all white crops by 10% per level.",
        BigDecimal("50000000000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        2600f, 900f
    ),
    BLACK_VALUE(
        "Black Value",
        "Increases the payout of all black crops by 10% per level.",
        BigDecimal("500000000000000"), BigDecimal("2"), 100,
        BarnUpgradeCategory.VALUE,
        2800f, 900f
    ),

    // ── Automation branch — below SOIL ────────────────────────────────────────
    //
    //  AUTO_BASIC (x=800, y=700)
    //  ├── AUTO_SOIL    (x=600, y=500)
    //  ├── AUTO_SPEED_1 (x=800, y=500)
    //  │   ├── AUTO_SPEED_2   (x=800, y=300) → AUTO_SPEED_3 (x=800, y=100)
    //  │   └── AUTO_BULK_1    (x=1200,y=500) → AUTO_BULK_2 (x=1200,y=300) → AUTO_BULK_MAX (x=1200,y=100)
    //  └── AUTO_KITCHEN (x=1000,y=500) [also requires KITCHEN]
    //      └── AUTO_RECIPE   (x=1000,y=300)
    // ─────────────────────────────────────────────────────────────────────────
    AUTOMATION_BASIC(
        "Basic Automation",
        "Unlocks the Automation panel. Automatically purchases enabled crops once per second.",
        BigDecimal("1e10"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        800f, 700f
    ),
    AUTOMATION_SOIL(
        "Soil Automation",
        "Automatically purchases Soil upgrades when you can afford them.",
        BigDecimal("1e11"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        600f, 500f
    ),
    AUTOMATION_SPEED_1(
        "Automation Speed I",
        "Increases automation purchase rate to 4 times per second.",
        BigDecimal("1e13"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        800f, 500f
    ),
    AUTOMATION_SPEED_2(
        "Automation Speed II",
        "Increases automation purchase rate to 10 times per second.",
        BigDecimal("1e15"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        800f, 300f
    ),
    AUTOMATION_SPEED_3(
        "Maximum Automation",
        "Automation purchases happen every frame (60 times per second).",
        BigDecimal("1e18"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        800f, 100f
    ),
    AUTOMATION_KITCHEN(
        "Kitchen Automation",
        "Unlocks auto-research in the Kitchen. Researchers automatically restart with the same inputs on completion.",
        BigDecimal("1e16"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        1000f, 500f
    ),
    AUTOMATION_RECIPE(
        "Recipe Optimizer",
        "Unlocks auto best recipe. Automatically assigns the highest-yield non-conflicting recipes.",
        BigDecimal("1e18"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        1000f, 300f
    ),
    AUTOMATION_BULK_1(
        "Bulk Buy I",
        "Each automation tick purchases up to 5 crops at once.",
        BigDecimal("1e14"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        1200f, 500f
    ),
    AUTOMATION_BULK_2(
        "Bulk Buy II",
        "Each automation tick purchases up to 10 crops at once.",
        BigDecimal("1e17"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        1200f, 300f
    ),
    AUTOMATION_BULK_MAX(
        "Buy Max",
        "Each automation tick purchases the maximum number of crops you can afford.",
        BigDecimal("1e19"), BigDecimal.ONE, 1,
        BarnUpgradeCategory.AUTOMATION,
        1200f, 100f
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
            AUTOMATION_SOIL     to listOf(AUTOMATION_BASIC),
            AUTOMATION_SPEED_1  to listOf(AUTOMATION_BASIC),
            AUTOMATION_SPEED_2  to listOf(AUTOMATION_SPEED_1),
            AUTOMATION_SPEED_3  to listOf(AUTOMATION_SPEED_2),
            AUTOMATION_KITCHEN  to listOf(AUTOMATION_BASIC, KITCHEN),
            AUTOMATION_RECIPE   to listOf(AUTOMATION_KITCHEN),
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
