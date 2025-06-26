package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.events.BuyResourceEvent
import com.github.jacks.planetaryIdle.events.GameCompletedEvent
import com.github.jacks.planetaryIdle.events.ResetGameEvent
import com.github.jacks.planetaryIdle.events.ResourceUpdateEvent
import com.github.jacks.planetaryIdle.events.UpdateBuyAmountEvent
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import ktx.app.gdxError
import kotlin.math.roundToInt

class PlanetModel(
    world : World,
    stage : Stage
) : PropertyChangeSource(), EventListener {

    private val resourceComponents : ComponentMapper<ResourceComponent> = world.mapper()
    private val resourceEntities = world.family(allOf = arrayOf(ResourceComponent::class))

    var totalPopulationAmount by propertyNotify(10)
    var availablePopulationAmount by propertyNotify(10)
    var populationGainPerSecond by propertyNotify(0f)
    var buyAmount by propertyNotify(1f)

    var wheatAmount by propertyNotify(0)
    var wheatMultiplier by propertyNotify(1f)
    var wheatCost by propertyNotify(10f)
    var cornAmount by propertyNotify(0)
    var cornMultiplier by propertyNotify(1f)
    var cornCost by propertyNotify(100f)
    var cabbageAmount by propertyNotify(0)
    var cabbageMultiplier by propertyNotify(1f)
    var cabbageCost by propertyNotify(1_000f)
    var potatoesAmount by propertyNotify(0)
    var potatoesMultiplier by propertyNotify(1f)
    var potatoesCost by propertyNotify(10_000f)

    var gameCompleted by propertyNotify(false)

    init {
        stage.addListener(this)
    }

    override fun handle(event: Event?): Boolean {
        when (event) {
            is BuyResourceEvent -> {
                val entity = getEntityByName(event.resourceType) ?: gdxError("No Entity with foodType: ${event.resourceType}")
                val rscComp = resourceComponents[entity]
                updateResourceComponent(rscComp)
                updatePopulation(rscComp)
                updateModelAmount(rscComp)
                updateModel()
            }
            is ResourceUpdateEvent -> {
                val rscComp = resourceComponents[event.entity]
                availablePopulationAmount += (rscComp.baseValue.roundToInt() * rscComp.amountOwned)
                totalPopulationAmount = (totalPopulationAmount + (rscComp.baseValue.roundToInt() * rscComp.amountOwned)).coerceAtMost(1000000000)
            }
            is UpdateBuyAmountEvent -> {
                buyAmount = event.amount
                updateModel()
            }
            is GameCompletedEvent -> {
                gameCompleted = true
            }
            is ResetGameEvent -> {
                resourceEntities.forEach { entity ->
                    val rscComp = resourceComponents[entity]
                    rscComp.amountOwned = 0
                    rscComp.currentUpdateDuration = rscComp.baseUpdateDuration
                }
                totalPopulationAmount = 10
                availablePopulationAmount = 10
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
        availablePopulationAmount -= (rscComp.cost * buyAmount).roundToInt()
    }

    /**
     * Update the Model values for:
     * amount of a given ResourceComponent
     */
    private fun updateModelAmount(rscComp: ResourceComponent) {
        when (rscComp.name) {
            "wheat" -> wheatAmount += buyAmount.roundToInt()
            "corn" -> cornAmount += buyAmount.roundToInt()
            "cabbage" -> cabbageAmount += buyAmount.roundToInt()
            "potatoes" -> potatoesAmount += buyAmount.roundToInt()
        }
    }

    /**
     * Update the Model values for:
     * multiplier and cost for each ResourceComponent, and populationGainRate,
     */
    private fun updateModel() {
        populationGainPerSecond = getPopulationGainRate()
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            when (rscComp.name) {
                "wheat" -> {
                    wheatMultiplier = rscComp.multiplier
                    wheatCost = rscComp.cost * buyAmount
                }
                "corn" -> {
                    cornMultiplier = rscComp.multiplier
                    cornCost = rscComp.cost * buyAmount
                }
                "cabbage" -> {
                    cabbageMultiplier = rscComp.multiplier
                    cabbageCost = rscComp.cost * buyAmount
                }
                "potatoes" -> {
                    potatoesMultiplier = rscComp.multiplier
                    potatoesCost = rscComp.cost * buyAmount
                }
            }
        }
    }

    /*
    (cost*(buyAmount-current)) + (nextCost*current)
    (cost*(((((current / buyAmount).toInt())+1)*buyAmount-current))) + (nextCost*(current%buyAmount))
    current = 40, buyAmount = 100, cost = 10, nextCost = 50 -> (10*(100-40)) + (50 * 40) = 600 + 2000 = 2600
    current = 260, buyAmount = 100, cost = 250, nextCost = 1250 -> (250*(((((260 / 100).toInt())+1)*100-260))) + (1250*(260%100)) = 10,000 + 75,000 = 85,000
    current = 100, buyAmount = 100, cost = 50, next = 250 -> (50*(((((25/10



     */

    private fun calculateCost(rscComp: ResourceComponent) : Float {
        val cost = rscComp.cost
        val nextCost = rscComp.nextCost
        val amount = rscComp.amountOwned
        return (cost * (((((amount / buyAmount).toInt()) + 1) * buyAmount - amount))) + (nextCost * (amount % buyAmount))
    }

    private fun getPopulationGainRate() : Float {
        var popGain = 0f
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            popGain += ((rscComp.baseValue * rscComp.multiplier) / rscComp.baseUpdateDuration * rscComp.amountOwned)
        }
        return popGain
    }
}
