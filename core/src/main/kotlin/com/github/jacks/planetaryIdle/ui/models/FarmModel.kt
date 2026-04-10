package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.AchievementBonus
import com.github.jacks.planetaryIdle.components.AchievementComponent
import com.github.jacks.planetaryIdle.components.Achievements
import com.github.jacks.planetaryIdle.components.PlanetResources
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.components.ScoreResources
import com.github.jacks.planetaryIdle.components.UpgradeComponent
import com.github.jacks.planetaryIdle.events.AchievementCompletedEvent
import com.github.jacks.planetaryIdle.events.ActiveCropChangedEvent
import com.github.jacks.planetaryIdle.events.BarnEffectsChangedEvent
import com.github.jacks.planetaryIdle.events.BarnUnlockedEvent
import com.github.jacks.planetaryIdle.events.BuyResourceEvent
import com.github.jacks.planetaryIdle.events.CreditGoldEvent
import com.github.jacks.planetaryIdle.events.GameCompletedEvent
import com.github.jacks.planetaryIdle.events.ObservatoryEffects
import com.github.jacks.planetaryIdle.events.ObservatoryEffectsChangedEvent
import com.github.jacks.planetaryIdle.events.RecipeActivatedEvent
import com.github.jacks.planetaryIdle.events.RecipeDeactivatedEvent
import com.github.jacks.planetaryIdle.events.ResetGameEvent
import com.github.jacks.planetaryIdle.events.ResourceUpdateEvent
import com.github.jacks.planetaryIdle.events.UpdateBuyAmountEvent
import com.github.jacks.planetaryIdle.events.UpgradeSoilEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import ktx.app.gdxError
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.reflect.KMutableProperty0

data class ResourceModelState(
    val owned: BigDecimal = BigDecimal.ZERO,
    val cost: BigDecimal = BigDecimal.ZERO,
    val payout: BigDecimal = BigDecimal.ZERO,
    val cycleDuration: BigDecimal = BigDecimal.ZERO
)

