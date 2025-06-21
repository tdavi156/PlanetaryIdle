package com.github.jacks.planetaryIdle.events

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.Entity

fun Stage.fire(event : Event) {
    this.root.fire(event)
}

class InitializeGameEvent : Event()
class QuitGameEvent : Event()


class ResourceUpdateEvent(val entity : Entity) : Event()

class BuyFoodEvent(val foodType : String) : Event()
