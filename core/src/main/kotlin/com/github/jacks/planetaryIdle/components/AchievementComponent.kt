package com.github.jacks.planetaryIdle.components

import kotlin.math.pow

enum class Achievements(
    val achievementId : Int,
    val achievementName : String,
    val achievementDescription : String,
    val bonusDescription : String = ""
) {
    ACH_1(0, "name1", "buy wheat"),
    ACH_2(1, "name2", "buy corn");
}

data class AchievementComponent(
    var count : Int = 0,
    var multiplierScale : Float = 1.25f,
    var completedAchievements : MutableList<Achievements> = mutableListOf<Achievements>()
) {

    val multiplier : Float
        get() {
            return if (count == 0) {
                0f
            } else {
                multiplierScale.pow(completedAchievements.count())
            }
        }
}
