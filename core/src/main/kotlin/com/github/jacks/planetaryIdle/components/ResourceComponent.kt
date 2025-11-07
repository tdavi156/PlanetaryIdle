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
    val baseValue : String,
    val valueScaling : String,
    val baseRate : String,
    val rateScaling : String
) {
    RED("red","1","0.31","10.04","1.3","0.17"),
    ORANGE("orange","100","2.4","0.09","0.95","0.11"),
    YELLOW("yellow","1000","18.9","0.23","0.67","0.07"),
    GREEN("green","50000","147.1","3.21","0.43","0.06"),
    BLUE("blue","1000000","1147","12.5","0.21","0.05"),
    PURPLE("purple","500000000","8952","46.3","0.12","0.04"),
    PINK("pink","10000000000","69811","374.8","0.08","0.03"),
    BROWN("brown","100000000000000","544532","2567","0.05","0.02"),
    WHITE("white","1000000000000000000","4247354","74502","0.03","0.01"),
    BLACK("black","1000000000000000000000000","33129365","3312936","0.01","0.005");
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
        get() {
            return if (amountOwned == ZERO) {
                ZERO
            } else if (amountOwned == ONE) {
                baseValue
            } else {
                baseValue + (valueScaling * amountSold)
            }
        }

    val rate : BigDecimal
        // might add additional scaling here later to improve the rate based on amount owned (scaling * owned)
        get() = baseRate

    val tickCount : Int
        get() {
            // if the rate is greater then the frame cap, increase the value proportionally based on the exceeding rate
            return if (rate.toInt() >= FRAMES_PER_SECOND_INT) { 1 }
            else {
                (ONE.divide((rate / BigDecimal(FRAMES_PER_SECOND_INT)), 2, RoundingMode.UP)).toInt()
            }
        }

    companion object {
        val ZERO = BigDecimal(0)
        val ONE = BigDecimal(1)
    }
}
