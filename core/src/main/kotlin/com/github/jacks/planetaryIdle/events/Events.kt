package com.github.jacks.planetaryIdle.events

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.ui.ViewState
import java.math.BigDecimal

fun Stage.fire(event : Event) {
    this.root.fire(event)
}

class InitializeGameEvent : Event()
class SaveGameEvent : Event()
class QuitGameEvent : Event()
class ResetGameEvent : Event()

class GameCompletedEvent : Event()

class ResourceUpdateEvent(val rscComp : ResourceComponent) : Event()
class BuyResourceEvent(val resourceType : String) : Event()
class UpdateBuyAmountEvent(val amount : Float) : Event()
class UpgradeSoilEvent(val amount : BigDecimal = BigDecimal(1)) : Event()

class AchievementNotificationEvent(val achId : Int = -1) : Event()
class AchievementCompletedEvent(val achId : Int = -1) : Event()

class FloatingTextEvent(val startPosition: Vector2, val targetPosition: Vector2, val amount: BigDecimal, val displayText: String) : Event()
class CreditGoldEvent(val amount: BigDecimal) : Event()

class ViewStateChangeEvent(val state: ViewState) : Event()