class FarmModel(
    world: World,
    private val stage: Stage
) : PropertyChangeSource(), EventListener {

    private val preferences: Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }
    private val resourceComponents: ComponentMapper<ResourceComponent> = world.mapper()
    private val achievementComponents: ComponentMapper<AchievementComponent> = world.mapper()
    private val upgradeComponents: ComponentMapper<UpgradeComponent> = world.mapper()
    private val resourceEntities = world.family(allOf = arrayOf(ResourceComponent::class))
    private val achievementEntities = world.family(allOf = arrayOf(AchievementComponent::class))
    private val upgradeEntities = world.family(allOf = arrayOf(UpgradeComponent::class))

    var goldCoins by propertyNotify(BigDecimal(preferences["gold_coins", "5"]))
    var productionRate by propertyNotify(BigDecimal(preferences["production_rate", "0"]))
    var buyAmount by propertyNotify(preferences["buy_amount", 1f])

    var redState    by propertyNotify(initialState(PlanetResources.RED))
    var orangeState by propertyNotify(initialState(PlanetResources.ORANGE))
    var yellowState by propertyNotify(initialState(PlanetResources.YELLOW))
    var greenState  by propertyNotify(initialState(PlanetResources.GREEN))
    var blueState   by propertyNotify(initialState(PlanetResources.BLUE))
    var purpleState by propertyNotify(initialState(PlanetResources.PURPLE))
    var pinkState   by propertyNotify(initialState(PlanetResources.PINK))
    var brownState  by propertyNotify(initialState(PlanetResources.BROWN))
    var whiteState  by propertyNotify(initialState(PlanetResources.WHITE))
    var blackState  by propertyNotify(initialState(PlanetResources.BLACK))

    // Set by ResourceUpdateEvent; FarmView binds to this to trigger floating text animation.
    var lastProductionPayout by propertyNotify(Pair("", BigDecimal.ZERO))

    var achievementMultiplier by propertyNotify(BigDecimal(preferences["achievement_multiplier", "1"]))
    var soilUpgrades by propertyNotify(BigDecimal(preferences["soil_upgrades", "0"]))
    var gameCompleted by propertyNotify(false)

    // ── Special bonus flags (loaded from prefs on startup) ────────────────────
    var redProductionBonusActive: Boolean = preferences["bonus_red_production", false]
    var allProductionBonusActive: Boolean = preferences["bonus_all_production", false]
    var goldIncomeBonusActive: Boolean    = preferences["bonus_gold_income", false]

    private val RED_PRODUCTION_MULTIPLIER = BigDecimal("1.15")
    private val ALL_PRODUCTION_MULTIPLIER = BigDecimal("1.10")
    private val GOLD_INCOME_MULTIPLIER    = BigDecimal("1.05")

    /** Per-resource-name payout multiplier from barn value/expertise upgrades. */
    private val barnPayoutMultipliers = mutableMapOf<String, BigDecimal>()

    // ── Observatory effects ───────────────────────────────────────────────────
    private var observatoryEffects: ObservatoryEffects = ObservatoryEffects(
        productionMultiplier     = BigDecimal.ONE,
        cycleSpeedMultiplier     = BigDecimal.ONE,
        recipePayoutMultiplier   = BigDecimal.ONE,
        soilEffectivenessBonus   = BigDecimal.ZERO,
        insightMultiplier        = BigDecimal.ONE,
        perColorMultipliers      = emptyMap(),
        achievementMultiplierSquared = false,
    )

    // ── Recipe state ──────────────────────────────────────────────────────────

    /** color → partner color for active recipe pairs (bidirectional). */
    private val activeRecipePairs = mutableMapOf<String, String>()

    /** Per-color payout buffered when it fires first in a recipe pair. */
    private val recipePendingPayouts = mutableMapOf<String, BigDecimal>()

    /** Per-color base cycle duration (before recipe linking). */
    private val baseCycleDurations = mutableMapOf<String, BigDecimal>()

    private val stateProps: Map<String, KMutableProperty0<ResourceModelState>> = mapOf(
        PlanetResources.RED.resourceName    to ::redState,
        PlanetResources.ORANGE.resourceName to ::orangeState,
        PlanetResources.YELLOW.resourceName to ::yellowState,
        PlanetResources.GREEN.resourceName  to ::greenState,
        PlanetResources.BLUE.resourceName   to ::blueState,
        PlanetResources.PURPLE.resourceName to ::purpleState,
        PlanetResources.PINK.resourceName   to ::pinkState,
        PlanetResources.BROWN.resourceName  to ::brownState,
        PlanetResources.WHITE.resourceName  to ::whiteState,
        PlanetResources.BLACK.resourceName  to ::blackState,
    )

    init {
        stage.addListener(this)
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is BuyResourceEvent -> {
                val entity = getResourceEntityByName(event.resourceType)
                    ?: gdxError("No Entity with type: ${event.resourceType}")
                val rscComp = resourceComponents[entity]
                val wasZero = rscComp.amountOwned <= BigDecimal.ZERO
                deductPurchaseCost(rscComp)
                rscComp.amountOwned += buyAmount.toBigDecimal()
                updateModel(rscComp)
                persistResourceState(rscComp)
                // Unlock barn on first green purchase
                if (event.resourceType == PlanetResources.GREEN.resourceName && wasZero) {
                    preferences.flush { this["barn_unlocked"] = true }
                    stage.fire(BarnUnlockedEvent())
                }
            }
            is ResourceUpdateEvent -> {
                handleResourceUpdate(event.rscComp)
            }
            is CreditGoldEvent -> {
                goldCoins += event.amount
                preferences.flush { this["gold_coins"] = goldCoins.toString() }
            }
            is UpgradeSoilEvent -> {
                updateUpgradeComponents(event.amount)
                soilUpgrades += event.amount
                resetValuesFromSoilUpgrade()
            }
            is AchievementCompletedEvent -> {
                if (event.achId.isEmpty()) return false
                val ach = Achievements.entries.find { it.achId == event.achId }
                // Multiply by the current scale (may already be 1.06 if TheEnd was previously unlocked)
                val currentScale = achievementEntities.firstOrNull()
                    ?.let { achievementComponents[it].multiplierScale }
                    ?: BigDecimal("1.05")
                achievementMultiplier = achievementMultiplier.multiply(currentScale)
                preferences.flush { this["achievement_multiplier"] = achievementMultiplier.toString() }
                ach?.bonus?.let { applyBonus(it) }
            }
            is UpdateBuyAmountEvent -> {
                buyAmount = event.amount
                preferences.flush { this["buy_amount"] = buyAmount }
                updateAllModelStates()
            }
            is BarnEffectsChangedEvent -> {
                barnPayoutMultipliers.clear()
                barnPayoutMultipliers.putAll(event.payoutMultipliers)
                upgradeEntities.forEach { entity ->
                    upgradeComponents[entity].soilSpeedMultiplier = event.soilBaseMultiplier
                        .add(observatoryEffects.soilEffectivenessBonus)
                }
                updateModelProductionRate()
            }
            is ObservatoryEffectsChangedEvent -> {
                observatoryEffects = event.effects
                // Re-apply soil bonus when observatory effects change
                upgradeEntities.forEach { entity ->
                    val upgComp = upgradeComponents[entity]
                    upgComp.soilSpeedMultiplier = upgComp.soilSpeedMultiplier
                        .add(observatoryEffects.soilEffectivenessBonus)
                }
                updateModelProductionRate()
            }
            is ActiveCropChangedEvent -> {
                val entity = getResourceEntityByName(event.color) ?: return false
                val rscComp = resourceComponents[entity]
                rscComp.basePayout    = event.newBasePayout
                rscComp.cycleDuration = event.newCycleDuration
                rscComp.amountOwned   = BigDecimal.ZERO
                rscComp.currentTicks  = 0
                baseCycleDurations[event.color] = event.newCycleDuration
                updateModel(rscComp)
                persistResourceState(rscComp)
            }
            is RecipeActivatedEvent -> {
                // Note: multi-color (3+) recipe payout mechanics are not yet implemented;
                // only the first two colors are linked for now.
                val color1 = event.recipe.crops[0].color
                val color2 = event.recipe.crops[1].color
                val entity1 = getResourceEntityByName(color1) ?: return false
                val entity2 = getResourceEntityByName(color2) ?: return false
                val rsc1 = resourceComponents[entity1]
                val rsc2 = resourceComponents[entity2]
                // Store individual durations before linking
                baseCycleDurations.putIfAbsent(color1, rsc1.cycleDuration)
                baseCycleDurations.putIfAbsent(color2, rsc2.cycleDuration)
                val combined = BigDecimal(event.recipe.combinedTime.toString())
                rsc1.cycleDuration = combined
                rsc2.cycleDuration = combined
                rsc1.currentTicks  = 0
                rsc2.currentTicks  = 0
                activeRecipePairs[color1] = color2
                activeRecipePairs[color2] = color1
                updateModel(rsc1)
                updateModel(rsc2)
            }
            is RecipeDeactivatedEvent -> {
                val color1 = event.recipe.crops[0].color
                val color2 = event.recipe.crops[1].color
                activeRecipePairs.remove(color1)
                activeRecipePairs.remove(color2)
                recipePendingPayouts.remove(color1)
                recipePendingPayouts.remove(color2)
                // Restore individual cycle durations
                restoreCycleDuration(color1)
                restoreCycleDuration(color2)
            }
            is ResetGameEvent -> {
                resetGameValues()
            }
            is GameCompletedEvent -> {
                gameCompleted = true
            }
            else -> return false
        }
        return true
    }

    // ── Special bonus application ─────────────────────────────────────────────

    private fun applyBonus(bonus: AchievementBonus) {
        when (bonus) {
            is AchievementBonus.RedProductionBonus -> {
                redProductionBonusActive = true
                preferences.flush { this["bonus_red_production"] = true }
                updateModelProductionRate()
            }
            is AchievementBonus.AllProductionBonus -> {
                allProductionBonusActive = true
                preferences.flush { this["bonus_all_production"] = true }
                updateModelProductionRate()
            }
            is AchievementBonus.GoldIncomeBonus -> {
                goldIncomeBonusActive = true
                preferences.flush { this["bonus_gold_income"] = true }
                updateModelProductionRate()
            }
            is AchievementBonus.TheEndBonus -> {
                achievementEntities.forEach { entity ->
                    achievementComponents[entity].multiplierScale = BigDecimal("1.06")
                }
                // Recalculate: component has N-1 completed (current ach not yet added);
                // multiply once more by 1.06 to account for this achievement.
                achievementMultiplier = calculateAchievementMultiplier().multiply(BigDecimal("1.06"))
                preferences.flush {
                    this["achievement_multiplier"] = achievementMultiplier.toString()
                    this["achievement_multiplier_scale"] = "1.06"
                }
                updateModelProductionRate()
            }
            // SoilCostDiscount, PerfectSoilBonus, ResearchSpeedBonus are handled
            // by BarnViewModel and KitchenViewModel which also listen to AchievementCompletedEvent.
            is AchievementBonus.SoilCostDiscount,
            is AchievementBonus.PerfectSoilBonus,
            is AchievementBonus.ResearchSpeedBonus,
            is AchievementBonus.DescriptiveOnly -> { /* handled elsewhere or informational */ }
        }
    }

    // ── Resource update with recipe support ───────────────────────────────────

    private fun handleResourceUpdate(rscComp: ResourceComponent) {
        val color = rscComp.name
        val partnerColor = activeRecipePairs[color]

        if (partnerColor == null) {
            // Normal independent production
            val barnMult = barnPayoutMultipliers[color] ?: BigDecimal.ONE
            val obsColorMult = observatoryEffects.perColorMultipliers[color] ?: BigDecimal.ONE
            val adjustedPayout = rscComp.payout
                .multiply(calculateAchievementMultiplier())
                .multiply(barnMult)
                .multiply(colorProductionBonus(color))
                .multiply(goldIncomeMultiplier())
                .multiply(observatoryEffects.productionMultiplier)
                .multiply(obsColorMult)
            goldCoins += adjustedPayout
            preferences.flush { this["gold_coins"] = goldCoins.toString() }
            lastProductionPayout = color to adjustedPayout
            updateModelProductionRate()
        } else {
            // This color is in a recipe pair
            val barnMult = barnPayoutMultipliers[color] ?: BigDecimal.ONE
            val obsColorMult = observatoryEffects.perColorMultipliers[color] ?: BigDecimal.ONE
            val myRawPayout = rscComp.payout
                .multiply(barnMult)
                .multiply(colorProductionBonus(color))
                .multiply(obsColorMult)
            val partnerPending = recipePendingPayouts[partnerColor]

            if (partnerPending != null) {
                // Partner already fired — complete the pair
                val combined = myRawPayout
                    .multiply(partnerPending)
                    .multiply(calculateAchievementMultiplier())
                    .multiply(goldIncomeMultiplier())
                    .multiply(observatoryEffects.productionMultiplier)
                    .multiply(observatoryEffects.recipePayoutMultiplier)
                goldCoins += combined
                preferences.flush { this["gold_coins"] = goldCoins.toString() }
                lastProductionPayout = color to combined
                updateModelProductionRate()
                recipePendingPayouts.remove(color)
                recipePendingPayouts.remove(partnerColor)
            } else {
                // Buffer and wait for partner
                recipePendingPayouts[color] = myRawPayout
            }
        }
    }

    /** Per-color production multiplier from Red Giant and Full Spectrum bonuses. */
    private fun colorProductionBonus(color: String): BigDecimal {
        var mult = BigDecimal.ONE
        if (allProductionBonusActive) mult = mult.multiply(ALL_PRODUCTION_MULTIPLIER)
        if (redProductionBonusActive && color == PlanetResources.RED.resourceName) {
            mult = mult.multiply(RED_PRODUCTION_MULTIPLIER)
        }
        return mult
    }

    /** Gold income multiplier from the Trillionaire! bonus. */
    private fun goldIncomeMultiplier(): BigDecimal =
        if (goldIncomeBonusActive) GOLD_INCOME_MULTIPLIER else BigDecimal.ONE

    private fun restoreCycleDuration(color: String) {
        val entity = getResourceEntityByName(color) ?: return
        val rscComp = resourceComponents[entity]
        val base = baseCycleDurations[color]
            ?: BigDecimal(PlanetResources.entries.find { it.resourceName == color }?.cycleDuration ?: "60")
        rscComp.cycleDuration = base
        updateModel(rscComp)
    }

    // ── Existing helpers ──────────────────────────────────────────────────────

    private fun getResourceEntityByName(name: String): Entity? {
        resourceEntities.forEach { entity ->
            if (resourceComponents[entity].name == name) return entity
        }
        return null
    }

    private fun deductPurchaseCost(rscComp: ResourceComponent) {
        goldCoins -= rscComp.cost
        preferences.flush { this["gold_coins"] = goldCoins.toString() }
    }

    private fun updateModel(rscComp: ResourceComponent) {
        stateProps[rscComp.name]?.set(
            ResourceModelState(
                owned = rscComp.amountOwned,
                cost = rscComp.cost,
                payout = rscComp.payout,
                cycleDuration = rscComp.cycleDuration
            )
        )
    }

    private fun persistResourceState(rscComp: ResourceComponent) {
        preferences.flush {
            this["${rscComp.name}_owned"] = rscComp.amountOwned.toString()
            this["${rscComp.name}_cost"] = rscComp.cost.toString()
        }
    }

    private fun updateModelProductionRate() {
        val achMult = calculateAchievementMultiplier()
        val goldMult = goldIncomeMultiplier()
        var total = BigDecimal.ZERO
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            if (rscComp.name == ScoreResources.GOLD_COINS.resourceName) return@forEach
            if (rscComp.amountOwned > BigDecimal.ZERO && rscComp.cycleDuration > BigDecimal.ZERO) {
                val barnMult = barnPayoutMultipliers[rscComp.name] ?: BigDecimal.ONE
                val obsColorMult = observatoryEffects.perColorMultipliers[rscComp.name] ?: BigDecimal.ONE
                total += rscComp.payout
                    .multiply(achMult)
                    .multiply(barnMult)
                    .multiply(colorProductionBonus(rscComp.name))
                    .multiply(goldMult)
                    .multiply(observatoryEffects.productionMultiplier)
                    .multiply(obsColorMult)
                    .divide(rscComp.cycleDuration, 10, RoundingMode.HALF_UP)
            }
        }
        productionRate = total
        preferences.flush { this["production_rate"] = productionRate.toString() }
    }

    private fun updateAllModelStates() {
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            if (rscComp.name != ScoreResources.GOLD_COINS.resourceName) updateModel(rscComp)
        }
    }

    private fun updateUpgradeComponents(amount: BigDecimal) {
        upgradeEntities.forEach { entity ->
            val upgComp = upgradeComponents[entity]
            upgComp.soilUpgrades += amount
            upgComp.isUnlocked = true
        }
    }

    private fun calculateAchievementMultiplier(): BigDecimal {
        var mult = BigDecimal.ONE
        achievementEntities.forEach { entity ->
            mult = mult.multiply(achievementComponents[entity].achMultiplier)
        }
        // Resonance Field (Observatory): apply achievement multiplier twice
        if (observatoryEffects.achievementMultiplierSquared) {
            mult = mult.multiply(mult)
        }
        return mult
    }

    private fun resetValuesFromSoilUpgrade() {
        activeRecipePairs.clear()
        recipePendingPayouts.clear()
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            rscComp.amountOwned = BigDecimal.ZERO
            rscComp.currentTicks = 0
        }
        PlanetResources.entries.forEach { resource ->
            val entity = getResourceEntityByName(resource.resourceName)
                ?: gdxError("No Entity with type: ${resource.resourceName}")
            updateModel(resourceComponents[entity])
        }
        goldCoins = BigDecimal("5")
        productionRate = BigDecimal.ZERO
        preferences.flush {
            this["gold_coins"] = goldCoins.toString()
            this["production_rate"] = productionRate.toString()
            this["soil_upgrades"] = soilUpgrades.toString()
            PlanetResources.entries.forEach { resource ->
                this["${resource.resourceName}_owned"] = "0"
                this["${resource.resourceName}_cost"] = resource.baseCost
            }
        }
    }

    private fun resetGameValues() {
        activeRecipePairs.clear()
        recipePendingPayouts.clear()
        baseCycleDurations.clear()
        resourceEntities.forEach { entity ->
            resourceComponents[entity].amountOwned = BigDecimal.ZERO
        }
        PlanetResources.entries.forEach { resource ->
            val entity = getResourceEntityByName(resource.resourceName)
                ?: gdxError("No Entity with type: ${resource.resourceName}")
            updateModel(resourceComponents[entity])
        }
        goldCoins = BigDecimal("5")
        productionRate = BigDecimal.ZERO
        buyAmount = 1f
        gameCompleted = false
        soilUpgrades = BigDecimal.ZERO
        barnPayoutMultipliers.clear()
        redProductionBonusActive = false
        allProductionBonusActive = false
        goldIncomeBonusActive = false
        preferences.flush { preferences.clear() }
    }

    private fun initialState(resource: PlanetResources): ResourceModelState {
        val owned = BigDecimal(preferences["${resource.resourceName}_owned", "0"])
        val cost = BigDecimal(preferences["${resource.resourceName}_cost", resource.baseCost])
        val cycleDuration = BigDecimal(resource.cycleDuration)
        val payout = if (owned <= BigDecimal.ZERO) BigDecimal.ZERO else {
            val base = BigDecimal(resource.basePayout)
            val scaled = Math.pow(owned.toDouble(), 0.75).toBigDecimal()
            var milestone = BigDecimal.ONE
            val o = owned.toInt()
            if (o >= 10)   milestone = milestone.multiply(BigDecimal("1.2"))
            if (o >= 25)   milestone = milestone.multiply(BigDecimal("1.5"))
            if (o >= 50)   milestone = milestone.multiply(BigDecimal("2.0"))
            if (o >= 100)  milestone = milestone.multiply(BigDecimal("3.0"))
            if (o >= 250)  milestone = milestone.multiply(BigDecimal("5.0"))
            if (o >= 500)  milestone = milestone.multiply(BigDecimal("15.0"))
            if (o >= 1000) milestone = milestone.multiply(BigDecimal("50.0"))
            if (o >= 5000) milestone = milestone.multiply(BigDecimal("200.0"))
            base.multiply(scaled).multiply(milestone)
        }
        return ResourceModelState(owned = owned, cost = cost, payout = payout, cycleDuration = cycleDuration)
    }

    companion object {
        private val log = logger<FarmModel>()
    }
}
