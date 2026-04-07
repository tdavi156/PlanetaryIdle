package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.AchievementComponent
import com.github.jacks.planetaryIdle.components.PlanetResources
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.components.ScoreResources
import com.github.jacks.planetaryIdle.components.UpgradeComponent
import com.github.jacks.planetaryIdle.events.AchievementCompletedEvent
import com.github.jacks.planetaryIdle.events.BarnEffectsChangedEvent
import com.github.jacks.planetaryIdle.events.BarnUnlockedEvent
import com.github.jacks.planetaryIdle.events.BuyResourceEvent
import com.github.jacks.planetaryIdle.events.CreditGoldEvent
import com.github.jacks.planetaryIdle.events.GameCompletedEvent
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

    /** Per-resource-name payout multiplier from barn value/expertise upgrades. */
    private val barnPayoutMultipliers = mutableMapOf<String, BigDecimal>()

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
                val barnMult = barnPayoutMultipliers[event.rscComp.name] ?: BigDecimal.ONE
                val adjustedPayout = event.rscComp.payout
                    .multiply(calculateAchievementMultiplier())
                    .multiply(barnMult)
                goldCoins += adjustedPayout
                preferences.flush { this["gold_coins"] = goldCoins.toString() }
                lastProductionPayout = event.rscComp.name to adjustedPayout
                updateModelProductionRate()
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
                achievementMultiplier = achievementMultiplier.multiply(BigDecimal("1.05"))
                preferences.flush { this["achievement_multiplier"] = achievementMultiplier.toString() }
            }
            is UpdateBuyAmountEvent -> {
                buyAmount = event.amount
                preferences.flush { this["buy_amount"] = buyAmount }
                updateAllModelStates()
            }
            is BarnEffectsChangedEvent -> {
                barnPayoutMultipliers.clear()
                barnPayoutMultipliers.putAll(event.payoutMultipliers)
                // Update soil component base multiplier from Improved Soil Quality
                upgradeEntities.forEach { entity ->
                    upgradeComponents[entity].soilSpeedMultiplier = event.soilBaseMultiplier
                }
                updateModelProductionRate()
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
        var total = BigDecimal.ZERO
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            if (rscComp.name == ScoreResources.GOLD_COINS.resourceName) return@forEach
            if (rscComp.amountOwned > BigDecimal.ZERO && rscComp.cycleDuration > BigDecimal.ZERO) {
                val barnMult = barnPayoutMultipliers[rscComp.name] ?: BigDecimal.ONE
                total += rscComp.payout
                    .multiply(achMult)
                    .multiply(barnMult)
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
        return mult
    }

    private fun resetValuesFromSoilUpgrade() {
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
            if (o >= 10)  milestone = milestone.multiply(BigDecimal("1.2"))
            if (o >= 25)  milestone = milestone.multiply(BigDecimal("1.5"))
            if (o >= 50)  milestone = milestone.multiply(BigDecimal("2.0"))
            if (o >= 100) milestone = milestone.multiply(BigDecimal("3.0"))
            base.multiply(scaled).multiply(milestone)
        }
        return ResourceModelState(owned = owned, cost = cost, payout = payout, cycleDuration = cycleDuration)
    }

    companion object {
        private val log = logger<FarmModel>()
    }
}
