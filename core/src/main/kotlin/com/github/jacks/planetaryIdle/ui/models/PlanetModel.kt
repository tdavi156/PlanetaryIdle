package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.AchievementComponent
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.components.UpgradeComponent
import com.github.jacks.planetaryIdle.events.BuyResourceEvent
import com.github.jacks.planetaryIdle.events.GameCompletedEvent
import com.github.jacks.planetaryIdle.events.ResetGameEvent
import com.github.jacks.planetaryIdle.events.ResourceUpdateEvent
import com.github.jacks.planetaryIdle.events.SaveGameEvent
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
    //private val upgradeComponents : ComponentMapper<UpgradeComponent> = world.mapper()
    //private val achievementComponents : ComponentMapper<AchievementComponent> = world.mapper()
    private val resourceEntities = world.family(allOf = arrayOf(ResourceComponent::class))
    //private val multiplierEntity = world.family(allOf = arrayOf(UpgradeComponent::class, AchievementComponent::class)).first()

    var goldCoins by propertyNotify(BigDecimal(preferences["gold_coins", "5"]))
    var productionRate by propertyNotify(BigDecimal(preferences["production_rate", "0"]))
    var buyAmount by propertyNotify(preferences["buy_amount", 1f])

    // var soilUpgrades by propertyNotify(preferences["soilUpgrades", 0])

    var redOwned by propertyNotify(BigDecimal(preferences["red_owned", "0"]))
    var redCost by propertyNotify(BigDecimal(preferences["red_cost", "1"]))
    var redValue by propertyNotify(BigDecimal(preferences["red_value", "0"]))
    var redValueIncrease by propertyNotify(BigDecimal(preferences["red_value_increase", "0.31"]))
    var redRate by propertyNotify(BigDecimal(preferences["red_rate", "1.3"]))
    var redRateIncrease by propertyNotify(BigDecimal(preferences["red_rate_increase", "0.12"]))

    var gameCompleted by propertyNotify(false)

    init {
        stage.addListener(this)
    }

    override fun handle(event: Event?): Boolean {
        when (event) {
            is BuyResourceEvent -> {
                val entity = getResourceEntityByName(event.resourceType) ?: gdxError("No Entity with type: ${event.resourceType}")
                val rscComp = resourceComponents[entity]
                updateGoldCoins(rscComp)
                updateModelAmount(rscComp)
                updateResourceComponent(rscComp)
                updateModel(rscComp)
                updateModelProductionRate()
            }
            is ResourceUpdateEvent -> {
                val amount = event.amount
                goldCoins += amount
                preferences.flush { this["gold_coins"] = goldCoins.toString() }
            }
            is UpgradeSoilEvent -> {
                //soilUpgrades += event.amount
                //upgradeComponents[multiplierEntity].soilUpgrades += event.amount
                // reset all crops amounts
                // reset AP
                // reset total AP

            }
            is UpdateBuyAmountEvent -> {
                buyAmount = event.amount
                preferences.flush { this["buy_amount"] = buyAmount }
                updateModel()
            }
            is SaveGameEvent -> {

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
     * Update the ResourceComponent value for:
     * amount of a given ResourceComponent
     */
    private fun updateResourceComponent(rscComp : ResourceComponent) {
        rscComp.amountOwned += buyAmount.toBigDecimal()
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
        when (rscComp.name) {
            "red" -> {
                redOwned += bigBuyAmount
                preferences.flush { this["red_owned"] = redOwned.toString() }
            }
        }
    }

    /**
     * Update the Model values for:
     * productionRate
     */
    private fun updateModelProductionRate() {
        var productionGain = BigDecimal(0)
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            if (rscComp.name == "gold_coins") return@forEach
            productionGain += (rscComp.baseValue * rscComp.amountOwned)
        }
        productionRate = productionGain
        preferences.flush { this["production_rate"] = productionRate.toString() }
    }

    /**
     * Update the Model values for:
     * cost for each ResourceComponent
     */
    private fun updateModel() {
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            updateModel(rscComp)
        }
    }

    /**
     * Update the Model values for:
     * cost for a given ResourceComponent
     */
    private fun updateModel(rscComp : ResourceComponent) {
        when (rscComp.name) {
            "red" -> {
                redCost = rscComp.cost
                redValueIncrease = rscComp.value
                redRate = rscComp.rate
                preferences.flush {
                    this["red_cost"] = redCost.toString()
                }
            }
        }
    }

    /**
     * @param ResourceComponent
     * Calculates the cost of a resource relative to the buyAmount and accounts
     * for the price increase per bought item.
     * @return Float
     */
    private fun calculateCost(rscComp: ResourceComponent) : BigDecimal {
        // do calculations based on buy amount
        return rscComp.cost
    }

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
