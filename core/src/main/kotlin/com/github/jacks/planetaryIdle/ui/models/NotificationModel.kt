package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.AchievementComponent
import com.github.jacks.planetaryIdle.components.Achievements
import com.github.jacks.planetaryIdle.events.AchievementNotificationEvent
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World

class NotificationModel(
    world : World,
    stage : Stage
) : PropertyChangeSource(), EventListener {

    private val preferences : Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }
    private val achievementComponents : ComponentMapper<AchievementComponent> = world.mapper()
    private val achievementEntities = world.family(allOf = arrayOf(AchievementComponent::class))

    // dummy variable to trigger the notification
    var achId by propertyNotify(-1)

    init {
        stage.addListener(this)
    }

    override fun handle(event : Event): Boolean {
        when(event) {
            is AchievementNotificationEvent -> {
                achievementEntities.forEach { achievement ->
                    if (!achievementComponents[achievement].completedAchievements.contains(event.achId)) {
                        achId = event.achId
                    }
                }
            }
            else -> return false
        }
        return true
    }
}
