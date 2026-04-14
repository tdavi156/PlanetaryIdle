package com.github.jacks.planetaryIdle.data

/**
 * Hand-curated multi-color recipes (3 or more sequential colors).
 *
 * Two-color recipes are generated exhaustively by RecipeRegistry — no need to add them here.
 *
 * FORMAT
 * ------
 * Each entry is a List<String> of crop names in any order.
 * The registry will sort them into canonical color-sequence order automatically.
 *
 * CONSTRAINTS (not enforced at runtime — honor them manually)
 * -----------------------------------------------------------
 *   • Crops must come from colors that are adjacent in the sequence:
 *       Red → Orange → Yellow → Green → Blue → Purple → Pink → Brown → White → Black
 *     e.g. Red + Orange + Yellow is valid; Red + Yellow (skipping Orange) is not.
 *   • Maximum tier difference of 2 between any two crops in the recipe.
 *
 * EXAMPLES (uncomment to enable)
 * --------------------------------
 *
 *   // 3-color recipes
 *   listOf("Apple",      "Carrot",  "Corn"),          // Red T1  × Orange T1 × Yellow T1
 *   listOf("Tomato",     "Orange",  "Banana"),         // Red T2  × Orange T2 × Yellow T2
 *   listOf("Strawberry", "Mango",   "Lemon"),          // Red T3  × Orange T3 × Yellow T3
 *   listOf("Cherry",     "Pumpkin", "Squash"),         // Red T4  × Orange T4 × Yellow T4
 *   listOf("Carrot",     "Corn",    "Lettuce"),        // Orange T1 × Yellow T1 × Green T1
 *
 *   // 4-color recipes
 *   listOf("Apple",  "Carrot", "Corn",   "Lettuce"),  // Red T1 × Orange T1 × Yellow T1 × Green T1
 *   listOf("Tomato", "Orange", "Banana", "Cucumber"), // Red T2 × Orange T2 × Yellow T2 × Green T2
 */
val CURATED_RECIPES: List<List<String>> = listOf(
    // Add multi-color recipes here:
)
