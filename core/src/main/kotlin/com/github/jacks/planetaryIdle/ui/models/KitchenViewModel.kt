package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.CropRegistry
import com.github.jacks.planetaryIdle.components.CropType
import com.github.jacks.planetaryIdle.components.PlanetResources
import com.github.jacks.planetaryIdle.components.Recipe
import com.github.jacks.planetaryIdle.components.RecipeRegistry
import com.github.jacks.planetaryIdle.components.AchievementBonus
import com.github.jacks.planetaryIdle.components.Achievements
import com.github.jacks.planetaryIdle.events.AchievementCompletedEvent
import com.github.jacks.planetaryIdle.events.AchievementNotificationEvent
import com.github.jacks.planetaryIdle.events.ActiveCropChangedEvent
import com.github.jacks.planetaryIdle.events.BuyResourceEvent
import com.github.jacks.planetaryIdle.events.CropUnlockedEvent
import com.github.jacks.planetaryIdle.events.KitchenUnlockedEvent
import com.github.jacks.planetaryIdle.events.RecipeActivatedEvent
import com.github.jacks.planetaryIdle.events.RecipeDeactivatedEvent
import com.github.jacks.planetaryIdle.events.ResetGameEvent
import com.github.jacks.planetaryIdle.events.ResearchCompleteEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.quillraven.fleks.World
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import java.math.BigDecimal

// ── Data classes ──────────────────────────────────────────────────────────────

data class ResearchJob(
    val inputs: List<CropType>,
    val duration: Float,
    val elapsed: Float,
    val discoveryChance: Float,
)

data class ResearcherState(
    val id: Int,
    val inputSlotCount: Int,
    val speedLevel: Int = 0,
    val activeJob: ResearchJob? = null,
) {
    val speedMultiplier: Float get() = 1f + speedLevel * 0.25f
}

sealed class ResearchResult {
    data class NewCrop(val cropType: CropType) : ResearchResult()
    data class NewRecipe(val recipe: Recipe) : ResearchResult()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class KitchenViewModel(
    @Suppress("UNUSED_PARAMETER") world: World,
    private val stage: Stage,
    val farmModel: FarmModel,
) : PropertyChangeSource(), EventListener {

    private val preferences: Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }

    var kitchenUnlocked by propertyNotify(false)

    /** color → ordered list of unlocked crop names for that color. */
    var unlockedCrops: Map<String, List<String>> by propertyNotify(emptyMap())

    /** color → currently active crop name. */
    var activeCrops: Map<String, String> by propertyNotify(emptyMap())

    /** All recipes the player has discovered. */
    var discoveredRecipes: List<Recipe> by propertyNotify(emptyList())

    /** Recipes currently set (in effect). */
    var activeRecipes: List<Recipe> by propertyNotify(emptyList())

    /** All researcher slots. */
    var researchers: List<ResearcherState> by propertyNotify(emptyList())

    /** Bonus multiplier applied to research job speed from the Master Builder achievement. */
    var researchSpeedBonusMultiplier: Float = 1f

    init {
        stage.addListener(this)
        researchSpeedBonusMultiplier = if (preferences["bonus_research_speed", false]) 1.2f else 1f
        load()
    }

    // ── Public helpers ────────────────────────────────────────────────────────

    fun unlockedCropCount(color: String): Int = (unlockedCrops[color] ?: emptyList()).size

    fun getActiveCropType(color: String): CropType? {
        val name = activeCrops[color] ?: return CropRegistry.tier1(color)
        return CropRegistry.forColor(color).find { it.cropName == name }
    }

    fun hireCost(currentCount: Int): BigDecimal = HIRE_RESEARCHER_BASE_COST.multiply(
        BigDecimal("5").pow(currentCount)
    )

    fun slotUpgradeCost(researcherIndex: Int): BigDecimal = UPGRADE_SLOTS_BASE_COST.multiply(
        BigDecimal("3").pow(researcherIndex)
    )

    fun speedUpgradeCost(researcher: ResearcherState): BigDecimal = SPEED_UPGRADE_BASE_COST.multiply(
        BigDecimal("2").pow(researcher.speedLevel)
    )

    // ── EventListener ────────────────────────────────────────────────────────

