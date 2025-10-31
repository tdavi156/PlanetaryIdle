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
    var ach4 by propertyNotify(preferences["ach4", false])
    var ach5 by propertyNotify(preferences["ach5", false])
    var ach6 by propertyNotify(preferences["ach6", false])
    var ach7 by propertyNotify(preferences["ach7", false])
    var ach8 by propertyNotify(preferences["ach8", false])
    var ach9 by propertyNotify(preferences["ach9", false])
    var ach10 by propertyNotify(preferences["ach10", false])
    var ach11 by propertyNotify(preferences["ach11", false])
    var ach12 by propertyNotify(preferences["ach12", false])
    var ach13 by propertyNotify(preferences["ach13", false])
    var ach14 by propertyNotify(preferences["ach14", false])
    var ach15 by propertyNotify(preferences["ach15", false])
    var ach16 by propertyNotify(preferences["ach16", false])
    var ach17 by propertyNotify(preferences["ach17", false])
    var ach18 by propertyNotify(preferences["ach18", false])
    var ach19 by propertyNotify(preferences["ach19", false])
    var ach20 by propertyNotify(preferences["ach20", false])
    var ach21 by propertyNotify(preferences["ach21", false])
    var ach22 by propertyNotify(preferences["ach22", false])

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
            "ach4" -> { ach4 = true }
            "ach5" -> { ach5 = true }
            "ach6" -> { ach6 = true }
            "ach7" -> { ach7 = true }
            "ach8" -> { ach8 = true }
            "ach9" -> { ach9 = true }
            "ach10" -> { ach10 = true }
            "ach11" -> { ach11 = true }
            "ach12" -> { ach12 = true }
            "ach13" -> { ach13 = true }
            "ach14" -> { ach14 = true }
            "ach15" -> { ach15 = true }
            "ach16" -> { ach16 = true }
            "ach17" -> { ach17 = true }
            "ach18" -> { ach18 = true }
            "ach19" -> { ach19 = true }
            "ach20" -> { ach20 = true }
            "ach21" -> { ach21 = true }
            "ach22" -> { ach22 = true }
        }
    }
}
