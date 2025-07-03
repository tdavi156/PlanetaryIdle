package com.github.jacks.planetaryIdle.components

import kotlin.math.pow

data class UpgradeComponent(
    var soilUpgrades : Int = 0,
    var soilScaling : Float = 1.5f,
) {

    val multiplier : Float
        get() {
            return if (soilUpgrades == 0) {
                0f
            } else {
                soilScaling.pow(soilUpgrades)
            }
        }
}
