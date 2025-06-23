package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.events.AssignPopEvent
import com.github.jacks.planetaryIdle.events.GameCompletedEvent
import com.github.jacks.planetaryIdle.events.ResetGameEvent
import com.github.jacks.planetaryIdle.events.ResourceUpdateEvent
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

    var wheatAmount by propertyNotify(0)
    var wheatMultiplier by propertyNotify(1f)
    var wheatCost by propertyNotify(10f)
    var cornAmount by propertyNotify(0)
    var cornMultiplier by propertyNotify(1f)
    var cornCost by propertyNotify(100f)
    var cabbageAmount by propertyNotify(0)
    var cabbageMultiplier by propertyNotify(1f)
    var cabbageCost by propertyNotify(1000f)
    var potatoesAmount by propertyNotify(0)
    var potatoesMultiplier by propertyNotify(1f)
    var potatoesCost by propertyNotify(10000f)

    var gameCompleted by propertyNotify(false)

    init {
        stage.addListener(this)
    }

    override fun handle(event: Event?): Boolean {
        when (event) {
            is AssignPopEvent -> {
                val entity = getEntityByName(event.foodType) ?: gdxError("No Entity with foodType: ${event.foodType}")
                val rscComp = resourceComponents[entity]
                updateResourceComponent(rscComp)
                updateModel(rscComp)
            }
            is ResourceUpdateEvent -> {
                val rscComp = resourceComponents[event.entity]
                availablePopulationAmount += (rscComp.resourceValue.roundToInt() * rscComp.amountOwned)
                totalPopulationAmount = (totalPopulationAmount + (rscComp.resourceValue.roundToInt() * rscComp.amountOwned)).coerceAtMost(1000000)
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
            if (resourceComponents[entity].resourceName == name) {
                return entity
            }
        }
        return null
    }

    private fun updateResourceComponent(rscComp : ResourceComponent) {
        rscComp.amountOwned++
    }

    private fun updateModel(rscComp : ResourceComponent) {
        availablePopulationAmount -= rscComp.cost.roundToInt()
        populationGainPerSecond = getPopulationGain()
        when (rscComp.resourceName) {
            "wheat" -> {
                wheatAmount++
                wheatMultiplier = rscComp.multiplier
                wheatCost = rscComp.cost
            }
            "corn" -> {
                cornAmount++
                cornMultiplier = rscComp.multiplier
                cornCost = rscComp.cost
            }
            "cabbage" -> {
                cabbageAmount++
                cabbageMultiplier = rscComp.multiplier
                cabbageCost = rscComp.cost
            }
            "potatoes" -> {
                potatoesAmount++
                potatoesMultiplier = rscComp.multiplier
                potatoesCost = rscComp.cost
            }
        }
    }

    private fun getPopulationGain() : Float {
        var popGain = 0f
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            popGain += ((rscComp.resourceValue * rscComp.multiplier) / rscComp.baseUpdateDuration * rscComp.amountOwned)
        }
        return popGain
    }
}
