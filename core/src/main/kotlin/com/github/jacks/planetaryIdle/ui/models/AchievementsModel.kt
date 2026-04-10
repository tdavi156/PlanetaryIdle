package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.AchievementComponent
import com.github.jacks.planetaryIdle.components.Achievements
import com.github.jacks.planetaryIdle.components.BarnUpgrade
import com.github.jacks.planetaryIdle.components.BarnUpgradeCategory
import com.github.jacks.planetaryIdle.components.CropRegistry
import com.github.jacks.planetaryIdle.events.AchievementCompletedEvent
import com.github.jacks.planetaryIdle.events.BuyBarnUpgradeEvent
import com.github.jacks.planetaryIdle.events.DiscoveryPurchasedEvent
import com.github.jacks.planetaryIdle.events.ObservatoryUnlockedEvent
import com.github.jacks.planetaryIdle.ui.views.HeaderView.Companion.formatShort
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import java.math.BigDecimal
import java.math.RoundingMode

// ── Progress data ─────────────────────────────────────────────────────────────

/** Snapshot of progress toward a single achievement. */
data class AchievementProgress(
    /** 0f = no progress, 1f = complete. Used to drive the card progress bar. */
    val fraction: Float,
    /** Human-readable text, e.g. "12 / 50" or "1.23 M / 1.00 T". */
    val text: String,
)

// ── Model ─────────────────────────────────────────────────────────────────────

