package com.github.jacks.planetaryIdle.components

import java.math.BigDecimal

data class Recipe(val crops: List<CropType>) {

    /** Unique stable ID used for persistence. Crops are always in canonical color-sequence order. */
    val id: String get() = crops.joinToString("_x_") { it.id }

    val displayName: String get() = crops.joinToString(" x ") { it.cropName }

    /** Combined cycle time in seconds (additive). */
    val combinedTime: Float get() = crops.sumOf { it.baseProductionTime.toDouble() }.toFloat()

    /** Multiplied base value (multiplicative). */
    val combinedBaseValue: BigDecimal get() = crops.fold(BigDecimal.ONE) { acc, c -> acc.multiply(c.baseValue) }

    companion object {
        /**
         * The first preset recipe automatically discovered on kitchen unlock:
         * Apple (red T1) × Carrot (orange T1).
         */
        fun preset(): Recipe = Recipe(listOf(CropRegistry.tier1("red"), CropRegistry.tier1("orange")))
    }
}
