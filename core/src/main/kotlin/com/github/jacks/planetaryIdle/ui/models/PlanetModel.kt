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
    var cornAmount by propertyNotify(0)
    var cabbageAmount by propertyNotify(0)
    var potatoesAmount by propertyNotify(0)
    var gameCompleted by propertyNotify(false)

    init {
        stage.addListener(this)
    }

    override fun handle(event: Event?): Boolean {
        when (event) {
            is AssignPopEvent -> {
                val entity = getEntityByName(event.foodType) ?: gdxError("No Entity with foodType: ${event.foodType}")
                val rscComp = resourceComponents[entity]
                updateAmountOwned(event.foodType)
                rscComp.amountOwned++
                availablePopulationAmount -= rscComp.resourceCost.roundToInt()
                populationGainPerSecond = getPopulationGain()
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
                cornAmount = 0
                cabbageAmount = 0
                potatoesAmount = 0
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

    private fun updateAmountOwned(foodType : String) {
        when (foodType) {
            "wheat" -> {
                wheatAmount++
            }
            "corn" -> {
                cornAmount++
            }
            "cabbage" -> {
                cabbageAmount++
            }
            "potatoes" -> {
                potatoesAmount++
            }
        }
    }

    private fun getPopulationGain() : Float {
        var popGain = 0f
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            popGain += (rscComp.resourceValue / rscComp.baseUpdateDuration * rscComp.amountOwned)
        }
        return popGain
    }
}
