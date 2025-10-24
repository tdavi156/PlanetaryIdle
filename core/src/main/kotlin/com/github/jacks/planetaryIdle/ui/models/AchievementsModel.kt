package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.AchievementComponent
import com.github.jacks.planetaryIdle.events.AchievementCompletedEvent
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set

class AchievementsModel(
    world : World,
    stage : Stage
) : PropertyChangeSource(), EventListener {

    private val preferences : Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }
    private val achievementComponents : ComponentMapper<AchievementComponent> = world.mapper()
    private val achievementEntities = world.family(allOf = arrayOf(AchievementComponent::class))

    var achCount by propertyNotify(preferences["achCount", 0])

    var ach1 by propertyNotify(preferences["ach1", false])
    var ach2 by propertyNotify(preferences["ach2", false])
    var ach3 by propertyNotify(preferences["ach3", false])

    init {
        stage.addListener(this)
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is AchievementCompletedEvent -> {
                val achId = event.achId
                achievementEntities.forEach { achievement ->
                    if (achId == -1) return false
                    if (!achievementComponents[achievement].completedAchievements.contains(achId)) {
                        achievementComponents[achievement].completedAchievements.add(achId)
                        achCount = achievementComponents[achievement].completedAchievements.count()
                        updateAchievement("ach$achId")
                        preferences.flush { this["ach$achId"] = true }
                    }
                }
            }
            else -> return false
        }
        return true
    }

    private fun updateAchievement(id : String) {
        when(id) {
            "ach1" -> { ach1 = true }
            "ach2" -> { ach2 = true }
            "ach3" -> { ach3 = true }
        }
    }
}