    override fun handle(event: Event): Boolean {
        when (event) {
            is KitchenUnlockedEvent -> {
                if (!kitchenUnlocked) {
                    kitchenUnlocked = true
                    preferences.flush { this["kitchen_unlocked"] = true }
                    initializeUnlockedCrops()
                    grantPresetRecipe()
                    if (researchers.isEmpty()) {
                        researchers = listOf(ResearcherState(id = 0, inputSlotCount = 2))
                        persistResearchers()
                    }
                    stage.fire(AchievementNotificationEvent("kitchen_unlock"))
                }
                return false
            }
            is BuyResourceEvent -> {
                if (kitchenUnlocked) {
                    val color = event.resourceType
                    val current = unlockedCrops[color] ?: emptyList()
                    if (current.isEmpty()) {
                        val t1 = CropRegistry.tier1(color)
                        val newMap = unlockedCrops.toMutableMap()
                        newMap[color] = listOf(t1.cropName)
                        unlockedCrops = newMap
                        if (activeCrops[color] == null) {
                            val newActive = activeCrops.toMutableMap()
                            newActive[color] = t1.cropName
                            activeCrops = newActive
                            persistActiveCrops()
                        }
                        persistUnlockedCrops()
                    }
                }
                return false
            }
            is AchievementCompletedEvent -> {
                if (event.achId.isEmpty()) return false
                val ach = Achievements.entries.find { it.achId == event.achId }
                if (ach?.bonus is AchievementBonus.ResearchSpeedBonus) {
                    researchSpeedBonusMultiplier = (ach.bonus as AchievementBonus.ResearchSpeedBonus).multiplier
                    preferences.flush { this["bonus_research_speed"] = true }
                }
                return false
            }
            is ResetGameEvent -> {
                researchSpeedBonusMultiplier = 1f
                return false
            }
            else -> return false
        }
    }

    // ── Active crop ───────────────────────────────────────────────────────────

    fun setActiveCrop(color: String, cropName: String) {
        val currentActive = activeCrops[color]
        if (currentActive == cropName) return
        val cropType = CropRegistry.forColor(color).find { it.cropName == cropName } ?: return

        // Deactivate any recipe that involves this color
        activeRecipes.filter { recipe -> recipe.crops.any { it.color == color } }
            .forEach { deactivateRecipe(it) }

        val newActive = activeCrops.toMutableMap()
        newActive[color] = cropName
        activeCrops = newActive
        persistActiveCrops()

        stage.fire(ActiveCropChangedEvent(
            color            = color,
            cropName         = cropName,
            newBasePayout    = cropType.baseValue,
            newCycleDuration = BigDecimal(cropType.baseProductionTime.toString()),
        ))
    }

    // ── Recipes ───────────────────────────────────────────────────────────────

    fun activateRecipe(recipe: Recipe) {
        if (activeRecipes.any { it.id == recipe.id }) return

        val colors = recipe.crops.map { it.color }

        // Remove any active recipe that shares a color with this one
        activeRecipes.filter { active -> active.crops.any { it.color in colors } }
            .forEach { deactivateRecipe(it) }

        // Auto-switch active crop for each color involved
        recipe.crops.forEach { crop ->
            if (activeCrops[crop.color] != crop.cropName) setActiveCrop(crop.color, crop.cropName)
        }

        activeRecipes = activeRecipes + recipe
        persistActiveRecipes()
        stage.fire(RecipeActivatedEvent(recipe))
    }

    fun deactivateRecipe(recipe: Recipe) {
        if (activeRecipes.none { it.id == recipe.id }) return
        activeRecipes = activeRecipes.filter { it.id != recipe.id }
        persistActiveRecipes()
        stage.fire(RecipeDeactivatedEvent(recipe))
    }

    // ── Research ──────────────────────────────────────────────────────────────

    fun startResearch(researcherIndex: Int, cropInputs: List<CropType>) {
        val researcher = researchers.getOrNull(researcherIndex) ?: return
        if (researcher.activeJob != null) return
        if (cropInputs.isEmpty()) return

        val rawDuration = cropInputs.sumOf { it.tier * it.tier } * BASE_RESEARCH_SECONDS
        val duration = rawDuration / researcher.speedMultiplier / researchSpeedBonusMultiplier
        val chance = computeDiscoveryChance(cropInputs)
        val job = ResearchJob(inputs = cropInputs, duration = duration, elapsed = 0f, discoveryChance = chance)

        val updated = researchers.toMutableList()
        updated[researcherIndex] = researcher.copy(activeJob = job)
        researchers = updated
    }

    fun cancelResearch(researcherIndex: Int) {
        val researcher = researchers.getOrNull(researcherIndex) ?: return
        val updated = researchers.toMutableList()
        updated[researcherIndex] = researcher.copy(activeJob = null)
        researchers = updated
    }

