package com.github.jacks.planetaryIdle.components

import java.math.BigDecimal

data class Recipe(
    val crop1: CropType,
    val crop2: CropType,
) {
    /** Unique stable ID used for persistence. */
    val id: String get() = "${crop1.id}_x_${crop2.id}"

    val displayName: String get() = "${crop1.cropName} x ${crop2.cropName}"

    /** Combined cycle time in seconds (times are additive). */
    val combinedTime: Float get() = crop1.baseProductionTime + crop2.baseProductionTime

    /** Multiplied base value (values are multiplicative). */
    val combinedBaseValue: BigDecimal get() = crop1.baseValue.multiply(crop2.baseValue)

    companion object {
        /**
         * The first preset recipe automatically discovered on kitchen unlock:
         * Apple (red T1) × Carrot (orange T1).
         */
        fun preset(): Recipe = Recipe(CropRegistry.tier1("red"), CropRegistry.tier1("orange"))
    }
}
