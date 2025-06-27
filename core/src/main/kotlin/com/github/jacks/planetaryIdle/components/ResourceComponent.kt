package com.github.jacks.planetaryIdle.components

import kotlin.math.*

enum class PlanetResources(
    val resourceName : String
) {
    WHEAT("wheat"),
    CORN("corn"),
    CABBAGE("cabbage"),
    POTATOES("potatoes");
}

data class ResourceConfiguration(
    val name : String = "",
    val tier : Float = 1f,
    val baseCost : Float = 0f,
    val baseValue : Float = 0f,
    val baseUpdateDuration : Float = -10f,
    val currentUpdateDuration : Float = 0f,
    val amountOwned : Int = 0,
    val isUnlocked : Boolean = false
)

data class ResourceComponent(
    var name : String = "",
    var tier : Float = 1f,
    var baseValue : Float = 0f,
    var baseCost : Float = 0f,
    var baseUpdateDuration : Float = -10f,
    var currentUpdateDuration : Float = 0f,
    var amountOwned : Int = 0,
    var isUnlocked : Boolean = false
) {

    private val numHundreds : Float
        get() = (amountOwned / 100).toFloat()

    val multiplier : Float
        get() = 1.5f.pow(numHundreds)

    val value : Float
        get() = baseValue * multiplier

    val cost : Float
        get() = baseCost * (5 * tier).pow(numHundreds)

    val nextCost : Float
        get() = baseCost * (5 * tier).pow(numHundreds + 1f)
}
