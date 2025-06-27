package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.events.BuyResourceEvent
import com.github.jacks.planetaryIdle.events.GameCompletedEvent
import com.github.jacks.planetaryIdle.events.ResetGameEvent
import com.github.jacks.planetaryIdle.events.ResourceUpdateEvent
import com.github.jacks.planetaryIdle.events.SaveGameEvent
import com.github.jacks.planetaryIdle.events.UpdateBuyAmountEvent
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import ktx.app.gdxError
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import kotlin.math.roundToInt

class PlanetModel(
    world : World,
    stage : Stage
) : PropertyChangeSource(), EventListener {

    private val preferences : Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }
    private val resourceComponents : ComponentMapper<ResourceComponent> = world.mapper()
    private val resourceEntities = world.family(allOf = arrayOf(ResourceComponent::class))

    var totalPopulationAmount by propertyNotify(preferences["totalPopulation", 10])
    var availablePopulationAmount by propertyNotify(preferences["availablePopulation", 10f])
    var populationGainPerSecond by propertyNotify(preferences["populationGainRate", 0f])
    var buyAmount by propertyNotify(preferences["buyAmount", 1f])

    var wheatAmount by propertyNotify(preferences["wheat_amount", 0])
    var wheatMultiplier by propertyNotify(preferences["wheat_multiplier", 1f])
    var wheatCost by propertyNotify(preferences["wheat_cost", 10f])
    var cornAmount by propertyNotify(preferences["corn_amount", 0])
    var cornMultiplier by propertyNotify(preferences["corn_multiplier", 1f])
    var cornCost by propertyNotify(preferences["corn_cost", 100f])
    var cabbageAmount by propertyNotify(preferences["cabbage_amount", 0])
    var cabbageMultiplier by propertyNotify(preferences["cabbage_multiplier", 1f])
    var cabbageCost by propertyNotify(preferences["cabbage_cost", 1_000f])
    var potatoesAmount by propertyNotify(preferences["potatoes_amount", 0])
    var potatoesMultiplier by propertyNotify(preferences["potatoes_multiplier", 1f])
    var potatoesCost by propertyNotify(preferences["potatoes_cost", 10_000f])

    var gameCompleted by propertyNotify(false)

    init {
        stage.addListener(this)
    }

    override fun handle(event: Event?): Boolean {
        when (event) {
            is BuyResourceEvent -> {
                val entity = getEntityByName(event.resourceType) ?: gdxError("No Entity with foodType: ${event.resourceType}")
                val rscComp = resourceComponents[entity]
                updatePopulation(rscComp)
                updateModelAmount(rscComp)
                updateResourceComponent(rscComp)
                updateModel(rscComp)
                updateModelPopulationRate()
            }
            is ResourceUpdateEvent -> {
                val rscComp = resourceComponents[event.entity]
                availablePopulationAmount += rscComp.value * rscComp.amountOwned.toFloat()
                totalPopulationAmount = (totalPopulationAmount + (rscComp.baseValue.roundToInt() * rscComp.amountOwned)).coerceAtMost(1000000000)
                preferences.flush {
                    this["availablePopulation"] = availablePopulationAmount
                    this["totalPopulation"] = totalPopulationAmount
                }
            }
            is UpdateBuyAmountEvent -> {
                buyAmount = event.amount
                preferences.flush { this["buyAmount"] = buyAmount }
                updateModel()
            }
            is SaveGameEvent -> {

            }
            is ResetGameEvent -> {
                resourceEntities.forEach { entity ->
                    val rscComp = resourceComponents[entity]
                    rscComp.amountOwned = 0
                    rscComp.currentUpdateDuration = rscComp.baseUpdateDuration
                }
                totalPopulationAmount = 10
                availablePopulationAmount = 10f
                populationGainPerSecond = 0f
                buyAmount = 1f
                wheatAmount = 0
                wheatMultiplier = 1f
                wheatCost = 10f
                cornAmount = 0
                cornMultiplier = 1f
                cornCost = 100f
                cabbageAmount = 0
                cabbageMultiplier = 1f
                cabbageCost = 1000f
                potatoesAmount = 0
                potatoesMultiplier = 1f
                potatoesCost = 10000f
                gameCompleted = false
                preferences.flush {
                    preferences.clear()
                }
            }
            is GameCompletedEvent -> {
                gameCompleted = true
            }
            else -> return false
        }
        return true
    }

    private fun getEntityByName(name : String) : Entity? {
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
        rscComp.amountOwned += buyAmount.roundToInt()
    }

    /**
     * Update the Model value for:
     * availablePopulation given the purchased ResourceComponent
     */
    private fun updatePopulation(rscComp : ResourceComponent) {
        availablePopulationAmount -= calculateCost(rscComp)
        preferences.flush { this["availablePopulation"] = availablePopulationAmount }
    }

    /**
     * Update the Model values for:
     * amount of a given ResourceComponent
     */
    private fun updateModelAmount(rscComp: ResourceComponent) {
        when (rscComp.name) {
            "wheat" -> {
                wheatAmount += buyAmount.roundToInt()
                preferences.flush { this["wheat_amount"] = wheatAmount }
            }
            "corn" -> {
                cornAmount += buyAmount.roundToInt()
                preferences.flush { this["corn_amount"] = cornAmount }
            }
            "cabbage" -> {
                cabbageAmount += buyAmount.roundToInt()
                preferences.flush { this["cabbage_amount"] = cabbageAmount }
            }
            "potatoes" -> {
                potatoesAmount += buyAmount.roundToInt()
                preferences.flush { this["potatoes_amount"] = potatoesAmount }
            }
        }
    }

    /**
     * Update the Model values for:
     * populationGainRate
     */
    private fun updateModelPopulationRate() {
        var popGain = 0f
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            popGain += ((rscComp.baseValue * rscComp.multiplier) / rscComp.baseUpdateDuration * rscComp.amountOwned)
        }
        populationGainPerSecond = popGain
        preferences.flush { this["populationGainRate"] = populationGainPerSecond }
    }

    /**
     * Update the Model values for:
     * multiplier and cost for each ResourceComponent
     */
    private fun updateModel() {
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            updateModel(rscComp)
        }
    }

    /**
     * Update the Model values for:
     * multiplier and cost for each ResourceComponent
     */
    private fun updateModel(rscComp : ResourceComponent) {
        when (rscComp.name) {
            "wheat" -> {
                wheatMultiplier = rscComp.multiplier
                wheatCost = calculateCost(rscComp)
                preferences.flush {
                    this["wheat_multiplier"] = wheatMultiplier
                    this["wheat_cost"] = wheatCost
                }
            }
            "corn" -> {
                cornMultiplier = rscComp.multiplier
                cornCost = calculateCost(rscComp)
                preferences.flush {
                    this["corn_multiplier"] = cornMultiplier
                    this["corn_cost"] = cornCost
                }
            }
            "cabbage" -> {
                cabbageMultiplier = rscComp.multiplier
                cabbageCost = calculateCost(rscComp)
                preferences.flush {
                    this["cabbage_multiplier"] = cabbageMultiplier
                    this["cabbage_cost"] = cabbageCost
                }
            }
            "potatoes" -> {
                potatoesMultiplier = rscComp.multiplier
                potatoesCost = calculateCost(rscComp)
                preferences.flush {
                    this["potatoes_multiplier"] = potatoesMultiplier
                    this["potatoes_cost"] = potatoesCost
                }
            }
        }
    }

    /**
     * @param ResourceComponent
     * Calculates the cost of a resource relative to the buyAmount and accounts
     * for part of the purchase bridging the price increase threshold.
     * @return Float
     */
    private fun calculateCost(rscComp: ResourceComponent) : Float {
        val cost = rscComp.cost
        val nextCost = rscComp.nextCost
        val amount = rscComp.amountOwned
        return if (buyAmount == 100f || (buyAmount == 10f && amount % 100 > 90)) {
            (cost * (((((amount / 100) + 1) * 100) - amount))) + (nextCost * (amount % buyAmount))
        } else {
            cost * buyAmount
        }
    }
}
