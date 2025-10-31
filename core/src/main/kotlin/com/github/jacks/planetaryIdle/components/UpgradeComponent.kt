package com.github.jacks.planetaryIdle.components

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
    var soilUpgrades : BigDecimal = BigDecimal(0),
    var soilSpeedMultiplier : BigDecimal = BigDecimal(2),
) {

    val multiplier : BigDecimal
        get() {
            return if (soilUpgrades <= BigDecimal(0)) {
                BigDecimal(1)
            } else {
                soilSpeedMultiplier.pow(soilUpgrades.toInt())
            }
        }
}
