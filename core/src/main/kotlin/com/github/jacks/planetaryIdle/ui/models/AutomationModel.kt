package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.data.BarnUpgrade
import com.github.jacks.planetaryIdle.data.Discovery
import com.github.jacks.planetaryIdle.data.Recipe
import com.github.jacks.planetaryIdle.components.PlanetResources
import com.github.jacks.planetaryIdle.events.AutomationUnlockedEvent
import com.github.jacks.planetaryIdle.events.BuyBarnUpgradeEvent
import com.github.jacks.planetaryIdle.events.BuyResourceEvent
import com.github.jacks.planetaryIdle.events.ResetGameEvent
import com.github.jacks.planetaryIdle.events.SmartBuyUnlockedEvent
import com.github.jacks.planetaryIdle.events.fire
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Holds all automation settings and executes the per-tick buy logic.
 *
 * Created before the Fleks world so it can be injected into [AutomationSystem].
 * References to [farmModel] and [kitchenViewModel] are wired post-construction
 * by GameScreen (same pattern as [BarnViewModel.kitchenViewModel]).
 */
class AutomationModel(
    private val stage: Stage,
) : PropertyChangeSource(), EventListener {

    private val preferences: Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }

    // ── Cross-references wired by GameScreen ──────────────────────────────────
    lateinit var farmModel: FarmModel
    lateinit var kitchenViewModel: KitchenViewModel

    // ── Observable properties (AutomationView binds to these) ────────────────
    var automationUnlocked  by propertyNotify(false)
    var soilAutoUnlocked    by propertyNotify(false)
    var soilAutoEnabled     by propertyNotify(false)
    var kitchenAutoUnlocked by propertyNotify(false)
    var autoRecipeUnlocked  by propertyNotify(false)
    var autoRecipeEnabled   by propertyNotify(false)
    var smartBuyUnlocked    by propertyNotify(false)

    var cropAutoEnabled: Map<String, Boolean> by propertyNotify(
        PlanetResources.entries.associate { it.resourceName to false }
    )
    var cropGoldThreshold: Map<String, Float> by propertyNotify(
        PlanetResources.entries.associate { it.resourceName to 1.0f }
    )

    // ── Internal state (read by AutomationSystem) ─────────────────────────────
    /** Number of frames between automation ticks. Updated on upgrade purchase. */
    var tickIntervalFrames: Int = TICK_1_PER_SEC

    /** Max crops to buy per tick per color. -1 = buy max. */
    private var bulkQuantity: Int = 1

    /** Counter passed in from AutomationSystem used to throttle recipe checks. */
    private var recipeTickCounter: Int = 0

    init {
        stage.addListener(this)
        load()
    }

    // ── EventListener ─────────────────────────────────────────────────────────

    override fun handle(event: Event): Boolean {
        when (event) {
            is AutomationUnlockedEvent -> {
                if (!automationUnlocked) {
                    automationUnlocked = true
                    preferences.flush { this[PREF_UNLOCKED] = true }
                }
                return false
            }
            is BuyBarnUpgradeEvent -> {
                when (event.upgrade) {
                    BarnUpgrade.AUTOMATION_SOIL -> {
                        soilAutoUnlocked = true
                        preferences.flush { this[PREF_SOIL_UNLOCKED] = true }
                    }
                    BarnUpgrade.AUTOMATION_KITCHEN -> {
                        kitchenAutoUnlocked = true
                        preferences.flush { this[PREF_KITCHEN_UNLOCKED] = true }
                    }
                    BarnUpgrade.AUTOMATION_RECIPE -> {
                        autoRecipeUnlocked = true
                        preferences.flush { this[PREF_RECIPE_UNLOCKED] = true }
                    }
                    BarnUpgrade.AUTOMATION_SPEED_1 -> tickIntervalFrames = TICK_4_PER_SEC
                    BarnUpgrade.AUTOMATION_SPEED_2 -> tickIntervalFrames = TICK_10_PER_SEC
                    BarnUpgrade.AUTOMATION_SPEED_3 -> tickIntervalFrames = TICK_60_PER_SEC
                    BarnUpgrade.AUTOMATION_BULK_1  -> bulkQuantity = 5
                    BarnUpgrade.AUTOMATION_BULK_2  -> bulkQuantity = 10
                    BarnUpgrade.AUTOMATION_BULK_MAX -> bulkQuantity = BUY_MAX
                    else -> { /* other upgrades — no automation effect */ }
                }
                return false
            }
            is SmartBuyUnlockedEvent -> {
                if (!smartBuyUnlocked) {
                    smartBuyUnlocked = true
                    preferences.flush { this[PREF_SMART_BUY_UNLOCKED] = true }
                }
                return false
            }
            is ResetGameEvent -> {
                resetState()
                return false
            }
            else -> return false
        }
    }

    // ── Public toggle methods (called by AutomationView) ──────────────────────

    fun toggleCropAuto(color: String) {
        val current = cropAutoEnabled[color] ?: false
        cropAutoEnabled = cropAutoEnabled.toMutableMap().also { it[color] = !current }
        preferences.flush { this["${PREF_CROP_ENABLED_PREFIX}$color"] = !current }
    }

    fun setCropAutoEnabled(color: String, enabled: Boolean) {
        cropAutoEnabled = cropAutoEnabled.toMutableMap().also { it[color] = enabled }
        preferences.flush { this["${PREF_CROP_ENABLED_PREFIX}$color"] = enabled }
    }

    fun toggleSoilAuto() {
        soilAutoEnabled = !soilAutoEnabled
        preferences.flush { this[PREF_SOIL_ENABLED] = soilAutoEnabled }
    }

    fun toggleRecipeAuto() {
        autoRecipeEnabled = !autoRecipeEnabled
        preferences.flush { this[PREF_RECIPE_ENABLED] = autoRecipeEnabled }
    }

    fun setGoldThreshold(color: String, threshold: Float) {
        val clamped = threshold.coerceIn(0.01f, 1.0f)
        cropGoldThreshold = cropGoldThreshold.toMutableMap().also { it[color] = clamped }
        preferences.flush { this["${PREF_CROP_THRESHOLD_PREFIX}$color"] = clamped }
    }

    // ── Main tick logic (called by AutomationSystem) ──────────────────────────

    /**
     * Executes one automation tick.
     * @param totalTicks running counter from AutomationSystem, used to throttle recipe checks.
     */
    fun performTick(totalTicks: Int) {
        performCropBuyTick()

        if (soilAutoUnlocked && soilAutoEnabled) {
            // Delegate to BarnViewModel via event — it handles affordability + cost discount
            stage.fire(BuyBarnUpgradeEvent(BarnUpgrade.SOIL))
        }

        if (autoRecipeUnlocked && autoRecipeEnabled && (totalTicks % AUTO_RECIPE_INTERVAL == 0)) {
            performAutoRecipe()
        }
    }

    // ── Crop auto-buy ─────────────────────────────────────────────────────────

    private fun performCropBuyTick() {
        val fm = if (::farmModel.isInitialized) farmModel else return
        val maxBuys = if (bulkQuantity == BUY_MAX) BUY_MAX_SAFETY_LIMIT else bulkQuantity

        // Every tick, attempt to buy ALL enabled + unlocked crops in PlanetResources order
        // (red first, then orange, yellow … black). Each color has its own independent
        // attempt — not round-robin. A locked crop is skipped even if its toggle is ON.
        PlanetResources.entries.forEach { res ->
            val color = res.resourceName
            if (cropAutoEnabled[color] != true) return@forEach

            // Skip crops not yet unlocked via soil upgrades.
            val state = fm.stateForColor(color) ?: return@forEach
            if (!state.isUnlocked) return@forEach

            var bought = 0
            while (bought < maxBuys) {
                val cost = fm.stateForColor(color)?.cost ?: break

                // Smart-buy threshold: only buy if gold >= cost / threshold
                // threshold=1.0 → no restriction; threshold=0.1 → need 10× cost in gold
                val threshold = if (smartBuyUnlocked) (cropGoldThreshold[color] ?: 1.0f).toDouble() else 1.0
                val requiredGold = if (threshold >= 1.0) cost
                else cost.divide(BigDecimal(threshold), 10, RoundingMode.HALF_UP)

                if (fm.goldCoins < requiredGold) break
                stage.fire(BuyResourceEvent(color))
                bought++
            }
        }
    }

    // ── Auto best recipe (greedy) ─────────────────────────────────────────────

    private fun performAutoRecipe() {
        val km = if (::kitchenViewModel.isInitialized) kitchenViewModel else return
        if (km.discoveredRecipes.isEmpty()) return

        // Sort discovered recipes by estimated payout (descending)
        val sortedRecipes = km.discoveredRecipes
            .sortedByDescending { km.estimatedRecipePayout(it) }

        // Greedy assignment: pick best recipe, mark its colors used, continue
        val usedColors = mutableSetOf<String>()
        val toActivate = mutableListOf<Recipe>()
        for (recipe in sortedRecipes) {
            val colors = recipe.crops.map { it.color }
            if (colors.none { it in usedColors }) {
                toActivate.add(recipe)
                usedColors.addAll(colors)
            }
        }

        // Deactivate any currently active recipe not in the new optimal set
        km.activeRecipes
            .filter { active -> toActivate.none { it.id == active.id } }
            .forEach { km.deactivateRecipe(it) }

        // Activate optimal recipes not yet active
        toActivate
            .filter { target -> km.activeRecipes.none { it.id == target.id } }
            .forEach { km.activateRecipe(it) }
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    private fun load() {
        automationUnlocked  = preferences[PREF_UNLOCKED, false]
        soilAutoUnlocked    = preferences[PREF_SOIL_UNLOCKED, false]
        soilAutoEnabled     = preferences[PREF_SOIL_ENABLED, false]
        kitchenAutoUnlocked = preferences[PREF_KITCHEN_UNLOCKED, false]
        autoRecipeUnlocked  = preferences[PREF_RECIPE_UNLOCKED, false]
        autoRecipeEnabled   = preferences[PREF_RECIPE_ENABLED, false]
        smartBuyUnlocked    = preferences[PREF_SMART_BUY_UNLOCKED, false]

        // Fallback: check observatory discovery pref directly in case SmartBuyUnlockedEvent
        // was never persisted (e.g., feature added after discovery was already purchased)
        if (!smartBuyUnlocked && preferences[Discovery.MARKET_ANALYSIS.prefKey, false]) {
            smartBuyUnlocked = true
            preferences.flush { this[PREF_SMART_BUY_UNLOCKED] = true }
        }

        // Per-color enabled / threshold
        val enabledMap = cropAutoEnabled.toMutableMap()
        val thresholdMap = cropGoldThreshold.toMutableMap()
        PlanetResources.entries.forEach { res ->
            enabledMap[res.resourceName]   = preferences["${PREF_CROP_ENABLED_PREFIX}${res.resourceName}", false]
            thresholdMap[res.resourceName] = preferences["${PREF_CROP_THRESHOLD_PREFIX}${res.resourceName}", 1.0f]
        }
        cropAutoEnabled   = enabledMap
        cropGoldThreshold = thresholdMap

        // Derive tick interval + bulk quantity from saved barn upgrade levels
        tickIntervalFrames = when {
            preferences[BarnUpgrade.AUTOMATION_SPEED_3.prefKey, 0] >= 1 -> TICK_60_PER_SEC
            preferences[BarnUpgrade.AUTOMATION_SPEED_2.prefKey, 0] >= 1 -> TICK_10_PER_SEC
            preferences[BarnUpgrade.AUTOMATION_SPEED_1.prefKey, 0] >= 1 -> TICK_4_PER_SEC
            else                                                          -> TICK_1_PER_SEC
        }
        bulkQuantity = when {
            preferences[BarnUpgrade.AUTOMATION_BULK_MAX.prefKey, 0] >= 1 -> BUY_MAX
            preferences[BarnUpgrade.AUTOMATION_BULK_2.prefKey,   0] >= 1 -> 10
            preferences[BarnUpgrade.AUTOMATION_BULK_1.prefKey,   0] >= 1 -> 5
            else                                                           -> 1
        }
    }

    private fun resetState() {
        // Unlock flags reset because preferences.clear() wipes barn upgrade levels too
        automationUnlocked  = false
        soilAutoUnlocked    = false
        soilAutoEnabled     = false
        kitchenAutoUnlocked = false
        autoRecipeUnlocked  = false
        autoRecipeEnabled   = false
        smartBuyUnlocked    = false
        cropAutoEnabled     = PlanetResources.entries.associate { it.resourceName to false }
        cropGoldThreshold   = PlanetResources.entries.associate { it.resourceName to 1.0f }
        tickIntervalFrames  = TICK_1_PER_SEC
        bulkQuantity        = 1
        log.debug { "Automation state reset." }
    }

    companion object {
        private val log = logger<AutomationModel>()

        // Tick intervals (frames at 60 fps)
        const val TICK_1_PER_SEC  = 60
        const val TICK_4_PER_SEC  = 15
        const val TICK_10_PER_SEC = 6
        const val TICK_60_PER_SEC = 1

        /** Sentinel value meaning "buy as many as affordable". */
        const val BUY_MAX = -1
        /** Safety cap applied when bulkQuantity == BUY_MAX to prevent frame stalls. */
        const val BUY_MAX_SAFETY_LIMIT = 500

        /** How many automation ticks between auto-recipe re-evaluations (~2 s at max speed). */
        const val AUTO_RECIPE_INTERVAL = 120

        // Preference keys
        const val PREF_UNLOCKED              = "automation_unlocked"
        const val PREF_SOIL_UNLOCKED         = "automation_soil_auto_unlocked"
        const val PREF_SOIL_ENABLED          = "automation_soil_enabled"
        const val PREF_KITCHEN_UNLOCKED      = "automation_kitchen_unlocked"
        const val PREF_RECIPE_UNLOCKED       = "automation_recipe_unlocked"
        const val PREF_RECIPE_ENABLED        = "automation_recipe_enabled"
        const val PREF_SMART_BUY_UNLOCKED    = "automation_smart_buy_unlocked"
        const val PREF_CROP_ENABLED_PREFIX   = "automation_crop_"
        const val PREF_CROP_THRESHOLD_PREFIX = "automation_threshold_"
    }
}
