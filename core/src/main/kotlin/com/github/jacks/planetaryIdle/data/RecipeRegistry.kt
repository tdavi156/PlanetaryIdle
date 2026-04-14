package com.github.jacks.planetaryIdle.data

import ktx.log.logger

object RecipeRegistry {

    /** Canonical left-to-right color order; defines adjacency for recipe generation. */
    val COLOR_SEQUENCE = listOf(
        "red", "orange", "yellow", "green", "blue",
        "purple", "pink", "brown", "white", "black"
    )

    /**
     * All exhaustive 2-color recipes: every adjacent color pair, every tier combination
     * where |tier1 - tier2| <= 2. Each pair appears exactly once (lower-sequence color first),
     * so there are no duplicates like "Apple x Carrot" and "Carrot x Apple".
     */
    val twoColorRecipes: List<Recipe> = buildTwoColorRecipes()

    /**
     * Parsed and normalized curated recipes from [CURATED_RECIPES].
     * - Unknown crop names are skipped with a warning.
     * - Entries are sorted into canonical color-sequence order before deduplication,
     *   so reversed or reordered entries collapse to the same recipe.
     */
    val curatedRecipes: List<Recipe> = parseCuratedRecipes()

    /**
     * The full recipe pool: two-color exhaustive list + curated multi-color entries,
     * deduplicated by id. This is what the research system draws from.
     */
    val all: List<Recipe> = (twoColorRecipes + curatedRecipes).distinctBy { it.id }

    // ── Builders ──────────────────────────────────────────────────────────────

    private fun buildTwoColorRecipes(): List<Recipe> {
        val recipes = mutableListOf<Recipe>()
        for (i in 0 until COLOR_SEQUENCE.size - 1) {
            val color1 = COLOR_SEQUENCE[i]
            val color2 = COLOR_SEQUENCE[i + 1]
            val crops1 = CropRegistry.forColor(color1) // sorted by tier ascending
            val crops2 = CropRegistry.forColor(color2)
            for (c1 in crops1) {
                for (c2 in crops2) {
                    if (kotlin.math.abs(c1.tier - c2.tier) <= 2) {
                        recipes.add(Recipe(listOf(c1, c2))) // color1 always first → no duplicates
                    }
                }
            }
        }
        return recipes
    }

    private fun parseCuratedRecipes(): List<Recipe> {
        return CURATED_RECIPES.mapNotNull { names ->
            if (names.size < 2) {
                log.error { "CuratedRecipes: entry has fewer than 2 names: $names — skipping" }
                return@mapNotNull null
            }
            val crops = names.mapNotNull { name ->
                CropRegistry.all.find { it.cropName == name }.also { found ->
                    if (found == null) log.error { "CuratedRecipes: unknown crop '$name' in entry $names — skipping entry" }
                }
            }
            if (crops.size != names.size) return@mapNotNull null
            // Normalize to canonical color-sequence order so "Orange, Apple" == "Apple, Orange"
            val sorted = crops.sortedBy { COLOR_SEQUENCE.indexOf(it.color) }
            Recipe(sorted)
        }.distinctBy { it.id }
    }

    private val log = logger<RecipeRegistry>()
}
