package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.configurations.Settings
import com.github.jacks.planetaryIdle.events.SettingsClosedEvent
import com.github.jacks.planetaryIdle.events.SettingsOpenEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.jacks.planetaryIdle.systems.AudioSystem
import com.github.jacks.planetaryIdle.systems.SettingsSystem
import ktx.preferences.flush
import ktx.preferences.set
import com.github.jacks.planetaryIdle.systems.InitializeGameSystem.Companion.preferences

class SettingsModel(
    private val stage: Stage,
    private val settingsSystem: SettingsSystem,
    private val audioSystem: AudioSystem,
) : PropertyChangeSource(), EventListener {

    var masterVolume  by propertyNotify(100)
    var musicVolume   by propertyNotify(100)
    var effectsVolume by propertyNotify(100)

    init {
        stage.addListener(this)
        loadFromPreferences()
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is SettingsOpenEvent -> {
                masterVolume  = settingsSystem.settings.masterVolume
                musicVolume   = settingsSystem.settings.musicVolume
                effectsVolume = settingsSystem.settings.effectsVolume
                return true
            }
        }
        return false
    }

    fun save() {
        settingsSystem.settings.masterVolume  = masterVolume
        settingsSystem.settings.musicVolume   = musicVolume
        settingsSystem.settings.effectsVolume = effectsVolume

        preferences.flush {
            this[Settings.KEY_MASTER_VOLUME]  = masterVolume
            this[Settings.KEY_MUSIC_VOLUME]   = musicVolume
            this[Settings.KEY_EFFECTS_VOLUME] = effectsVolume
        }

        audioSystem.applyMusicVolume()
        stage.fire(SettingsClosedEvent())
    }

    fun cancel() {
        stage.fire(SettingsClosedEvent())
    }

    private fun loadFromPreferences() {
        val master  = preferences.getInteger(Settings.KEY_MASTER_VOLUME,  100)
        val music   = preferences.getInteger(Settings.KEY_MUSIC_VOLUME,   100)
        val effects = preferences.getInteger(Settings.KEY_EFFECTS_VOLUME, 100)

        settingsSystem.settings.masterVolume  = master
        settingsSystem.settings.musicVolume   = music
        settingsSystem.settings.effectsVolume = effects

        masterVolume  = master
        musicVolume   = music
        effectsVolume = effects
    }
}
