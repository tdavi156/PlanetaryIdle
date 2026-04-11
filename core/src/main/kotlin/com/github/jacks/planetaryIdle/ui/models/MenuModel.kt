package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.events.AutomationUnlockedEvent
import com.github.jacks.planetaryIdle.events.BarnUnlockedEvent
import com.github.jacks.planetaryIdle.events.KitchenUnlockedEvent
import com.github.jacks.planetaryIdle.events.ObservatoryUnlockedEvent
import com.github.jacks.planetaryIdle.events.ResearchExhaustedEvent
import com.github.jacks.planetaryIdle.events.ViewStateChangeEvent
import com.github.jacks.planetaryIdle.ui.ViewState
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set

class MenuModel(
    stage: Stage
) : PropertyChangeSource(), EventListener {

    private val preferences: Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }

    var barnUnlocked        by propertyNotify(preferences["barn_unlocked",         false])
    var kitchenUnlocked     by propertyNotify(preferences["kitchen_unlocked",      false])
    var observatoryUnlocked by propertyNotify(preferences["observatory_unlocked",  false])
    var automationUnlocked  by propertyNotify(preferences[AutomationModel.PREF_UNLOCKED, false])

    /** True when any auto-researcher has exhausted all possible discoveries. Drives the Kitchen badge dot. */
    var kitchenHasExhausted by propertyNotify(false)

    init {
        stage.addListener(this)
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is BarnUnlockedEvent -> {
                barnUnlocked = true
                return false  // let other listeners receive it too
            }
            is KitchenUnlockedEvent -> {
                if (!kitchenUnlocked) {
                    kitchenUnlocked = true
                    preferences.flush { this["kitchen_unlocked"] = true }
                }
                return false
            }
            is ObservatoryUnlockedEvent -> {
                if (!observatoryUnlocked) {
                    observatoryUnlocked = true
                }
                return false
            }
            is AutomationUnlockedEvent -> {
                if (!automationUnlocked) {
                    automationUnlocked = true
                }
                return false
            }
            is ResearchExhaustedEvent -> {
                kitchenHasExhausted = true
                return false
            }
            is ViewStateChangeEvent -> {
                // Clear the kitchen badge when the player opens the Kitchen view
                if (event.state == ViewState.KITCHEN && kitchenHasExhausted) {
                    kitchenHasExhausted = false
                }
                return false
            }
            else -> return false
        }
    }

    companion object {
        private val log = logger<MenuModel>()
    }
}
