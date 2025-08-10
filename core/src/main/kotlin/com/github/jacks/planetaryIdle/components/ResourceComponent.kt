package com.github.jacks.planetaryIdle.components

import com.github.jacks.planetaryIdle.PlanetaryIdle.Companion.FRAMES_PER_SECOND_INT
import java.math.*

enum class ScoreResources(
    val resourceName : String
) {
    GOLD_COINS("gold_coins")
}

enum class PlanetResources(
    val resourceName : String
) {
    RED("red"),
    ORANGE("orange"),
    YELLOW("yellow"),
    GREEN("green"),
    BLUE("blue"),
    PURPLE("purple"),
    PINK("pink"),
    BROWN("brown"),
    WHITE("white"),
    BLACK("black");
}

data class ResourceConfiguration(
    val name: String = "",
    val amountOwned: BigDecimal = BigDecimal("0"),
    val baseCost: BigDecimal = BigDecimal("0"),
    val costScaling: BigDecimal = BigDecimal("0"),
    val baseValue: BigDecimal = BigDecimal("0"),
    val valueScaling: BigDecimal = BigDecimal("0"),
    val baseRate : BigDecimal = BigDecimal("0"),
    val rateScaling : BigDecimal = BigDecimal("0"),
    val currentTicks : Int = 0,
    val isUnlocked: Boolean = false
)

data class ResourceComponent(
    var name : String = "",
    var amountOwned: BigDecimal = BigDecimal("0"),
    var baseCost: BigDecimal = BigDecimal("0"),
    var costScaling: BigDecimal = BigDecimal("0"),
    var baseValue: BigDecimal = BigDecimal("0"),
    var valueScaling: BigDecimal = BigDecimal("0"),
    var baseRate : BigDecimal = BigDecimal("0"),
    var rateScaling : BigDecimal = BigDecimal("0"),
    var amountSold : BigDecimal = BigDecimal("0"),
    var currentTicks : Int = 0,
    var isUnlocked : Boolean = false
) {

    val cost : BigDecimal
        get() = baseCost.multiply((ONE + costScaling).pow(amountOwned.toInt()))

    val value : BigDecimal
        get() = baseValue + (valueScaling * amountSold)

    val rate : BigDecimal
        get() = baseRate + (rateScaling * amountOwned)

    val tickCount : Int
        get() {
            return if (rate.toInt() >= FRAMES_PER_SECOND_INT) { 1 }
            else { (ONE.divide((rate / BigDecimal(FRAMES_PER_SECOND_INT)), 0, RoundingMode.UP)).toInt() }
        }

    companion object {
        val ONE = BigDecimal("1")
    }
}
