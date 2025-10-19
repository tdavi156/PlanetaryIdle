package com.github.jacks.planetaryIdle.components

import java.math.BigDecimal

enum class AchievementType(
    val typeName : String
) {
    BASIC_ACHIEVEMENT("basic_achievement")
}

enum class Achievements(
    val achievementId : Int,
    val achievementName : String,
    val achievementDescription : String,
    val bonusDescription : String = ""
) {
    ACH_1(0, "Red 1", "Buy 1 red resource."),
    ACH_2(1, "Orange 1", "Buy 1 orange resource."),
    ACH_3(2, "Yellow 1", "Buy 1 yellow resource.");
}

data class AchievementConfiguration(
    val name : String = "",
    val ach1 : Boolean = false,
    val ach2 : Boolean = false,
    val ach3 : Boolean = false
)

data class AchievementComponent(
    var multiplierScale : BigDecimal = BigDecimal(1.05),
    var completedAchievements : MutableList<Achievements> = mutableListOf<Achievements>()
) {

    val achMultiplier : BigDecimal
        get() {
            return if (completedAchievements.isEmpty()) {
                BigDecimal(1)
            } else {
                multiplierScale.pow(completedAchievements.count())
            }
        }
}
