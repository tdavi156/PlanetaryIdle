package com.github.jacks.planetaryIdle.components

import java.math.*

enum class PlanetResources(
    val resourceName : String
) {
    WHEAT("wheat"),
    CORN("corn"),
    LETTUCE("lettuce"),
    CARROTS("carrots"),
    TOMATOES("tomatoes"),
    BROCCOLI("broccoli"),
    ONIONS("onions"),
    POTATOES("potatoes");
}

data class ResourceConfiguration(
    val name: String = "",
    val tier: Int = 0,
    val baseCost: BigDecimal = BigDecimal("0"),
    val baseValue: BigDecimal = BigDecimal("0"),
    val amountOwned: BigInteger = BigInteger("0"),
    val isUnlocked: Boolean = false
)

data class ResourceComponent(
    var name : String = "",
    var tier : Int = 0,
    var baseCost: BigDecimal = BigDecimal("0"),
    var baseValue: BigDecimal = BigDecimal("0"),
    var amountOwned: BigInteger = BigInteger("0"),
    var isUnlocked : Boolean = false
) {

    private val numTens : Int
        get() = amountOwned.divide(BigInteger("10")).toInt()

    val multiplier : BigDecimal
        get() = BigDecimal("2").pow(numTens)

    val value : BigDecimal
        get() = baseValue * multiplier

    val cost : BigDecimal
        get() = baseCost * BigDecimal(10).pow((numTens * (tier + 1)) + tier)

    val nextCost : BigDecimal
        get() = baseCost * BigDecimal(10).pow(((numTens + 1) * (tier + 1)) + tier)
}
