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
import kotlin.reflect.KMutableProperty0
import com.github.jacks.planetaryIdle.events.AchievementCompletedEvent
import com.github.jacks.planetaryIdle.events.BuyResourceEvent
import com.github.jacks.planetaryIdle.events.GameCompletedEvent
import com.github.jacks.planetaryIdle.events.ResetGameEvent
import com.github.jacks.planetaryIdle.events.ResourceUpdateEvent
import com.github.jacks.planetaryIdle.events.UpdateBuyAmountEvent
import com.github.jacks.planetaryIdle.events.UpgradeSoilEvent
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import ktx.app.gdxError
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import java.math.*

class PlanetModel(
    world : World,
    stage : Stage
) : PropertyChangeSource(), EventListener {

    private val preferences : Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }
    private val resourceComponents : ComponentMapper<ResourceComponent> = world.mapper()
    private val achievementComponents : ComponentMapper<AchievementComponent> = world.mapper()
    private val upgradeComponents : ComponentMapper<UpgradeComponent> = world.mapper()
    private val resourceEntities = world.family(allOf = arrayOf(ResourceComponent::class))
    private val achievementEntities = world.family(allOf = arrayOf(AchievementComponent::class))
    private val upgradeEntities = world.family(allOf = arrayOf(UpgradeComponent::class))

    var goldCoins by propertyNotify(BigDecimal(preferences["gold_coins", "5"]))
    var productionRate by propertyNotify(BigDecimal(preferences["production_rate", "0"]))
    var buyAmount by propertyNotify(preferences["buy_amount", 1f])

    var redOwned by propertyNotify(BigDecimal(preferences["red_owned", "0"]))
    var redCost by propertyNotify(BigDecimal(preferences["red_cost", "1"]))
    var redValue by propertyNotify(BigDecimal(preferences["red_value", "0"]))
    var redValueIncrease by propertyNotify(BigDecimal(preferences["red_value_increase", "0.04"]))
    var redRate by propertyNotify(BigDecimal(preferences["red_rate", "1.3"]))
    var redRateIncrease by propertyNotify(BigDecimal(preferences["red_rate_increase", "0.17"]))

    var orangeOwned by propertyNotify(BigDecimal(preferences["orange_owned", "0"]))
    var orangeCost by propertyNotify(BigDecimal(preferences["orange_cost", "100"]))
    var orangeValue by propertyNotify(BigDecimal(preferences["orange_value", "0"]))
    var orangeValueIncrease by propertyNotify(BigDecimal(preferences["orange_value_increase", "2.4"]))
    var orangeRate by propertyNotify(BigDecimal(preferences["orange_rate", "0.95"]))
    var orangeRateIncrease by propertyNotify(BigDecimal(preferences["orange_rate_increase", "0.11"]))

    var yellowOwned by propertyNotify(BigDecimal(preferences["yellow_owned", "0"]))
    var yellowCost by propertyNotify(BigDecimal(preferences["yellow_cost", "1"]))
    var yellowValue by propertyNotify(BigDecimal(preferences["yellow_value", "0"]))
    var yellowValueIncrease by propertyNotify(BigDecimal(preferences["yellow_value_increase", "0.04"]))
    var yellowRate by propertyNotify(BigDecimal(preferences["yellow_rate", "1.3"]))
    var yellowRateIncrease by propertyNotify(BigDecimal(preferences["yellow_rate_increase", "0.17"]))

    var greenOwned by propertyNotify(BigDecimal(preferences["green_owned", "0"]))
    var greenCost by propertyNotify(BigDecimal(preferences["green_cost", "1"]))
    var greenValue by propertyNotify(BigDecimal(preferences["green_value", "0"]))
    var greenValueIncrease by propertyNotify(BigDecimal(preferences["green_value_increase", "0.04"]))
    var greenRate by propertyNotify(BigDecimal(preferences["green_rate", "1.3"]))
    var greenRateIncrease by propertyNotify(BigDecimal(preferences["green_rate_increase", "0.17"]))

    var blueOwned by propertyNotify(BigDecimal(preferences["blue_owned", "0"]))
    var blueCost by propertyNotify(BigDecimal(preferences["blue_cost", "1"]))
    var blueValue by propertyNotify(BigDecimal(preferences["blue_value", "0"]))
    var blueValueIncrease by propertyNotify(BigDecimal(preferences["blue_value_increase", "0.04"]))
    var blueRate by propertyNotify(BigDecimal(preferences["blue_rate", "1.3"]))
    var blueRateIncrease by propertyNotify(BigDecimal(preferences["blue_rate_increase", "0.17"]))

    var purpleOwned by propertyNotify(BigDecimal(preferences["purple_owned", "0"]))
    var purpleCost by propertyNotify(BigDecimal(preferences["purple_cost", "1"]))
    var purpleValue by propertyNotify(BigDecimal(preferences["purple_value", "0"]))
    var purpleValueIncrease by propertyNotify(BigDecimal(preferences["purple_value_increase", "0.04"]))
    var purpleRate by propertyNotify(BigDecimal(preferences["purple_rate", "1.3"]))
    var purpleRateIncrease by propertyNotify(BigDecimal(preferences["purple_rate_increase", "0.17"]))

    var pinkOwned by propertyNotify(BigDecimal(preferences["pink_owned", "0"]))
    var pinkCost by propertyNotify(BigDecimal(preferences["pink_cost", "1"]))
    var pinkValue by propertyNotify(BigDecimal(preferences["pink_value", "0"]))
    var pinkValueIncrease by propertyNotify(BigDecimal(preferences["pink_value_increase", "0.04"]))
    var pinkRate by propertyNotify(BigDecimal(preferences["pink_rate", "1.3"]))
    var pinkRateIncrease by propertyNotify(BigDecimal(preferences["pink_rate_increase", "0.17"]))

    var brownOwned by propertyNotify(BigDecimal(preferences["brown_owned", "0"]))
    var brownCost by propertyNotify(BigDecimal(preferences["brown_cost", "1"]))
    var brownValue by propertyNotify(BigDecimal(preferences["brown_value", "0"]))
    var brownValueIncrease by propertyNotify(BigDecimal(preferences["brown_value_increase", "0.04"]))
    var brownRate by propertyNotify(BigDecimal(preferences["brown_rate", "1.3"]))
    var brownRateIncrease by propertyNotify(BigDecimal(preferences["brown_rate_increase", "0.17"]))

    var whiteOwned by propertyNotify(BigDecimal(preferences["white_owned", "0"]))
    var whiteCost by propertyNotify(BigDecimal(preferences["white_cost", "1"]))
    var whiteValue by propertyNotify(BigDecimal(preferences["white_value", "0"]))
    var whiteValueIncrease by propertyNotify(BigDecimal(preferences["white_value_increase", "0.04"]))
    var whiteRate by propertyNotify(BigDecimal(preferences["white_rate", "1.3"]))
    var whiteRateIncrease by propertyNotify(BigDecimal(preferences["white_rate_increase", "0.17"]))

    var blackOwned by propertyNotify(BigDecimal(preferences["black_owned", "0"]))
    var blackCost by propertyNotify(BigDecimal(preferences["black_cost", "1"]))
    var blackValue by propertyNotify(BigDecimal(preferences["black_value", "0"]))
    var blackValueIncrease by propertyNotify(BigDecimal(preferences["black_value_increase", "0.04"]))
    var blackRate by propertyNotify(BigDecimal(preferences["black_rate", "1.3"]))
    var blackRateIncrease by propertyNotify(BigDecimal(preferences["black_rate_increase", "0.17"]))

    private val ownedProps : Map<String, KMutableProperty0<BigDecimal>> = mapOf(
        PlanetResources.RED.resourceName    to ::redOwned,
        PlanetResources.ORANGE.resourceName to ::orangeOwned,
        PlanetResources.YELLOW.resourceName to ::yellowOwned,
        PlanetResources.GREEN.resourceName  to ::greenOwned,
        PlanetResources.BLUE.resourceName   to ::blueOwned,
        PlanetResources.PURPLE.resourceName to ::purpleOwned,
        PlanetResources.PINK.resourceName   to ::pinkOwned,
        PlanetResources.BROWN.resourceName  to ::brownOwned,
        PlanetResources.WHITE.resourceName  to ::whiteOwned,
        PlanetResources.BLACK.resourceName  to ::blackOwned,
    )
    private val costProps : Map<String, KMutableProperty0<BigDecimal>> = mapOf(
        PlanetResources.RED.resourceName    to ::redCost,
        PlanetResources.ORANGE.resourceName to ::orangeCost,
        PlanetResources.YELLOW.resourceName to ::yellowCost,
        PlanetResources.GREEN.resourceName  to ::greenCost,
        PlanetResources.BLUE.resourceName   to ::blueCost,
        PlanetResources.PURPLE.resourceName to ::purpleCost,
        PlanetResources.PINK.resourceName   to ::pinkCost,
        PlanetResources.BROWN.resourceName  to ::brownCost,
        PlanetResources.WHITE.resourceName  to ::whiteCost,
        PlanetResources.BLACK.resourceName  to ::blackCost,
    )
    private val valueProps : Map<String, KMutableProperty0<BigDecimal>> = mapOf(
        PlanetResources.RED.resourceName    to ::redValue,
        PlanetResources.ORANGE.resourceName to ::orangeValue,
        PlanetResources.YELLOW.resourceName to ::yellowValue,
        PlanetResources.GREEN.resourceName  to ::greenValue,
        PlanetResources.BLUE.resourceName   to ::blueValue,
        PlanetResources.PURPLE.resourceName to ::purpleValue,
        PlanetResources.PINK.resourceName   to ::pinkValue,
        PlanetResources.BROWN.resourceName  to ::brownValue,
        PlanetResources.WHITE.resourceName  to ::whiteValue,
        PlanetResources.BLACK.resourceName  to ::blackValue,
    )
    private val rateProps : Map<String, KMutableProperty0<BigDecimal>> = mapOf(
        PlanetResources.RED.resourceName    to ::redRate,
        PlanetResources.ORANGE.resourceName to ::orangeRate,
        PlanetResources.YELLOW.resourceName to ::yellowRate,
        PlanetResources.GREEN.resourceName  to ::greenRate,
        PlanetResources.BLUE.resourceName   to ::blueRate,
        PlanetResources.PURPLE.resourceName to ::purpleRate,
        PlanetResources.PINK.resourceName   to ::pinkRate,
        PlanetResources.BROWN.resourceName  to ::brownRate,
        PlanetResources.WHITE.resourceName  to ::whiteRate,
        PlanetResources.BLACK.resourceName  to ::blackRate,
    )

    var achievementMultiplier by propertyNotify(BigDecimal(preferences["achievement_multiplier", "1"]))

    var soilCost by propertyNotify(BigDecimal(preferences["soil_cost", "1000000"]))
    var soilUpgrades by propertyNotify(BigDecimal(preferences["soi_upgrades", "0"]))

    var gameCompleted by propertyNotify(false)

    init {
        stage.addListener(this)
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is BuyResourceEvent -> {
                val entity = getResourceEntityByName(event.resourceType) ?: gdxError("No Entity with type: ${event.resourceType}")
                val rscComp = resourceComponents[entity]
                updateGoldCoins(rscComp)
                updateModelAmount(rscComp)
                updateResourceComponent(rscComp)
                updateModel(rscComp)
            }
            is ResourceUpdateEvent -> {
                val valueMultiplier = calculateValueMultiplier()
                updateModelProductionRate()
                updateModelValue(event.rscComp)
                goldCoins += (event.rscComp.value * valueMultiplier)
                preferences.flush { this["gold_coins"] = goldCoins.toString() }
            }
            is UpgradeSoilEvent -> {
                updateUpgradeComponents(event.amount)
                soilUpgrades += event.amount
                resetValuesFromSoilUpgrade()
            }
            is AchievementCompletedEvent -> {
                achievementMultiplier = calculateAchievementMultiplier()
                preferences.flush { this["achievement_multiplier"] = achievementMultiplier }
            }
            is UpdateBuyAmountEvent -> {
                buyAmount = event.amount
                preferences.flush { this["buy_amount"] = buyAmount }
                updateModelCost()
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

    private fun getResourceEntityByName(name : String) : Entity? {
        resourceEntities.forEach { entity ->
            if (resourceComponents[entity].name == name) {
                return entity
            }
        }
        return null
    }

    /**
     * Update the Model value for:
     * goldCoins given the purchased ResourceComponent
     */
    private fun updateGoldCoins(rscComp : ResourceComponent) {
        goldCoins -= calculateCost(rscComp)
        preferences.flush { this["gold_coins"] = goldCoins.toString() }
    }

    /**
     * Update the Model values for:
     * amount of a given ResourceComponent
     */
    private fun updateModelAmount(rscComp: ResourceComponent) {
        val bigBuyAmount = buyAmount.toBigDecimal()
        ownedProps[rscComp.name]?.let { prop ->
            prop.set(prop.get() + bigBuyAmount)
            preferences.flush { this["${rscComp.name}_owned"] = prop.get().toString() }
        }
    }

    /**
     * Update the ResourceComponent value for:
     * amount of a given ResourceComponent
     */
    private fun updateResourceComponent(rscComp : ResourceComponent) {
        rscComp.amountOwned += buyAmount.toBigDecimal()
    }

    private fun updateUpgradeComponents(amount : BigDecimal) {
        upgradeEntities.forEach { entity ->
            val upgComp = upgradeComponents[entity]
            upgComp.soilUpgrades += amount
            soilCost = upgComp.cost
        }
    }

    /**
     * Update the Model values for:
     * cost for a given ResourceComponent
     */
    private fun updateModelCost(rscComp : ResourceComponent) {
        costProps[rscComp.name]?.let { prop ->
            prop.set(rscComp.cost)
            preferences.flush { this["${rscComp.name}_cost"] = prop.get().toString() }
        }
    }

    /**
     * Update the Model values for:
     * value for a given ResourceComponent
     */
    private fun updateModelValue(rscComp : ResourceComponent) {
        valueProps[rscComp.name]?.set(rscComp.value)
    }

    /**
     * Update the Model values for:
     * rate for a given ResourceComponent
     */
    private fun updateModelRate(rscComp : ResourceComponent) {
        rateProps[rscComp.name]?.set(rscComp.rate)
    }

    /**
     * Update the Model values for:
     * productionRate
     */
    private fun updateModelProductionRate() {
        var productionRate = BigDecimal(0)
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            if (rscComp.name == ScoreResources.GOLD_COINS.resourceName) return@forEach
            productionRate += (rscComp.value * rscComp.rate)
        }
        this@PlanetModel.productionRate = productionRate
        preferences.flush { this["production_rate"] = this@PlanetModel.productionRate.toString() }
    }

    /**
     * Update the Model values for:
     * cost for each ResourceComponent
     */
    private fun updateModelCost() {
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            updateModelCost(rscComp)
        }
    }

    /**
     * Update the Model values for:
     * cost, value, and rate of a given ResourceComponent
     */
    private fun updateModel(rscComp : ResourceComponent) {
        updateModelCost(rscComp)
        updateModelValue(rscComp)
        updateModelRate(rscComp)
    }

    /**
     * @param ResourceComponent
     * Calculates the cost of a resource relative to the buyAmount and accounts
     * for the price increase per bought item.
     * @return BigDecimal
     */
    private fun calculateCost(rscComp: ResourceComponent) : BigDecimal {
        // do calculations based on buy amount
        return rscComp.cost
    }

    /**
     * Get the Value Multiplier based on achievements, soil upgrades, etc.
     * @return BigDecimal
     */
    private fun calculateValueMultiplier() : BigDecimal {
        var valueMultiplier = BigDecimal(1)
        valueMultiplier = valueMultiplier * calculateAchievementMultiplier()

        return valueMultiplier
    }

    /**
     * Get the Achievement Multiplier
     * @return BigDecimal
     */
    private fun calculateAchievementMultiplier() : BigDecimal {
        var achievementMultiplier = BigDecimal(1)

        achievementEntities.forEach { entity ->
            achievementMultiplier = achievementMultiplier * achievementComponents[entity].achMultiplier
        }
        return achievementMultiplier
    }

    /**
     * Reset values from upgrading Soil.
     */
    private fun resetValuesFromSoilUpgrade() {
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            rscComp.amountOwned = BigDecimal("0")
            rscComp.amountSold = BigDecimal("0")
            rscComp.currentTicks = 0
        }

        PlanetResources.entries.forEach { resource ->
            val entity = getResourceEntityByName(resource.resourceName)
                ?: gdxError("No Entity with type: ${resource.resourceName}")
            val rscComp = resourceComponents[entity]
            ownedProps[resource.resourceName]?.set(BigDecimal("0"))
            updateModel(rscComp)
        }

        goldCoins = BigDecimal("5")
        productionRate = BigDecimal("0")
    }

    /**
     * Reset all game values. Used for debugging and testing.
     */
    private fun resetGameValues() {
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            rscComp.amountOwned = BigDecimal("0")
        }

        goldCoins = BigDecimal("5")
        productionRate = BigDecimal("0")
        buyAmount = 1f

        redOwned = BigDecimal("0")
        redCost = BigDecimal("1")
        redValue = BigDecimal("0")
        redValueIncrease = BigDecimal("0.31")
        redRate = BigDecimal("1.3")
        redRateIncrease = BigDecimal("0.12")

        gameCompleted = false

        preferences.flush {
            preferences.clear()
        }
    }

    companion object {
        private val log = logger<PlanetModel>()
    }
}