class AchievementsModel(
    world: World,
    stage: Stage,
    private val farmModel: FarmModel,
    private val kitchenViewModel: KitchenViewModel,
) : PropertyChangeSource(), EventListener {

    /** Set by GameScreen after construction to avoid circular dependency. */
    var observatoryViewModel: ObservatoryViewModel? = null

    private val preferences: Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }
    private val achievementComponents: ComponentMapper<AchievementComponent> = world.mapper()
    private val achievementEntities = world.family(allOf = arrayOf(AchievementComponent::class))

    var completedAchievements by propertyNotify(
        Achievements.entries
            .filter { preferences["ach_${it.achId}", false] }
            .map { it.achId }
            .toSet()
    )

    var achievementMultiplier by propertyNotify(
        BigDecimal(preferences["achievement_multiplier", "1"])
    )

    /** Current progress snapshot for all 57 achievements. Updated on each relevant model change. */
    var progressMap: Map<String, AchievementProgress> by propertyNotify(emptyMap())

    /** Running total of non-SOIL barn upgrade levels purchased (mirrors BarnViewModel logic). */
    private var barnNonSoilUpgradeCount: Int = 0

    /** Whether the Observatory has been unlocked. */
    private var observatoryIsUnlocked: Boolean = preferences["observatory_unlocked", false]

    /** Running count of purchased discoveries (mirrors ObservatoryViewModel). */
    private var discoveryCount: Int = 0

    init {
        stage.addListener(this)

        // Reconstruct barn count from persisted levels at startup
        barnNonSoilUpgradeCount = BarnUpgrade.entries
            .filter { it.category != BarnUpgradeCategory.SOIL }
            .sumOf { preferences.getInteger("barn_upgrade_${it.name}_level", 0) }

        // Bind to FarmModel resource state changes
        farmModel.onPropertyChange(FarmModel::redState)    { updateAllProgress() }
        farmModel.onPropertyChange(FarmModel::orangeState) { updateAllProgress() }
        farmModel.onPropertyChange(FarmModel::yellowState) { updateAllProgress() }
        farmModel.onPropertyChange(FarmModel::greenState)  { updateAllProgress() }
        farmModel.onPropertyChange(FarmModel::blueState)   { updateAllProgress() }
        farmModel.onPropertyChange(FarmModel::purpleState) { updateAllProgress() }
        farmModel.onPropertyChange(FarmModel::pinkState)   { updateAllProgress() }
        farmModel.onPropertyChange(FarmModel::brownState)  { updateAllProgress() }
        farmModel.onPropertyChange(FarmModel::whiteState)  { updateAllProgress() }
        farmModel.onPropertyChange(FarmModel::blackState)  { updateAllProgress() }
        farmModel.onPropertyChange(FarmModel::goldCoins)   { updateAllProgress() }
        farmModel.onPropertyChange(FarmModel::soilUpgrades) { updateAllProgress() }

        // Bind to KitchenViewModel changes
        kitchenViewModel.onPropertyChange(KitchenViewModel::kitchenUnlocked)   { updateAllProgress() }
        kitchenViewModel.onPropertyChange(KitchenViewModel::unlockedCrops)     { updateAllProgress() }
        kitchenViewModel.onPropertyChange(KitchenViewModel::discoveredRecipes) { updateAllProgress() }

        // Load initial discovery count from prefs
        discoveryCount = com.github.jacks.planetaryIdle.components.Discovery.entries
            .count { preferences[it.prefKey, false] }

        // Seed initial progress map
        updateAllProgress()
    }

    // ── EventListener ─────────────────────────────────────────────────────────

    override fun handle(event: Event): Boolean {
        when (event) {
            is AchievementCompletedEvent -> {
                val achId = event.achId
                if (achId.isEmpty()) return false
                achievementEntities.forEach { achievement ->
                    if (!achievementComponents[achievement].completedAchievements.contains(achId)) {
                        achievementComponents[achievement].completedAchievements.add(achId)
                        completedAchievements = completedAchievements + achId
                        preferences.flush { this["ach_$achId"] = true }
                        achievementMultiplier = BigDecimal(preferences["achievement_multiplier", "1"])
                        updateAllProgress()
                    }
                }
            }
            is BuyBarnUpgradeEvent -> {
                // Mirror the non-SOIL count that BarnViewModel uses for barn achievements
                if (event.upgrade.category != BarnUpgradeCategory.SOIL) {
                    barnNonSoilUpgradeCount++
                    updateAllProgress()
                }
                return false
            }
            is ObservatoryUnlockedEvent -> {
                observatoryIsUnlocked = true
                updateAllProgress()
                return false
            }
            is DiscoveryPurchasedEvent -> {
                discoveryCount++
                updateAllProgress()
                return false
            }
            else -> return false
        }
        return true
    }

    // ── Progress computation ───────────────────────────────────────────────────

    private fun updateAllProgress() {
        progressMap = Achievements.entries.associate { ach -> ach.achId to computeProgress(ach) }
    }

    private fun computeProgress(ach: Achievements): AchievementProgress {
        if (ach.achId in completedAchievements) return AchievementProgress(1f, "Complete!")
        val t = parseThreshold(ach.achId)
        return when {
            ach.achId == "combined_full_spectrum"   -> fullSpectrumProgress()
            ach.achId.startsWith("red_")            -> cropProgress(farmModel.redState.owned.toLong(), t)
            ach.achId.startsWith("orange_")         -> cropProgress(farmModel.orangeState.owned.toLong(), t)
            ach.achId.startsWith("yellow_")         -> cropProgress(farmModel.yellowState.owned.toLong(), t)
            ach.achId.startsWith("green_")          -> cropProgress(farmModel.greenState.owned.toLong(), t)
            ach.achId.startsWith("blue_")           -> cropProgress(farmModel.blueState.owned.toLong(), t)
            ach.achId.startsWith("purple_")         -> cropProgress(farmModel.purpleState.owned.toLong(), t)
            ach.achId.startsWith("pink_")           -> cropProgress(farmModel.pinkState.owned.toLong(), t)
            ach.achId.startsWith("brown_")          -> cropProgress(farmModel.brownState.owned.toLong(), t)
            ach.achId.startsWith("white_")          -> cropProgress(farmModel.whiteState.owned.toLong(), t)
            ach.achId.startsWith("black_")          -> cropProgress(farmModel.blackState.owned.toLong(), t)
            ach.achId.startsWith("gold_")           -> goldProgress(ach)
            ach.achId.startsWith("soil_")           -> cropProgress(farmModel.soilUpgrades.toLong(), t)
            ach.achId.startsWith("barn_")           -> cropProgress(barnNonSoilUpgradeCount.toLong(), t)
            ach.achId == "kitchen_unlock"           -> AchievementProgress(0f, "0 / 1")
            ach.achId.startsWith("kitchen_crop_")   -> kitchenCropProgress(t)
            ach.achId.startsWith("kitchen_recipe_") -> kitchenRecipeProgress(t)
            ach.achId == "observatory_unlock"       -> AchievementProgress(if (observatoryIsUnlocked) 1f else 0f, if (observatoryIsUnlocked) "1 / 1" else "0 / 1")
            ach.achId.startsWith("discovery_")      -> cropProgress(discoveryCount.toLong(), t)
            ach.achId == "mythical_first"           -> cropProgress(discoveryCount.toLong().coerceAtMost(1), 1)
            ach.achId.startsWith("insight_")        -> insightProgress(ach)
            else                                    -> AchievementProgress(0f, "")
        }
    }

    /** Parses the numeric threshold from an achievement ID, e.g. "red_250" → 250, "barn_15" → 15. */
    private fun parseThreshold(achId: String): Long =
        achId.substringAfterLast("_").toLongOrNull() ?: 0L

    private fun cropProgress(current: Long, threshold: Long): AchievementProgress {
        if (threshold == 0L) return AchievementProgress(0f, "")
        val capped = current.coerceAtMost(threshold)
        val fraction = (capped.toFloat() / threshold).coerceIn(0f, 1f)
        return AchievementProgress(fraction, "$capped / $threshold")
    }

    private fun goldProgress(ach: Achievements): AchievementProgress {
        val target = when (ach.achId) {
            "gold_1m"    -> GOLD_1M
            "gold_1b"    -> GOLD_1B
            "gold_1t"    -> GOLD_1T
            "gold_1q"    -> GOLD_1Q
            "gold_1e33"  -> GOLD_1E33
            "gold_1e50"  -> GOLD_1E50
            "gold_1e75"  -> GOLD_1E75
            "gold_1e100" -> GOLD_1E100
            "gold_1e150" -> GOLD_1E150
            "gold_1e200" -> GOLD_1E200
            "gold_1e308" -> GOLD_1E308
            else         -> return AchievementProgress(0f, "")
        }
        val current = farmModel.goldCoins.min(target)
        val fraction = current.divide(target, 6, RoundingMode.HALF_UP).toFloat().coerceIn(0f, 1f)
        return AchievementProgress(fraction, "${formatShort(current)} / ${formatShort(target)}")
    }

    private fun insightProgress(ach: Achievements): AchievementProgress {
        val ovm = observatoryViewModel ?: return AchievementProgress(0f, "0 / ?")
        val target = when (ach.achId) {
            "insight_1b" -> INSIGHT_1B
            "insight_1t" -> INSIGHT_1T
            else         -> return AchievementProgress(0f, "")
        }
        val current = ovm.insight.min(target)
        val fraction = current.divide(target, 6, RoundingMode.HALF_UP).toFloat().coerceIn(0f, 1f)
        return AchievementProgress(fraction, "${formatShort(current)} / ${formatShort(target)}")
    }

    private fun fullSpectrumProgress(): AchievementProgress {
        val states = listOf(
            farmModel.redState, farmModel.orangeState, farmModel.yellowState,
            farmModel.greenState, farmModel.blueState, farmModel.purpleState,
            farmModel.pinkState, farmModel.brownState, farmModel.whiteState, farmModel.blackState,
        )
        val colorsAt10 = states.count { it.owned >= TEN }
        return AchievementProgress(colorsAt10 / 10f, "$colorsAt10 / 10 colors")
    }

    private fun kitchenCropProgress(threshold: Long): AchievementProgress {
        val discovered = kitchenViewModel.unlockedCrops.entries.sumOf { (color, names) ->
            names.count { name ->
                CropRegistry.forColor(color).find { it.cropName == name }?.tier?.let { it > 1 } ?: false
            }
        }.toLong()
        return cropProgress(discovered, threshold)
    }

    private fun kitchenRecipeProgress(threshold: Long): AchievementProgress {
        val count = kitchenViewModel.discoveredRecipes.size.toLong()
        return cropProgress(count, threshold)
    }

    // ── Companion ─────────────────────────────────────────────────────────────

    companion object {
        private val TEN       = BigDecimal(10)
        private val GOLD_1M   = BigDecimal(1_000_000L)
        private val GOLD_1B   = BigDecimal(1_000_000_000L)
        private val GOLD_1T   = BigDecimal(1_000_000_000_000L)
        private val GOLD_1Q   = BigDecimal(1_000_000_000_000_000L)
        private val GOLD_1E33  = BigDecimal("1e33")
        private val GOLD_1E50  = BigDecimal("1e50")
        private val GOLD_1E75  = BigDecimal("1e75")
        private val GOLD_1E100 = BigDecimal("1e100")
        private val GOLD_1E150 = BigDecimal("1e150")
        private val GOLD_1E200 = BigDecimal("1e200")
        private val GOLD_1E308 = BigDecimal("1e308")
        private val INSIGHT_1B = BigDecimal("1000000000")
        private val INSIGHT_1T = BigDecimal("1000000000000")
    }
}
