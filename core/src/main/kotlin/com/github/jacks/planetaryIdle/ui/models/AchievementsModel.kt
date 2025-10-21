package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.AchievementComponent
import com.github.jacks.planetaryIdle.events.AchievementEvent
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import ktx.preferences.flush
import ktx.preferences.set

class AchievementsModel(
    world : World,
    stage : Stage
) : PropertyChangeSource(), EventListener {

    private val preferences : Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }
    private val achievementComponents : ComponentMapper<AchievementComponent> = world.mapper()
    private val achievementEntities = world.family(allOf = arrayOf(AchievementComponent::class))

    init {
        stage.addListener(this)
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is AchievementEvent -> {
                val achId = event.achId
                achievementEntities.forEach { achievement ->
                    if (!achievementComponents[achievement].completedAchievements.contains(achId)) {
                        achievementComponents[achievement].completedAchievements.add(achId)
                        preferences.flush { this["ach$achId"] = true }
                    }
                }
            }
            else -> return false
        }
        return true
    }
}
