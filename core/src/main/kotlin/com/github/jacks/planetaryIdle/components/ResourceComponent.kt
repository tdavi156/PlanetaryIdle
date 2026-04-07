package com.github.jacks.planetaryIdle.components

import com.github.jacks.planetaryIdle.PlanetaryIdle.Companion.FRAMES_PER_SECOND_INT
import java.math.*

enum class ScoreResources(
    val resourceName : String
) {
    GOLD_COINS("gold_coins")
}

enum class PlanetResources(
    val resourceName : String,
    val baseCost : String,
    val basePayout : String,
    val cycleDuration : String
) {
    RED("red",       "1",                        "5",         "1.5"),
    ORANGE("orange", "100",                       "25",        "4.5"),
    YELLOW("yellow", "1000",                      "125",       "13.5"),
    GREEN("green",   "50000",                     "625",       "40.5"),
    BLUE("blue",     "1000000",                   "3125",      "121.5"),
    PURPLE("purple", "500000000",                 "15625",     "364.5"),
    PINK("pink",     "10000000000",               "78125",     "1093.5"),
    BROWN("brown",   "100000000000000",           "390625",    "3280.5"),
    WHITE("white",   "1000000000000000000",       "1953125",   "9841.5"),
    BLACK("black",   "1000000000000000000000000", "9765625",   "29524.5");
}

data class ResourceConfiguration(
    val name: String = "",
    val amountOwned: BigDecimal = BigDecimal("0"),
    val baseCost: BigDecimal = BigDecimal("0"),
    val costScaling: BigDecimal = BigDecimal("0"),
    val basePayout: BigDecimal = BigDecimal("0"),
    val cycleDuration: BigDecimal = BigDecimal("0"),
    val currentTicks : Int = 0,
    val isUnlocked: Boolean = false
)

data class ResourceComponent(
    var name : String = "",
    var amountOwned: BigDecimal = BigDecimal("0"),
    var baseCost: BigDecimal = BigDecimal("0"),
    var costScaling: BigDecimal = BigDecimal("0"),
    var basePayout: BigDecimal = BigDecimal("0"),
    var cycleDuration: BigDecimal = BigDecimal("0"),
    var currentTicks : Int = 0,
    var isUnlocked : Boolean = false
) {

    val cost : BigDecimal
        get() = baseCost.multiply((BigDecimal.ONE + costScaling).pow(amountOwned.toInt()))

    val milestoneMultiplier : BigDecimal
        get() {
            var mult = BigDecimal.ONE
            val owned = amountOwned.toInt()
            if (owned >= 10)  mult = mult.multiply(BigDecimal("1.2"))
            if (owned >= 25)  mult = mult.multiply(BigDecimal("1.5"))
            if (owned >= 50)  mult = mult.multiply(BigDecimal("2.0"))
            if (owned >= 100) mult = mult.multiply(BigDecimal("3.0"))
            return mult
        }

    val payout : BigDecimal
        get() {
            if (amountOwned <= BigDecimal.ZERO) return BigDecimal.ZERO
            val ownedScaled = Math.pow(amountOwned.toDouble(), 0.75).toBigDecimal()
            return basePayout.multiply(ownedScaled).multiply(milestoneMultiplier)
        }

    val tickCount : Int
        get() {
            if (cycleDuration <= BigDecimal.ZERO) return Int.MAX_VALUE
            return (cycleDuration.toFloat() * FRAMES_PER_SECOND_INT).toInt()
        }
}
