package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.PlanetaryIdle.Companion.MATH_CONTEXT
import com.github.jacks.planetaryIdle.components.BarnUpgrade
import com.github.jacks.planetaryIdle.components.Discovery
import com.github.jacks.planetaryIdle.components.DiscoveryCategory
import com.github.jacks.planetaryIdle.components.PlanetResources
import com.github.jacks.planetaryIdle.events.AchievementCompletedEvent
import com.github.jacks.planetaryIdle.events.AchievementNotificationEvent
import com.github.jacks.planetaryIdle.events.BarnEffectsChangedEvent
import com.github.jacks.planetaryIdle.events.CreditGoldEvent
import com.github.jacks.planetaryIdle.events.CropUnlockedEvent
import com.github.jacks.planetaryIdle.events.DiscoveryPurchasedEvent
import com.github.jacks.planetaryIdle.events.InsightTickEvent
import com.github.jacks.planetaryIdle.events.ObservatoryEffects
import com.github.jacks.planetaryIdle.events.ObservatoryEffectsChangedEvent
import com.github.jacks.planetaryIdle.events.ObservatoryUnlockedEvent
import com.github.jacks.planetaryIdle.events.ResetGameEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.quillraven.fleks.World
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/** Snapshot of one discovery's state for the view to render. */
data class DiscoveryViewState(
    val discovery: Discovery,
    val isPurchased: Boolean,
    val isCategoryUnlocked: Boolean,
    val canAfford: Boolean,
    val thresholdText: String,      // e.g. "Requires 3 Common Discoveries"
)

