package com.github.jacks.planetaryIdle.events

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.quillraven.fleks.Entity
import java.math.BigDecimal

fun Stage.fire(event : Event) {
    this.root.fire(event)
}

class InitializeGameEvent : Event()
class SaveGameEvent : Event()
class LoadGameEvent : Event()
class QuitGameEvent : Event()
class ResetGameEvent : Event()

class GameCompletedEvent : Event()

class ResourceUpdateEvent(val rscComp : ResourceComponent) : Event()
class BuyResourceEvent(val resourceType : String) : Event()
class UpdateBuyAmountEvent(val amount : Float) : Event()
class UpgradeSoilEvent(val amount : Int = 1) : Event()