    fun hireResearcher(): Boolean {
        val cost = hireCost(researchers.size)
        if (farmModel.goldCoins < cost) return false
        farmModel.goldCoins -= cost
        preferences.flush { this["gold_coins"] = farmModel.goldCoins.toString() }
        val newResearcher = ResearcherState(id = researchers.size, inputSlotCount = 2)
        researchers = researchers + newResearcher
        persistResearchers()
        return true
    }

    fun upgradeResearcherSlots(researcherIndex: Int): Boolean {
        val researcher = researchers.getOrNull(researcherIndex) ?: return false
        if (researcher.inputSlotCount >= MAX_INPUT_SLOTS) return false
        val cost = slotUpgradeCost(researcherIndex)
        if (farmModel.goldCoins < cost) return false
        farmModel.goldCoins -= cost
        preferences.flush { this["gold_coins"] = farmModel.goldCoins.toString() }
        val updated = researchers.toMutableList()
        updated[researcherIndex] = researcher.copy(inputSlotCount = researcher.inputSlotCount + 1)
        researchers = updated
        persistResearchers()
        return true
    }

    fun upgradeResearcherSpeed(researcherIndex: Int): Boolean {
        val researcher = researchers.getOrNull(researcherIndex) ?: return false
        val cost = speedUpgradeCost(researcher)
        if (farmModel.goldCoins < cost) return false
        farmModel.goldCoins -= cost
        preferences.flush { this["gold_coins"] = farmModel.goldCoins.toString() }
        val updated = researchers.toMutableList()
        updated[researcherIndex] = researcher.copy(speedLevel = researcher.speedLevel + 1)
        researchers = updated
        persistResearchers()
        return true
    }

    fun computeDiscoveryChance(inputs: List<CropType>): Float {
        if (inputs.isEmpty()) return 0f
        val avgTier = inputs.map { it.tier }.average().toFloat()
        val baseChance = (0.3f + avgTier * 0.1f).coerceIn(0f, 0.95f)
        return if (canDiscoverAnything(inputs)) baseChance else 0f
    }

    /** Called every frame from KitchenView.act(). Advances active research jobs. */
    fun update(delta: Float) {
        if (!kitchenUnlocked) return
        var changed = false
        val updated = researchers.toMutableList()
        updated.forEachIndexed { index, researcher ->
            val job = researcher.activeJob ?: return@forEachIndexed
            val newElapsed = job.elapsed + delta
            if (newElapsed >= job.duration) {
                val result = resolveResearch(job)
                if (result != null) applyResearchResult(result)
                updated[index] = researcher.copy(activeJob = null)
                stage.fire(ResearchCompleteEvent(
                    researcherIndex   = index,
                    discoveredCropId  = (result as? ResearchResult.NewCrop)?.cropType?.id,
                    discoveredRecipeId = (result as? ResearchResult.NewRecipe)?.recipe?.id,
                ))
                changed = true
            } else {
                updated[index] = researcher.copy(activeJob = job.copy(elapsed = newElapsed))
                changed = true
            }
        }
        if (changed) researchers = updated
    }

    // ── Research internals ────────────────────────────────────────────────────

    private fun canDiscoverAnything(inputs: List<CropType>): Boolean {
        val colors = inputs.map { it.color }.distinct()
        // Can we unlock a new crop tier for any input color?
        for (color in colors) {
            val unlocked = unlockedCrops[color] ?: emptyList()
            if (unlocked.size < CropRegistry.forColor(color).size) return true
        }
        // Can we discover an undiscovered recipe from the registry using only these colors?
        return RecipeRegistry.all.any { recipe ->
            recipe.crops.map { it.color }.all { it in colors } &&
            discoveredRecipes.none { it.id == recipe.id }
        }
    }

    private fun resolveResearch(job: ResearchJob): ResearchResult? {
        val roll = Math.random().toFloat()
        if (roll > job.discoveryChance) return null

        val colors = job.inputs.map { it.color }.distinct()
        val newCrop   = findDiscoverableCrop(colors)
        val newRecipe = findDiscoverableRecipe(colors)
        val preferCrop = Math.random() < 0.7

        return when {
            preferCrop && newCrop   != null -> ResearchResult.NewCrop(newCrop)
            !preferCrop && newRecipe != null -> ResearchResult.NewRecipe(newRecipe)
            newCrop   != null               -> ResearchResult.NewCrop(newCrop)
            newRecipe != null               -> ResearchResult.NewRecipe(newRecipe)
            else                            -> null
        }
    }

