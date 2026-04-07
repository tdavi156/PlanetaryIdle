package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.events.BarnUnlockedEvent
import com.github.jacks.planetaryIdle.events.KitchenUnlockedEvent
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set

class MenuModel(
    stage: Stage
) : PropertyChangeSource(), EventListener {

    private val preferences: Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }

    var barnUnlocked    by propertyNotify(preferences["barn_unlocked",    false])
    var kitchenUnlocked by propertyNotify(preferences["kitchen_unlocked", false])

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
            else -> return false
        }
    }

    companion object {
        private val log = logger<MenuModel>()
    }
}
