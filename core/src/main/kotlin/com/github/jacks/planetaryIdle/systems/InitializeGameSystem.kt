package com.github.jacks.planetaryIdle.systems

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.jacks.planetaryIdle.components.AchievementComponent
import com.github.jacks.planetaryIdle.components.AchievementConfiguration
import com.github.jacks.planetaryIdle.components.AchievementType
import com.github.jacks.planetaryIdle.components.Achievements
import com.github.jacks.planetaryIdle.components.ConfigurationComponent
import com.github.jacks.planetaryIdle.components.ConfigurationType.*
import com.github.jacks.planetaryIdle.components.PlanetResources
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.components.ResourceConfiguration
import com.github.jacks.planetaryIdle.components.ScoreResources
import com.github.jacks.planetaryIdle.components.UpgradeComponent
import com.github.jacks.planetaryIdle.components.UpgradeConfiguration
import com.github.jacks.planetaryIdle.components.UpgradeType
import com.github.jacks.planetaryIdle.events.InitializeGameEvent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import java.math.BigDecimal

@AllOf([ConfigurationComponent::class])
class InitializeGameSystem(
    private val configurationComponents: ComponentMapper<ConfigurationComponent>
) : EventListener, IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        with(configurationComponents[entity]) {
            when (configurationType) {
                SCORE_RESOURCE -> {
                    val config = getScoreResourceConfiguration(configurationName)
                    world.entity {
                        add<ResourceComponent> {
                            name = config.name
                            amountOwned = config.amountOwned
                        }
                    }
                }
                PLANET_RESOURCE -> {
                    val config = getPlanetResourceConfiguration(configurationName)
                    world.entity {
                        add<ResourceComponent> {
                            name = config.name
                            amountOwned = config.amountOwned
                            baseCost = config.baseCost
                            costScaling = config.costScaling
                            basePayout = config.basePayout
                            cycleDuration = config.cycleDuration
                            currentTicks = config.currentTicks
                            isUnlocked = config.isUnlocked
                        }
                    }
                }
                ACHIEVEMENT -> {
                    val config = getAchievementConfiguration(configurationName)
                    world.entity {
                        add<AchievementComponent> {
                            multiplierScale = config.multiplierScale
                            completedAchievements.addAll(config.completedAchievements)
                        }
                    }
                }
                UPGRADE -> {
                    val config = getUpgradeConfiguration(configurationName)
                    world.entity {
                        add<UpgradeComponent> {
                            isUnlocked = config.isUnlocked
                            soilUpgrades = config.soilUpgrades
                        }
                    }
                }
                UNDEFINED -> {
                    log.debug { "$configurationName has an UNDEFINED type." }
                    return@with
                }
            }
        }
        world.remove(entity)
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is InitializeGameEvent -> {
                if (!preferences["is_game_initialized", false]) {
                    preferences.clear()
                    setupPreferences()
                }
                ScoreResources.entries.forEach { resource ->
                    world.entity {
                        add<ConfigurationComponent> {
                            configurationName = resource.resourceName
                            configurationType = SCORE_RESOURCE
                        }
                    }
                }
                PlanetResources.entries.forEach { resource ->
                    world.entity {
                        add<ConfigurationComponent> {
                            configurationName = resource.resourceName
                            configurationType = PLANET_RESOURCE
                        }
                    }
                }
                AchievementType.entries.forEach { type ->
                    world.entity {
                        add<ConfigurationComponent> {
                            configurationName = type.typeName
                            configurationType = ACHIEVEMENT
                        }
                    }
                }
                UpgradeType.entries.forEach { type ->
                    world.entity {
                        add<ConfigurationComponent> {
                            configurationName = type.typeName
                            configurationType = UPGRADE
                        }
                    }
                }
            }
            else -> return false
        }
        return true
    }

    private fun getScoreResourceConfiguration(configName: String): ResourceConfiguration {
        return when (configName) {
            ScoreResources.GOLD_COINS.resourceName -> ResourceConfiguration(
                name = "gold_coins",
                amountOwned = BigDecimal(preferences["gold_coins", "5"])
            )
            else -> ResourceConfiguration()
        }
    }

    private fun getPlanetResourceConfiguration(configName: String): ResourceConfiguration {
        val resource = PlanetResources.entries.find { it.resourceName == configName }
            ?: return ResourceConfiguration()
        return ResourceConfiguration(
            name = resource.resourceName,
            amountOwned = BigDecimal(preferences["${resource.resourceName}_owned", "0"]),
            baseCost = BigDecimal(resource.baseCost),
            costScaling = BigDecimal(COST_SCALING[resource] ?: "0.2"),
            basePayout = BigDecimal(resource.basePayout),
            cycleDuration = BigDecimal(resource.cycleDuration),
            currentTicks = preferences["${resource.resourceName}_current_ticks", 0],
            isUnlocked = preferences["${resource.resourceName}_unlocked", resource == PlanetResources.RED]
        )
    }

    private fun getAchievementConfiguration(configName: String): AchievementConfiguration {
        return when (configName) {
            AchievementType.BASIC_ACHIEVEMENT.typeName -> AchievementConfiguration(
                name = "basic_achievement",
                completedAchievements = Achievements.entries
                    .filter { preferences["ach_${it.achId}", false] }
                    .map { it.achId }
                    .toSet(),
                multiplierScale = java.math.BigDecimal(
                    preferences["achievement_multiplier_scale", "1.05"]
                )
            )
            else -> AchievementConfiguration()
        }
    }

    private fun getUpgradeConfiguration(configName: String): UpgradeConfiguration {
        return when (configName) {
            UpgradeType.SOIL_UPGRADE.typeName -> UpgradeConfiguration(
                name = "soil_upgrade",
                isUnlocked = preferences["soil_is_unlocked", false],
                soilUpgrades = BigDecimal(preferences["soil_upgrades", "0"])
            )
            else -> UpgradeConfiguration()
        }
    }

    private fun setupPreferences() {
        preferences.flush {
            this["is_game_initialized"] = true
            this["gold_coins"] = "5"
            this["production_rate"] = "0"
            this["buy_amount"] = 1f
            this["achievement_multiplier"] = "1"

            PlanetResources.entries.forEach { resource ->
                this["${resource.resourceName}_owned"] = "0"
                this["${resource.resourceName}_cost"] = resource.baseCost
                this["${resource.resourceName}_current_ticks"] = 0
                this["${resource.resourceName}_unlocked"] = (resource == PlanetResources.RED)
            }

            this["soil_is_unlocked"] = false
            this["soil_upgrades"] = "0"
            this["soil_cost"] = "1000000"
            this["barn_unlocked"] = false

            this["achievement_multiplier_scale"] = "1.05"
            this["bonus_red_production"] = false
            this["bonus_all_production"] = false
            this["bonus_gold_income"] = false
            this["bonus_soil_cost_discount"] = false
            this["bonus_perfect_soil"] = false
            this["bonus_research_speed"] = false

            Achievements.entries.forEach { ach -> this["ach_${ach.achId}"] = false }
        }
    }

    companion object {
        private val log = logger<InitializeGameSystem>()
        val preferences: Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }

        private val COST_SCALING: Map<PlanetResources, String> = mapOf(
            PlanetResources.RED    to "0.2",
            PlanetResources.ORANGE to "0.225",
            PlanetResources.YELLOW to "0.25",
            PlanetResources.GREEN  to "0.275",
            PlanetResources.BLUE   to "0.3",
            PlanetResources.PURPLE to "0.325",
            PlanetResources.PINK   to "0.35",
            PlanetResources.BROWN  to "0.375",
            PlanetResources.WHITE  to "0.4",
            PlanetResources.BLACK  to "0.5",
        )
    }
}