    private fun findDiscoverableCrop(colors: List<String>): CropType? {
        for (color in colors.shuffled()) {
            val unlocked = unlockedCrops[color] ?: emptyList()
            val next = CropRegistry.forColor(color).firstOrNull { it.cropName !in unlocked }
            if (next != null) return next
        }
        return null
    }

    /** Returns a random undiscovered recipe from the registry whose crops all belong to [colors]. */
    private fun findDiscoverableRecipe(colors: List<String>): Recipe? {
        return RecipeRegistry.all
            .filter { recipe ->
                recipe.crops.map { it.color }.all { it in colors } &&
                discoveredRecipes.none { it.id == recipe.id }
            }
            .randomOrNull()
    }

    private fun applyResearchResult(result: ResearchResult) {
        when (result) {
            is ResearchResult.NewCrop -> {
                val crop = result.cropType
                val current = unlockedCrops.toMutableMap()
                val list = current[crop.color]?.toMutableList() ?: mutableListOf()
                if (crop.cropName !in list) {
                    list.add(crop.cropName)
                    current[crop.color] = list
                    unlockedCrops = current
                    persistUnlockedCrops()
                    stage.fire(CropUnlockedEvent(crop.color, crop.cropName))
                    checkCropAchievements()
                }
            }
            is ResearchResult.NewRecipe -> {
                val recipe = result.recipe
                if (!discoveredRecipes.any { it.id == recipe.id }) {
                    discoveredRecipes = discoveredRecipes + recipe
                    persistDiscoveredRecipes()
                    checkRecipeAchievements()
                }
            }
        }
    }

    private fun checkCropAchievements() {
        // Discovered crops = non-tier-1 crops (T1 are granted at kitchen unlock, not "discovered")
        val discovered = unlockedCrops.entries.sumOf { (color, names) ->
            names.count { name ->
                CropRegistry.forColor(color).find { it.cropName == name }?.tier?.let { it > 1 } ?: false
            }
        }
        if (discovered >= 1)  stage.fire(AchievementNotificationEvent("kitchen_crop_1"))
        if (discovered >= 5)  stage.fire(AchievementNotificationEvent("kitchen_crop_5"))
        if (discovered >= 10) stage.fire(AchievementNotificationEvent("kitchen_crop_10"))
        if (discovered >= 50) stage.fire(AchievementNotificationEvent("kitchen_crop_50"))
    }

    private fun checkRecipeAchievements() {
        val count = discoveredRecipes.size
        if (count >= 1)  stage.fire(AchievementNotificationEvent("kitchen_recipe_1"))
        if (count >= 5)  stage.fire(AchievementNotificationEvent("kitchen_recipe_5"))
        if (count >= 10) stage.fire(AchievementNotificationEvent("kitchen_recipe_10"))
        if (count >= 50) stage.fire(AchievementNotificationEvent("kitchen_recipe_50"))
    }

    // ── Initialization helpers ────────────────────────────────────────────────

    private fun grantPresetRecipe() {
        val preset = Recipe.preset()
        if (!discoveredRecipes.any { it.id == preset.id }) {
            discoveredRecipes = discoveredRecipes + preset
            persistDiscoveredRecipes()
        }
    }

