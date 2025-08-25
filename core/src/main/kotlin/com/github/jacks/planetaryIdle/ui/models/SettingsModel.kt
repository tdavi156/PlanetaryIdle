package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.World

class SettingsModel(
    world : World,
    stage : Stage
) : PropertyChangeSource(), EventListener {

    init {
        stage.addListener(this)
    }

    override fun handle(event: Event): Boolean {
        return true
    }
}
