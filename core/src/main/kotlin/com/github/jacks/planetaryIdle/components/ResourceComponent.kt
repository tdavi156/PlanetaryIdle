package com.github.jacks.planetaryIdle.components

import kotlin.math.*

data class ResourceComponent(
    var resourceName : String = "",
    var resourceTier : Float = 1f,
    var baseResourceCost : Float = 0f,
    var baseUpdateDuration : Float = -10f,
    var currentUpdateDuration : Float = 0f,
    var resourceValue : Float = 0f,
    var amountOwned : Int = 0,
) {

    private val numHundreds : Float
        get() = (amountOwned / 100).toFloat()

    val multiplier : Float
        get() = 1.5f.pow(numHundreds)

    val cost : Float
        get() = baseResourceCost * (5 * resourceTier).pow(numHundreds)
}
