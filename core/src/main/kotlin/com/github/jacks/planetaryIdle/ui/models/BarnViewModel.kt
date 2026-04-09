package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.PlanetaryIdle.Companion.MATH_CONTEXT
import com.github.jacks.planetaryIdle.components.BarnUpgrade
import com.github.jacks.planetaryIdle.components.PlanetResources
import com.github.jacks.planetaryIdle.components.AchievementBonus
import com.github.jacks.planetaryIdle.components.Achievements
import com.github.jacks.planetaryIdle.events.AchievementCompletedEvent
import com.github.jacks.planetaryIdle.events.AchievementNotificationEvent
import com.github.jacks.planetaryIdle.events.BarnEffectsChangedEvent
import com.github.jacks.planetaryIdle.events.BarnUnlockedEvent
import com.github.jacks.planetaryIdle.events.BuyBarnUpgradeEvent
import com.github.jacks.planetaryIdle.events.CropUnlockedEvent
import com.github.jacks.planetaryIdle.events.KitchenUnlockedEvent
import com.github.jacks.planetaryIdle.events.ResetGameEvent
import com.github.jacks.planetaryIdle.events.UpgradeSoilEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.quillraven.fleks.World
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import java.math.BigDecimal
import java.math.RoundingMode

data class BarnUpgradeState(
    val level: Int,
    val maxLevel: Int,
    val cost: BigDecimal,
    val isRevealed: Boolean,
    val isMaxed: Boolean,
)

