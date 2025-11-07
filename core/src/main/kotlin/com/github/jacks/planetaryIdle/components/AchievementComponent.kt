package com.github.jacks.planetaryIdle.components

import java.math.BigDecimal

enum class AchievementType(
    val typeName : String
) {
    BASIC_ACHIEVEMENT("basic_achievement")
}

enum class Achievements(
    val achId : Int,
    val achName : String,
    val achDesc : String,
    val bonusDesc : String = ""
) {
    ACH_1(1, "Easy Start", "Own 5 of any Red resource"),
    ACH_2(2, "Orange 1", "Own 1 of any Orange resource"),
    ACH_3(3, "Yellow 1", "Own 1 of any Yellow resource"),
    ACH_4(4, "Green 1", "Own 1 of any Green resource"),
    ACH_5(5, "Making Progress", "Own 50 of any Red resource"),
    ACH_6(6, "Millionaire!", "Have 1,000,000 Gold Coins"),
    ACH_7(7, "Fresh Soil", "Have 1 Soil upgrade"),
    ACH_8(8, "Blue 1", "Own 1 of any Blue resource"),
    ACH_9(9, "Purple 1", "Own 1 of any Purple resource"),
    ACH_10(10, "Pink 1", "Own 1 of any Pink resource"),
    ACH_11(11, "It's a Rainbow!", "Own 10 of each of the first 7 resource colors"),
    ACH_12(12, "Excellent Soil", "Have 5 soil upgrades"),
    ACH_13(13, "Trillionaire!", "Have 1,000,000,000,000 Gold Coins"),
    ACH_14(14, "Brown 1", "Own 1 of any Brown resource"),
    ACH_15(15, "White 1", "Own 1 of any White resource"),
    ACH_16(16, "Exceptional Soil", "Have 10 Soil upgrades"),
    ACH_17(17, "Red Giant", "Own 250 of any Red resource"),
    ACH_18(18, "The Last Color", "Own 1 of any Black resource"),
    ACH_19(19, "Ten is a Team", "Own 10 of each color"),
    ACH_20(20, "Decillionaire!", "Have 1,000,000,000,000,000,000,000,000,000,000,000 Gold Coins"),
    ACH_21(21, "Perfect Soil", "Have 25 Soil upgrades", "Increase the effectiveness of Soil Upgrades from x2 to x2.5"),
    ACH_22(22, "The End", "Have 1e50 Gold Coins");
}

data class AchievementConfiguration(
    val name : String = "",
    val ach1 : Boolean = false,
    val ach2 : Boolean = false,
    val ach3 : Boolean = false,
    val ach4 : Boolean = false,
    val ach5 : Boolean = false,
    val ach6 : Boolean = false,
    val ach7 : Boolean = false,
    val ach8 : Boolean = false,
    val ach9 : Boolean = false,
    val ach10 : Boolean = false,
    val ach11 : Boolean = false,
    val ach12 : Boolean = false,
    val ach13 : Boolean = false,
    val ach14 : Boolean = false,
    val ach15 : Boolean = false,
    val ach16 : Boolean = false,
    val ach17 : Boolean = false,
    val ach18 : Boolean = false,
    val ach19 : Boolean = false,
    val ach20 : Boolean = false,
    val ach21 : Boolean = false,
    val ach22 : Boolean = false,
)

data class AchievementComponent(
    var multiplierScale : BigDecimal = BigDecimal(1.05),
    var completedAchievements : MutableList<Int> = mutableListOf<Int>()
) {

    val achMultiplier : BigDecimal
        get() {
            return if (completedAchievements.isEmpty()) {
                BigDecimal(1)
            } else if (completedAchievements.count() == 1) {
                BigDecimal(1.05)
            } else {
        multiplierScale.pow(completedAchievements.count())
    }
        }
}
