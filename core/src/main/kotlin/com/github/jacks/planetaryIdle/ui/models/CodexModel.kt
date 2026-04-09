package com.github.jacks.planetaryIdle.ui.models

import com.github.jacks.planetaryIdle.components.Recipe
import java.math.BigDecimal

class CodexModel(
    private val kitchenViewModel: KitchenViewModel,
) : PropertyChangeSource() {

    var kitchenUnlocked by propertyNotify(kitchenViewModel.kitchenUnlocked)
    var unlockedCrops: Map<String, List<String>> by propertyNotify(kitchenViewModel.unlockedCrops)
    var discoveredRecipes: List<Recipe> by propertyNotify(kitchenViewModel.discoveredRecipes)

    init {
        kitchenViewModel.onPropertyChange(KitchenViewModel::kitchenUnlocked)   { kitchenUnlocked = it }
        kitchenViewModel.onPropertyChange(KitchenViewModel::unlockedCrops)     { unlockedCrops = it }
        kitchenViewModel.onPropertyChange(KitchenViewModel::discoveredRecipes) { discoveredRecipes = it }
    }

    /** Delegates to KitchenViewModel so the view doesn't need a direct reference. */
    fun estimatedPayout(recipe: Recipe): BigDecimal = kitchenViewModel.estimatedRecipePayout(recipe)
}