class BarnViewModel(
    @Suppress("UNUSED_PARAMETER") world: World,
    private val stage: Stage,
    val farmModel: FarmModel,
) : PropertyChangeSource(), EventListener {

    private val preferences: Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }

    // ── Persisted state ───────────────────────────────────────────────────────
    private val levels = mutableMapOf<BarnUpgrade, Int>()

    // ── Bonus flags (loaded from prefs on startup) ────────────────────────────
    var soilCostDiscountActive: Boolean = false
    var perfectSoilBonusActive: Boolean = false

    /** Set after construction by GameScreen to avoid circular dependency. */
    var kitchenViewModel: KitchenViewModel? = null

    // ── Observable model properties ───────────────────────────────────────────
    var barnUnlocked by propertyNotify(false)
    /** Snapshot of all upgrade states; fires whenever any level changes. */
    var upgradeStates by propertyNotify(emptyMap<BarnUpgrade, BarnUpgradeState>())

    init {
        stage.addListener(this)
        loadFromPreferences()
        barnUnlocked = preferences["barn_unlocked", false]
        soilCostDiscountActive = preferences["bonus_soil_cost_discount", false]
        perfectSoilBonusActive = preferences["bonus_perfect_soil", false]
        upgradeStates = buildUpgradeStates()
    }

    // ── EventListener ────────────────────────────────────────────────────────
    override fun handle(event: Event): Boolean {
        when (event) {
            is BuyBarnUpgradeEvent -> {
                handlePurchase(event.upgrade)
                return true
            }
            is BarnUnlockedEvent -> {
                if (!barnUnlocked) {
                    barnUnlocked = true
                    preferences.flush { this["barn_unlocked"] = true }
                }
                return false  // let other listeners also receive it
            }
            is CropUnlockedEvent -> {
                // A new crop was researched — re-broadcast expertise multipliers
                stage.fire(buildEffectsEvent())
                return false
            }
            is AchievementCompletedEvent -> {
                if (event.achId.isEmpty()) return false
                val ach = Achievements.entries.find { it.achId == event.achId }
                when (ach?.bonus) {
                    is AchievementBonus.SoilCostDiscount -> {
                        soilCostDiscountActive = true
                        preferences.flush { this["bonus_soil_cost_discount"] = true }
                        upgradeStates = buildUpgradeStates()
                    }
                    is AchievementBonus.PerfectSoilBonus -> {
                        perfectSoilBonusActive = true
                        preferences.flush { this["bonus_perfect_soil"] = true }
                        stage.fire(buildEffectsEvent())
                    }
                    else -> {}
                }
                return false
            }
            is ResetGameEvent -> {
                soilCostDiscountActive = false
                perfectSoilBonusActive = false
                return false
            }
            else -> return false
        }
    }

    // ── Purchase logic ────────────────────────────────────────────────────────
    private fun handlePurchase(upgrade: BarnUpgrade) {
        val state = upgradeStates[upgrade] ?: return
        if (state.isMaxed) return
        if (farmModel.goldCoins < state.cost) return

        val newLevel = state.level + 1
        levels[upgrade] = newLevel

        // Deduct gold
        farmModel.goldCoins -= state.cost
        preferences.flush { this["gold_coins"] = farmModel.goldCoins.toString() }

        // Persist upgrade level
        preferences.flush { this[upgrade.prefKey] = newLevel }

        // Soil: delegate effects to FarmModel via UpgradeSoilEvent (handles reset + unlock logic)
        if (upgrade == BarnUpgrade.SOIL) {
            stage.fire(UpgradeSoilEvent(BigDecimal.ONE))
        }

        // Kitchen: fire unlock event (persisted by KitchenViewModel/MenuModel listeners)
        if (upgrade == BarnUpgrade.KITCHEN) {
            stage.fire(KitchenUnlockedEvent())
        }

        // Broadcast updated effects to systems
        stage.fire(buildEffectsEvent())

        // Refresh states (reveals newly connected nodes)
        upgradeStates = buildUpgradeStates()

        // Barn purchase achievement milestones (total non-soil upgrades purchased)
        val totalPurchased = levels.entries
            .filter { (u, _) -> u != BarnUpgrade.SOIL }
            .sumOf { (_, lvl) -> lvl }
        if (totalPurchased >= 1)  stage.fire(AchievementNotificationEvent("barn_1"))
        if (totalPurchased >= 5)  stage.fire(AchievementNotificationEvent("barn_5"))
        if (totalPurchased >= 10) stage.fire(AchievementNotificationEvent("barn_10"))
        if (totalPurchased >= 15) stage.fire(AchievementNotificationEvent("barn_15"))

        log.debug { "Purchased ${upgrade.displayName} → level $newLevel" }
    }

    // ── Effects computation ───────────────────────────────────────────────────

    /** Payout multiplier for a given resource name from value and expertise upgrades. */
    fun getPayoutMultiplier(resourceName: String): BigDecimal {
        val valueUpgrade = BarnUpgrade.valueUpgradeFor[resourceName]
        val valueLevel = if (valueUpgrade != null) levels[valueUpgrade] ?: 0 else 0
        val valueMult = if (valueLevel > 0) BigDecimal("1.1").pow(valueLevel, MATH_CONTEXT) else BigDecimal.ONE

        // Expertise: multiply by number of crop types researched for this color (if upgrade purchased)
        val expertiseUpgrade = BarnUpgrade.expertiseUpgradeFor[resourceName]
        val expertiseLevel = if (expertiseUpgrade != null) levels[expertiseUpgrade] ?: 0 else 0
        val expertiseMult = if (expertiseLevel >= 1) {
            val cropCount = kitchenViewModel?.unlockedCropCount(resourceName) ?: 0
            if (cropCount > 0) BigDecimal(cropCount) else BigDecimal.ONE
        } else BigDecimal.ONE

        return valueMult.multiply(expertiseMult)
    }

    /** Combined speed multiplier from Improved Seeds. */
    fun getSpeedMultiplier(): BigDecimal {
        val level = levels[BarnUpgrade.IMPROVED_SEEDS] ?: 0
        if (level == 0) return BigDecimal.ONE
        return BigDecimal("1.05").pow(level, MATH_CONTEXT)
    }

    /** Base per-level soil speed multiplier, boosted by Improved Soil Quality.
     *  Perfect Soil achievement raises the base from x1.5 to x1.75. */
    fun getSoilBaseMultiplier(): BigDecimal {
        val level = levels[BarnUpgrade.IMPROVED_SOIL_QUALITY] ?: 0
        val base = if (perfectSoilBonusActive) BigDecimal("1.75") else BigDecimal("1.5")
        return base.multiply(BigDecimal("1.05").pow(level, MATH_CONTEXT))
    }

    private fun buildEffectsEvent(): BarnEffectsChangedEvent {
        val payoutMultipliers = PlanetResources.entries.associate { resource ->
            resource.resourceName to getPayoutMultiplier(resource.resourceName)
        }
        return BarnEffectsChangedEvent(
            payoutMultipliers = payoutMultipliers,
            speedMultiplier   = getSpeedMultiplier(),
            soilBaseMultiplier = getSoilBaseMultiplier(),
        )
    }

    // ── Upgrade state snapshot ────────────────────────────────────────────────
    private fun buildUpgradeStates(): Map<BarnUpgrade, BarnUpgradeState> =
        BarnUpgrade.entries.associateWith { upgrade ->
            val level = levels[upgrade] ?: 0
            val isMaxed = level >= upgrade.maxLevel
            BarnUpgradeState(
                level      = level,
                maxLevel   = upgrade.maxLevel,
                cost       = costFor(upgrade, level),
                isRevealed = isRevealed(upgrade),
                isMaxed    = isMaxed,
            )
        }

    private fun isRevealed(upgrade: BarnUpgrade): Boolean {
        if (upgrade == BarnUpgrade.SOIL) return barnUnlocked
        val prereqs = BarnUpgrade.prerequisites[upgrade] ?: return false
        return prereqs.all { (levels[it] ?: 0) >= 1 }
    }

    fun costFor(upgrade: BarnUpgrade, atLevel: Int): BigDecimal {
        val raw = if (upgrade.maxLevel == 1) upgrade.baseCost
                  else upgrade.baseCost.multiply(upgrade.costScaling.pow(atLevel, MATH_CONTEXT))
        // Tilled Earth bonus: soil upgrades cost 10% less
        return if (soilCostDiscountActive && upgrade == BarnUpgrade.SOIL)
            raw.multiply(BigDecimal("0.90"), MATH_CONTEXT)
        else raw
    }

    // ── Persistence ───────────────────────────────────────────────────────────
    private fun loadFromPreferences() {
        BarnUpgrade.entries.forEach { upgrade ->
            // SOIL level is authoritative in FarmModel via "soil_upgrades"; mirror it here.
            val level = if (upgrade == BarnUpgrade.SOIL) {
                preferences["soil_upgrades", "0"].let { BigDecimal(it).toInt() }
            } else {
                preferences[upgrade.prefKey, 0]
            }
            levels[upgrade] = level
        }
        // Fire effects on load so ResourceUpdateSystem and FarmModel start with correct values
        stage.fire(buildEffectsEvent())
    }

    /** Called by GameScreen after all listeners are wired, to broadcast initial state. */
    fun fireInitialEffects() {
        stage.fire(buildEffectsEvent())
        upgradeStates = buildUpgradeStates()
    }

    companion object {
        private val log = logger<BarnViewModel>()
    }
}
