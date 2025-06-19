package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.events.AssignPopulationToFarm
import com.github.jacks.planetaryIdle.events.ConvertFoodToPopulation
import com.github.jacks.planetaryIdle.events.FoodGrowthEvent
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World

class PlanetModel(
    world : World,
    stage : Stage
) : PropertyChangeSource(), EventListener {

    private val resourceComponent : ComponentMapper<ResourceComponent> = world.mapper()

    var populationAmount by propertyNotify(10)
    var foodAmount by propertyNotify(0)

    init {
        stage.addListener(this)
    }

    override fun handle(event: Event?): Boolean {
        when (event) {
            is AssignPopulationToFarm -> {

            }
            is FoodGrowthEvent -> {
                foodAmount++
            }
            is ConvertFoodToPopulation -> {

            }
            else -> return false
        }
        return true
    }

}
