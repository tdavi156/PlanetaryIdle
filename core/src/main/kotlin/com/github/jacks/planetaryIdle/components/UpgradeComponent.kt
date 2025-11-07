package com.github.jacks.planetaryIdle.components

import com.github.jacks.planetaryIdle.PlanetaryIdle.Companion.MATH_CONTEXT
import java.math.BigDecimal

enum class UpgradeType(
    val typeName : String
) {
    SOIL_UPGRADE("soil_upgrade")
}

data class UpgradeConfiguration(
    val name: String = "",
    val isUnlocked: Boolean = false,
    var soilUpgrades : BigDecimal = BigDecimal(0),
)

data class UpgradeComponent(
    var isUnlocked : Boolean = false,
    var soilUpgradeBaseCost : BigDecimal = BigDecimal(1000000),
    var soilUpgrades : BigDecimal = BigDecimal(0),
    var soilSpeedMultiplier : BigDecimal = BigDecimal(2),
) {

    val cost : BigDecimal
        get() = soilUpgradeBaseCost.multiply(BigDecimal(10).pow(soilUpgrades.toInt(), MATH_CONTEXT))

    val multiplier : BigDecimal
        get() {
            return if (soilUpgrades <= BigDecimal(0)) {
                BigDecimal(1)
            } else {
                soilSpeedMultiplier.pow(soilUpgrades.toInt())
            }
        }
}
