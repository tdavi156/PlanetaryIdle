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
    val tier: Int = 1,
    val baseCost: BigDecimal = BigDecimal("0"),
    val baseValue: BigDecimal = BigDecimal("0"),
    val amountOwned: BigInteger = BigInteger("0"),
    val isUnlocked: Boolean = false
)

data class ResourceComponent(
    var name : String = "",
    var tier : Int = 1,
    var baseCost: BigDecimal = BigDecimal("0"),
    var baseValue: BigDecimal = BigDecimal("0"),
    var amountOwned: BigInteger = BigInteger("0"),
    var isUnlocked : Boolean = false
) {

    private val numHundreds : Int
        get() = amountOwned.divide(BigInteger("100")).toInt()

    val multiplier : BigDecimal
        get() = BigDecimal("2").pow(numHundreds)

    val value : BigDecimal
        get() = baseValue * multiplier

    val cost : BigDecimal
        get() = baseCost * BigDecimal(10).pow(numHundreds * tier)

    val nextCost : BigDecimal
        get() = baseCost * BigDecimal(10).pow((numHundreds + 1) * tier)
}