class ObservatoryViewModel(
    @Suppress("UNUSED_PARAMETER") world: World,
    private val stage: Stage,
    private val farmModel: FarmModel,
) : PropertyChangeSource(), EventListener {

    private val preferences: Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }

    // ── Observable properties (view binds to these) ───────────────────────────

    var observatoryUnlocked by propertyNotify(preferences["observatory_unlocked", false])
    var insight by propertyNotify(BigDecimal(preferences["observatory_insight", "0"]))
    var insightPerSecond by propertyNotify(BigDecimal.ZERO)
    var discoveryStates by propertyNotify(emptyList<DiscoveryViewState>())
    var totalDiscoveriesPurchased by propertyNotify(0)

    // ── Internal state ────────────────────────────────────────────────────────

    private val purchasedDiscoveries: MutableSet<Discovery> = mutableSetOf()
    private var totalGoldEarned: BigDecimal = BigDecimal(preferences["observatory_total_gold_earned", "0"])

    /** Cached achievement multiplier (updated on AchievementCompletedEvent). */
    private var cachedAchievementMultiplier: BigDecimal = BigDecimal(preferences["achievement_multiplier", "1"])

    /** Cached Kitchen tier counts per color (updated on CropUnlockedEvent). */
    private val kitchenTierCounts: MutableMap<String, Int> = PlanetResources.entries
        .associate { it.resourceName to 0 }.toMutableMap()

    /** Cached barn value levels per color (updated on BarnEffectsChangedEvent). */
    private val barnValueLevels: MutableMap<String, Int> = PlanetResources.entries
        .associate { it.resourceName to 0 }.toMutableMap()

    init {
        stage.addListener(this)

        // Load purchased discoveries from prefs
        Discovery.entries.forEach { discovery ->
            if (preferences[discovery.prefKey, false]) {
                purchasedDiscoveries.add(discovery)
            }
        }

        // Load kitchen tier counts from prefs
        PlanetResources.entries.forEach { resource ->
            val color = resource.resourceName
            val unlockedCount = preferences["kitchen_unlocked_crops_$color", ""]
                .split(",").count { it.isNotBlank() }
            kitchenTierCounts[color] = unlockedCount
        }

        // Load barn value levels from prefs
        BarnUpgrade.valueUpgradeFor.forEach { (color, upgrade) ->
            barnValueLevels[color] = preferences[upgrade.prefKey, 0]
        }

        totalDiscoveriesPurchased = purchasedDiscoveries.size
        discoveryStates = buildDiscoveryStates()

        // Broadcast initial effects
        if (observatoryUnlocked) {
            fireEffects()
        }
    }

    // ── EventListener ─────────────────────────────────────────────────────────

    override fun handle(event: Event): Boolean {
        when (event) {
            is ObservatoryUnlockedEvent -> {
                observatoryUnlocked = true
                discoveryStates = buildDiscoveryStates()
                stage.fire(AchievementNotificationEvent("observatory_unlock"))
                return false
            }
            is InsightTickEvent -> {
                if (!observatoryUnlocked) return false
                generateInsight()
                return false
            }
            is DiscoveryPurchasedEvent -> {
                handlePurchase(event.discovery)
                return true
            }
            is CreditGoldEvent -> {
                totalGoldEarned += event.amount
                preferences.flush { this["observatory_total_gold_earned"] = totalGoldEarned.toString() }
                // Singularity scales with total gold — re-fire effects if purchased
                if (Discovery.THE_SINGULARITY in purchasedDiscoveries) {
                    fireEffects()
                }
                return false
            }
            is AchievementCompletedEvent -> {
                cachedAchievementMultiplier = BigDecimal(preferences["achievement_multiplier", "1"])
                if (observatoryUnlocked) fireEffects()
                return false
            }
            is BarnEffectsChangedEvent -> {
                // Refresh barn value levels for Stellar Cartography
                BarnUpgrade.valueUpgradeFor.forEach { (color, upgrade) ->
                    barnValueLevels[color] = preferences[upgrade.prefKey, 0]
                }
                if (observatoryUnlocked && Discovery.STELLAR_CARTOGRAPHY in purchasedDiscoveries) {
                    fireEffects()
                }
                return false
            }
            is CropUnlockedEvent -> {
                // Refresh kitchen tier count for this color for Spectrum Analysis
                val color = event.color
                kitchenTierCounts[color] = preferences["kitchen_unlocked_crops_$color", ""]
                    .split(",").count { it.isNotBlank() }
                if (observatoryUnlocked && Discovery.SPECTRUM_ANALYSIS in purchasedDiscoveries) {
                    fireEffects()
                }
                return false
            }
            is ResetGameEvent -> {
                // Observatory state persists through resets (like Kitchen)
                return false
            }
            else -> return false
        }
    }

    // ── Purchase logic ────────────────────────────────────────────────────────

    private fun handlePurchase(discovery: Discovery) {
        if (discovery in purchasedDiscoveries) return
        if (insight < discovery.insightCost) return
        if (!Discovery.isCategoryUnlocked(discovery, purchasedDiscoveries)) return

        insight -= discovery.insightCost
        purchasedDiscoveries.add(discovery)
        totalDiscoveriesPurchased = purchasedDiscoveries.size

        preferences.flush {
            this["observatory_insight"] = insight.toString()
            this[discovery.prefKey] = true
        }

        discoveryStates = buildDiscoveryStates()
        fireEffects()

        // Fire achievement notifications
        val count = purchasedDiscoveries.size
        if (count >= 1)  stage.fire(AchievementNotificationEvent("discovery_1"))
        if (count >= 5)  stage.fire(AchievementNotificationEvent("discovery_5"))
        if (count >= 10) stage.fire(AchievementNotificationEvent("discovery_10"))
        if (count >= 15) stage.fire(AchievementNotificationEvent("discovery_15"))
        if (discovery.category == DiscoveryCategory.MYTHICAL) {
            stage.fire(AchievementNotificationEvent("mythical_first"))
        }

        log.debug { "Purchased Discovery: ${discovery.displayName}" }
    }

    // ── Insight generation ────────────────────────────────────────────────────

    private fun generateInsight() {
        val productionRate = farmModel.productionRate
        if (productionRate <= BigDecimal.ZERO) return

        val baseRate = productionRate.pow(2, INSIGHT_MATH_CONTEXT)
            .let { BigDecimal(Math.pow(productionRate.toDouble(), 0.4)) }
        val rate = baseRate.multiply(computeInsightMultiplier())
        insightPerSecond = rate
        insight += rate

        preferences.flush { this["observatory_insight"] = insight.toString() }

        // Insight achievement notifications
        if (insight >= INSIGHT_1B) stage.fire(AchievementNotificationEvent("insight_1b"))
        if (insight >= INSIGHT_1T) stage.fire(AchievementNotificationEvent("insight_1t"))
    }

    // ── Effects computation ───────────────────────────────────────────────────

    private fun computeInsightMultiplier(): BigDecimal {
        var mult = BigDecimal.ONE
        if (Discovery.SOLAR_CURRENT in purchasedDiscoveries)   mult = mult.multiply(BigDecimal("1.5"))
        if (Discovery.ABYSSAL_LENS  in purchasedDiscoveries)   mult = mult.multiply(BigDecimal("1000"))
        if (Discovery.GRAND_UNIFICATION in purchasedDiscoveries) mult = mult.multiply(BigDecimal("10000"))
        if (Discovery.LUMINOUS_VEIL in purchasedDiscoveries) {
            mult = mult.multiply(cachedAchievementMultiplier.max(BigDecimal.ONE))
        }
        if (Discovery.COSMIC_CASCADE in purchasedDiscoveries) {
            val n = purchasedDiscoveries.size.toBigDecimal()
            mult = mult.multiply(n)
        }
        return mult
    }

    private fun fireEffects() {
        val effects = computeEffects()
        stage.fire(ObservatoryEffectsChangedEvent(effects))
        // Refresh view state (affordability may have changed)
        discoveryStates = buildDiscoveryStates()
    }

    private fun computeEffects(): ObservatoryEffects {
        val n = purchasedDiscoveries.size

        // ── Base multipliers from individual discoveries ───────────────────
        var productionMult = BigDecimal.ONE
        var cycleSpeedMult = BigDecimal.ONE
        var recipePayoutMult = BigDecimal.ONE
        var soilEffectivenessBonus = BigDecimal.ZERO
        val perColorMults: MutableMap<String, BigDecimal> = PlanetResources.entries
            .associate { it.resourceName to BigDecimal.ONE }.toMutableMap()
        var achievementSquared = false

        if (Discovery.FIRST_LIGHT in purchasedDiscoveries) {
            productionMult = productionMult.multiply(BigDecimal("2"))
        }
        if (Discovery.ORBITAL_SURVEY in purchasedDiscoveries) {
            cycleSpeedMult = cycleSpeedMult.multiply(BigDecimal("1.15"))
        }
        if (Discovery.GRAVITY_WELL in purchasedDiscoveries) {
            soilEffectivenessBonus = soilEffectivenessBonus.add(BigDecimal("0.25"))
        }
        if (Discovery.BINARY_INFLUENCE in purchasedDiscoveries) {
            recipePayoutMult = recipePayoutMult.multiply(BigDecimal("3"))
        }
        if (Discovery.PLANETARY_MASS in purchasedDiscoveries) {
            val soilMult = farmModel.soilUpgrades.add(BigDecimal.ONE)
            productionMult = productionMult.multiply(soilMult)
        }
        if (Discovery.RESONANCE_FIELD in purchasedDiscoveries) {
            achievementSquared = true
        }
        if (Discovery.COSMIC_EXPANSION in purchasedDiscoveries) {
            productionMult = productionMult.multiply(BigDecimal("10000"))
        }
        if (Discovery.VOID_RESONANCE in purchasedDiscoveries) {
            productionMult = productionMult.multiply(BigDecimal("1000000"))
        }
        if (Discovery.STELLAR_COLLAPSE in purchasedDiscoveries) {
            productionMult = productionMult.multiply(BigDecimal("1000000000"))
        }
        if (Discovery.COSMIC_CASCADE in purchasedDiscoveries) {
            val nSquared = BigDecimal(n).multiply(BigDecimal(n))
            productionMult = productionMult.multiply(nSquared)
        }
        if (Discovery.THE_SINGULARITY in purchasedDiscoveries && totalGoldEarned > BigDecimal.ONE) {
            val exponent = totalGoldEarned.toDouble()
            if (exponent.isFinite() && exponent > 0.0) {
                val singularityMult = BigDecimal(Math.pow(exponent, 0.1))
                productionMult = productionMult.multiply(singularityMult)
            }
        }
        if (Discovery.GRAND_UNIFICATION in purchasedDiscoveries) {
            productionMult = productionMult.multiply(BigDecimal("1e15"))
        }
        if (Discovery.PLANETARY_ASCENSION in purchasedDiscoveries) {
            val soil = farmModel.soilUpgrades
            val ascensionMult = soil.multiply(soil).add(BigDecimal.ONE)
            productionMult = productionMult.multiply(ascensionMult)
        }

        // ── Per-color multipliers ─────────────────────────────────────────
        if (Discovery.SPECTRUM_ANALYSIS in purchasedDiscoveries) {
            PlanetResources.entries.forEach { resource ->
                val color = resource.resourceName
                val tiers = kitchenTierCounts[color] ?: 0
                val spectrumMult = BigDecimal.ONE.add(BigDecimal("0.25").multiply(BigDecimal(tiers)))
                perColorMults[color] = (perColorMults[color] ?: BigDecimal.ONE).multiply(spectrumMult)
            }
        }
        if (Discovery.STELLAR_CARTOGRAPHY in purchasedDiscoveries) {
            PlanetResources.entries.forEach { resource ->
                val color = resource.resourceName
                val levels = barnValueLevels[color] ?: 0
                val cartographyMult = BigDecimal.ONE.add(
                    BigDecimal(levels).divide(BigDecimal("10"), 10, RoundingMode.HALF_UP)
                )
                perColorMults[color] = (perColorMults[color] ?: BigDecimal.ONE).multiply(cartographyMult)
            }
        }

        // ── Unified Principle: multiply all discovery bonuses by n ────────
        if (Discovery.UNIFIED_PRINCIPLE in purchasedDiscoveries && n > 0) {
            val unifiedMult = BigDecimal(n)
            productionMult   = productionMult.multiply(unifiedMult)
            recipePayoutMult = recipePayoutMult.multiply(unifiedMult)
            perColorMults.keys.forEach { color ->
                perColorMults[color] = (perColorMults[color] ?: BigDecimal.ONE).multiply(unifiedMult)
            }
            soilEffectivenessBonus = soilEffectivenessBonus.multiply(unifiedMult)
        }

        // ── The Observable Universe: square all discovery bonuses ─────────
        if (Discovery.THE_OBSERVABLE_UNIVERSE in purchasedDiscoveries) {
            productionMult   = productionMult.multiply(productionMult)
            recipePayoutMult = recipePayoutMult.multiply(recipePayoutMult)
            perColorMults.keys.forEach { color ->
                val m = perColorMults[color] ?: BigDecimal.ONE
                perColorMults[color] = m.multiply(m)
            }
            soilEffectivenessBonus = soilEffectivenessBonus.multiply(BigDecimal("2"))
        }

        return ObservatoryEffects(
            productionMultiplier     = productionMult,
            cycleSpeedMultiplier     = cycleSpeedMult,
            recipePayoutMultiplier   = recipePayoutMult,
            soilEffectivenessBonus   = soilEffectivenessBonus,
            insightMultiplier        = computeInsightMultiplier(),
            perColorMultipliers      = perColorMults.toMap(),
            achievementMultiplierSquared = achievementSquared,
        )
    }

    // ── View state ────────────────────────────────────────────────────────────

    private fun buildDiscoveryStates(): List<DiscoveryViewState> {
        return Discovery.entries.map { discovery ->
            val isPurchased = discovery in purchasedDiscoveries
            val isCategoryUnlocked = Discovery.isCategoryUnlocked(discovery, purchasedDiscoveries)
            val canAfford = insight >= discovery.insightCost
            val thresholdText = when (discovery.category) {
                DiscoveryCategory.COMMON    -> ""
                DiscoveryCategory.TREASURED -> "Requires ${Discovery.COMMON_REQUIRED_FOR_TREASURED} Common Discoveries"
                DiscoveryCategory.LEGENDARY -> "Requires ${Discovery.TREASURED_REQUIRED_FOR_LEGENDARY} Treasured Discoveries"
                DiscoveryCategory.FABLED    -> "Requires ${Discovery.LEGENDARY_REQUIRED_FOR_FABLED} Legendary Discoveries"
                DiscoveryCategory.MYTHICAL  -> "Requires ${Discovery.FABLED_REQUIRED_FOR_MYTHICAL} Fabled Discoveries"
            }
            DiscoveryViewState(
                discovery = discovery,
                isPurchased = isPurchased,
                isCategoryUnlocked = isCategoryUnlocked,
                canAfford = canAfford,
                thresholdText = thresholdText,
            )
        }
    }

    // ── Public helpers ────────────────────────────────────────────────────────

    /** Called by GameScreen after wiring, to broadcast initial state. */
    fun fireInitialEffects() {
        if (observatoryUnlocked) fireEffects()
    }

    /** Called by ObservatoryView when the player taps a Research button. */
    fun purchase(discovery: Discovery) {
        stage.fire(DiscoveryPurchasedEvent(discovery))
    }

    companion object {
        private val log = logger<ObservatoryViewModel>()

        private val INSIGHT_MATH_CONTEXT = MathContext(10, RoundingMode.HALF_UP)
        private val INSIGHT_1B = BigDecimal("1000000000")
        private val INSIGHT_1T = BigDecimal("1000000000000")
    }
}
