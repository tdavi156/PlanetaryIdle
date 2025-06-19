package com.github.jacks.planetaryIdle.events

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage

fun Stage.fire(event : Event) {
    this.root.fire(event)
}

class AssignPopulationToFarm : Event()

class FoodGrowthEvent : Event()

class ConvertFoodToPopulation : Event()