    private fun initializeUnlockedCrops() {
        val newMap = mutableMapOf<String, List<String>>()
        // Load any previously saved unlocked crops first
        PlanetResources.entries.forEach { res ->
            val saved = preferences["kitchen_unlocked_crops_${res.resourceName}", ""]
            if (saved.isNotEmpty()) newMap[res.resourceName] = saved.split(",").filter { it.isNotEmpty() }
        }
        // Also unlock T1 for any colors the player currently owns or has ever unlocked
        PlanetResources.entries.forEach { res ->
            val ownedStr = preferences["${res.resourceName}_owned", "0"]
            val hasUnlocked = preferences["${res.resourceName}_unlocked", res == PlanetResources.RED]
            if ((ownedStr.toBigDecimalOrNull() ?: BigDecimal.ZERO) > BigDecimal.ZERO || hasUnlocked) {
                val existing = newMap[res.resourceName] ?: emptyList()
                val t1 = CropRegistry.tier1(res.resourceName)
                if (t1.cropName !in existing) {
                    newMap[res.resourceName] = listOf(t1.cropName) + existing.filter { it != t1.cropName }
                } else if (newMap[res.resourceName] == null) {
                    newMap[res.resourceName] = listOf(t1.cropName)
                }
            }
        }
        unlockedCrops = newMap

        // Initialize active crops to T1 for each color that has unlocked crops
        val newActive = activeCrops.toMutableMap()
        newMap.keys.forEach { color ->
            if (newActive[color] == null) {
                newActive[color] = (newMap[color] ?: emptyList()).firstOrNull()
                    ?: CropRegistry.tier1(color).cropName
            }
        }
        activeCrops = newActive
        persistUnlockedCrops()
        persistActiveCrops()
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    private fun load() {
        kitchenUnlocked = preferences["kitchen_unlocked", false]
        if (!kitchenUnlocked) return

        loadUnlockedCrops()
        loadActiveCrops()
        loadDiscoveredRecipes()
        loadActiveRecipes()
        loadResearchers()
    }

    private fun loadUnlockedCrops() {
        val map = mutableMapOf<String, List<String>>()
        PlanetResources.entries.forEach { res ->
            val saved = preferences["kitchen_unlocked_crops_${res.resourceName}", ""]
            if (saved.isNotEmpty()) map[res.resourceName] = saved.split(",").filter { it.isNotEmpty() }
        }
        unlockedCrops = map
    }

    private fun loadActiveCrops() {
        val map = mutableMapOf<String, String>()
        PlanetResources.entries.forEach { res ->
            val saved = preferences["kitchen_active_crop_${res.resourceName}", ""]
            if (saved.isNotEmpty()) map[res.resourceName] = saved
        }
        activeCrops = map
    }

    private fun loadDiscoveredRecipes() {
        val saved = preferences["kitchen_discovered_recipes", ""]
        discoveredRecipes = if (saved.isEmpty()) emptyList()
        else saved.split(",").filter { it.isNotEmpty() }.mapNotNull { parseRecipe(it) }
    }

    private fun loadActiveRecipes() {
        val saved = preferences["kitchen_active_recipes", ""]
        activeRecipes = if (saved.isEmpty()) emptyList()
        else saved.split(",").filter { it.isNotEmpty() }.mapNotNull { parseRecipe(it) }
    }

    private fun loadResearchers() {
        val count = preferences["kitchen_researcher_count", 0]
        researchers = if (count == 0) {
            listOf(ResearcherState(id = 0, inputSlotCount = 2))
        } else {
            (0 until count).map { i ->
                ResearcherState(
                    id             = i,
                    inputSlotCount = preferences["kitchen_researcher_${i}_input_slots", 2],
                    speedLevel     = preferences["kitchen_researcher_${i}_speed_level", 0],
                )
            }
        }
    }

    private fun parseRecipe(id: String): Recipe? {
        // id format: "{cropId1}_x_{cropId2}[_x_{cropId3}...]"
        val parts = id.split("_x_")
        if (parts.size < 2) return null
        val crops = parts.map { part -> CropRegistry.byId(part) ?: return null }
        return Recipe(crops)
    }

    private fun persistUnlockedCrops() {
        preferences.flush {
            PlanetResources.entries.forEach { res ->
                this["kitchen_unlocked_crops_${res.resourceName}"] =
                    (unlockedCrops[res.resourceName] ?: emptyList()).joinToString(",")
            }
        }
    }

    private fun persistActiveCrops() {
        preferences.flush {
            PlanetResources.entries.forEach { res ->
                this["kitchen_active_crop_${res.resourceName}"] = activeCrops[res.resourceName] ?: ""
            }
        }
    }

    private fun persistDiscoveredRecipes() {
        preferences.flush { this["kitchen_discovered_recipes"] = discoveredRecipes.joinToString(",") { it.id } }
    }

    private fun persistActiveRecipes() {
        preferences.flush { this["kitchen_active_recipes"] = activeRecipes.joinToString(",") { it.id } }
    }

    private fun persistResearchers() {
        preferences.flush {
            this["kitchen_researcher_count"] = researchers.size
            researchers.forEachIndexed { i, r ->
                this["kitchen_researcher_${i}_input_slots"] = r.inputSlotCount
                this["kitchen_researcher_${i}_speed_level"] = r.speedLevel
            }
        }
    }

    companion object {
        private val log = logger<KitchenViewModel>()
        const val BASE_RESEARCH_SECONDS = 60f
        const val MAX_INPUT_SLOTS = 4
        val HIRE_RESEARCHER_BASE_COST   = BigDecimal("1000000000")
        val UPGRADE_SLOTS_BASE_COST     = BigDecimal("10000000000")
        val SPEED_UPGRADE_BASE_COST     = BigDecimal("5000000000")
    }
}
