package com.github.jacks.planetaryIdle.components

import java.math.BigDecimal

enum class DiscoveryCategory(val displayName: String) {
    COMMON("Common"),
    TREASURED("Treasured"),
    LEGENDARY("Legendary"),
    FABLED("Fabled"),
    MYTHICAL("Mythical"),
}

/**
 * All Observatory Discoveries, organized by category.
 *
 * Effects are intentionally NOT encoded here — they are computed dynamically
 * in ObservatoryViewModel based on purchased discoveries and current game state.
 * This mirrors how BarnUpgrade effects are computed in BarnViewModel.
 */
enum class Discovery(
    val discoveryId: String,
    val displayName: String,
    val description: String,
    val category: DiscoveryCategory,
    val insightCost: BigDecimal,
) {
    // ── Common ────────────────────────────────────────────────────────────────
    FIRST_LIGHT(
        "first_light",
        "First Light",
        "All crop production ×2.",
        DiscoveryCategory.COMMON,
        BigDecimal("100"),
    ),
    ORBITAL_SURVEY(
        "orbital_survey",
        "Orbital Survey",
        "Crop cycle speed ×1.15.",
        DiscoveryCategory.COMMON,
        BigDecimal("500"),
    ),
    SPECTRUM_ANALYSIS(
        "spectrum_analysis",
        "Spectrum Analysis",
        "Each crop color's production multiplied by (1 + 0.25 × Kitchen tiers discovered for that color).",
        DiscoveryCategory.COMMON,
        BigDecimal("2000"),
    ),
    GRAVITY_WELL(
        "gravity_well",
        "Gravity Well",
        "Soil effectiveness ×1.25.",
        DiscoveryCategory.COMMON,
        BigDecimal("5000"),
    ),
    SOLAR_CURRENT(
        "solar_current",
        "Solar Current",
        "Insight generation ×1.5.",
        DiscoveryCategory.COMMON,
        BigDecimal("10000"),
    ),

    // ── Treasured ─────────────────────────────────────────────────────────────
    BINARY_INFLUENCE(
        "binary_influence",
        "Binary Influence",
        "Recipe payout ×3.",
        DiscoveryCategory.TREASURED,
        BigDecimal("100000"),
    ),
    PLANETARY_MASS(
        "planetary_mass",
        "Planetary Mass",
        "All production ×(Soil upgrades + 1).",
        DiscoveryCategory.TREASURED,
        BigDecimal("500000"),
    ),
    RESONANCE_FIELD(
        "resonance_field",
        "Resonance Field",
        "Achievement multiplier is applied twice (squared).",
        DiscoveryCategory.TREASURED,
        BigDecimal("2000000"),
    ),
    STELLAR_CARTOGRAPHY(
        "stellar_cartography",
        "Stellar Cartography",
        "Each crop color multiplied by (Barn value levels for that color ÷ 10 + 1).",
        DiscoveryCategory.TREASURED,
        BigDecimal("5000000"),
    ),
    LUMINOUS_VEIL(
        "luminous_veil",
        "Luminous Veil",
        "Insight generation multiplied by the current achievement multiplier.",
        DiscoveryCategory.TREASURED,
        BigDecimal("20000000"),
    ),

    // ── Legendary ─────────────────────────────────────────────────────────────
    COSMIC_EXPANSION(
        "cosmic_expansion",
        "Cosmic Expansion",
        "All production ×10,000.",
        DiscoveryCategory.LEGENDARY,
        BigDecimal("1000000000000"),
    ),
    VOID_RESONANCE(
        "void_resonance",
        "Void Resonance",
        "All production ×1,000,000.",
        DiscoveryCategory.LEGENDARY,
        BigDecimal("1e16"),
    ),
    STELLAR_COLLAPSE(
        "stellar_collapse",
        "Stellar Collapse",
        "All production ×1,000,000,000.",
        DiscoveryCategory.LEGENDARY,
        BigDecimal("1e20"),
    ),
    ABYSSAL_LENS(
        "abyssal_lens",
        "Abyssal Lens",
        "Insight generation ×1,000.",
        DiscoveryCategory.LEGENDARY,
        BigDecimal("1e18"),
    ),

    // ── Fabled ────────────────────────────────────────────────────────────────
    UNIFIED_PRINCIPLE(
        "unified_principle",
        "Unified Principle",
        "All other discovery bonuses multiplied by the total number of Discoveries owned.",
        DiscoveryCategory.FABLED,
        BigDecimal("1e28"),
    ),
    COSMIC_CASCADE(
        "cosmic_cascade",
        "Cosmic Cascade",
        "All production ×(Discoveries owned²). Insight generation ×(Discoveries owned).",
        DiscoveryCategory.FABLED,
        BigDecimal("1e38"),
    ),
    THE_SINGULARITY(
        "the_singularity",
        "The Singularity",
        "All production ×(total gold ever earned)^0.1.",
        DiscoveryCategory.FABLED,
        BigDecimal("1e48"),
    ),

    // ── Mythical ──────────────────────────────────────────────────────────────
    GRAND_UNIFICATION(
        "grand_unification",
        "Grand Unification",
        "All production ×10^15. Insight generation ×10,000.",
        DiscoveryCategory.MYTHICAL,
        BigDecimal("1e62"),
    ),
    PLANETARY_ASCENSION(
        "planetary_ascension",
        "Planetary Ascension",
        "All production ×(Soil upgrades²).",
        DiscoveryCategory.MYTHICAL,
        BigDecimal("1e78"),
    ),
    THE_OBSERVABLE_UNIVERSE(
        "the_observable_universe",
        "The Observable Universe",
        "All discovery bonuses are squared (applied a second time).",
        DiscoveryCategory.MYTHICAL,
        BigDecimal("1e95"),
    );

    val prefKey: String get() = "observatory_discovery_${discoveryId}_purchased"

    companion object {
        // Minimum number of discoveries of the preceding tier needed to unlock each tier
        const val COMMON_REQUIRED_FOR_TREASURED   = 3
        const val TREASURED_REQUIRED_FOR_LEGENDARY = 3
        const val LEGENDARY_REQUIRED_FOR_FABLED    = 2
        const val FABLED_REQUIRED_FOR_MYTHICAL     = 2

        fun byCategory(category: DiscoveryCategory): List<Discovery> =
            entries.filter { it.category == category }

        /**
         * Returns true if [discovery] is unlocked (its category threshold is met)
         * given the set of already-purchased discoveries.
         */
        fun isCategoryUnlocked(discovery: Discovery, purchased: Set<Discovery>): Boolean {
            return when (discovery.category) {
                DiscoveryCategory.COMMON    -> true
                DiscoveryCategory.TREASURED -> purchased.count { it.category == DiscoveryCategory.COMMON }    >= COMMON_REQUIRED_FOR_TREASURED
                DiscoveryCategory.LEGENDARY -> purchased.count { it.category == DiscoveryCategory.TREASURED } >= TREASURED_REQUIRED_FOR_LEGENDARY
                DiscoveryCategory.FABLED    -> purchased.count { it.category == DiscoveryCategory.LEGENDARY } >= LEGENDARY_REQUIRED_FOR_FABLED
                DiscoveryCategory.MYTHICAL  -> purchased.count { it.category == DiscoveryCategory.FABLED }    >= FABLED_REQUIRED_FOR_MYTHICAL
            }
        }
    }
}
