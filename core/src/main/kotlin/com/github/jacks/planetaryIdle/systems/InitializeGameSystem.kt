package com.github.jacks.planetaryIdle.systems

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.jacks.planetaryIdle.components.AchievementComponent
import com.github.jacks.planetaryIdle.components.ConfigurationComponent
import com.github.jacks.planetaryIdle.components.ConfigurationType.*
import com.github.jacks.planetaryIdle.components.PlanetResources
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.components.ResourceConfiguration
import com.github.jacks.planetaryIdle.components.ScoreResources
import com.github.jacks.planetaryIdle.components.UpgradeComponent
import com.github.jacks.planetaryIdle.events.InitializeGameEvent
import com.github.jacks.planetaryIdle.events.LoadGameEvent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import java.math.*

@AllOf([ConfigurationComponent::class])
class InitializeGameSystem(
    private val configurationComponents : ComponentMapper<ConfigurationComponent>
) : EventListener, IteratingSystem() {

    private val preferences : Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }

    override fun onTickEntity(entity: Entity) {
        with(configurationComponents[entity]) {
            when(configurationType) {
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
                    val config = getPlanetResourceConfigurations(configurationName)
                    world.entity {
                        add<ResourceComponent> {
                            name = config.name
                            amountOwned = config.amountOwned
                            baseCost = config.baseCost
                            costScaling = config.costScaling
                            baseValue = config.baseValue
                            valueScaling = config.valueScaling
                            baseRate = config.baseRate
                            rateScaling = config.rateScaling
                            currentTicks = config.currentTicks
                            isUnlocked = config.isUnlocked
                        }
                    }
                }
                MULTIPLIER -> {
                    world.entity {
                        add<AchievementComponent> {
                            // load completed achievements
                        }
                        add<UpgradeComponent> {
                            // load upgrade counts and scaling
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
                if (!preferences["isGameInitialized", false]) {
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
                world.entity {
                    add<ConfigurationComponent> {
                        configurationName = "multiplier"
                        configurationType = MULTIPLIER
                    }
                }
            }
            is LoadGameEvent -> {

            }
            else -> return false
        }
        return true
    }

    private fun getScoreResourceConfiguration(configName : String) : ResourceConfiguration {
        return when (configName) {
            ScoreResources.GOLD_COINS.resourceName -> GOLD_SCORE_CONFIGURATION
            else -> ResourceConfiguration()
        }
    }
    private fun getPlanetResourceConfigurations(configName : String) : ResourceConfiguration {
        return when (configName) {
            PlanetResources.RED.resourceName -> RED_CONFIGURATION
            PlanetResources.ORANGE.resourceName -> ORANGE_CONFIGURATION
            PlanetResources.YELLOW.resourceName -> YELLOW_CONFIGURATION
            PlanetResources.GREEN.resourceName -> GREEN_CONFIGURATION
            PlanetResources.BLUE.resourceName -> BLUE_CONFIGURATION
            PlanetResources.PURPLE.resourceName -> PURPLE_CONFIGURATION
            PlanetResources.PINK.resourceName -> PINK_CONFIGURATION
            PlanetResources.BROWN.resourceName -> BROWN_CONFIGURATION
            PlanetResources.WHITE.resourceName -> WHITE_CONFIGURATION
            PlanetResources.BLACK.resourceName -> BLACK_CONFIGURATION
            else -> ResourceConfiguration()
        }
    }

    private fun setupPreferences() {
        preferences.flush {
            this["is_game_initialized"] = true

            this["gold_coins"] = "5"
            this["production_rate"] = "0"
            this["buy_amount"] = 1f

            this["red_owned"] = "0"
            this["red_cost"] = "1"
            this["red_value"] = "0.31"
            this["red_rate"] = "1.3"
            this["red_current_ticks"] = 0
            this["red_unlocked"] = true

            this["orange_owned"] = "0"
            this["orange_cost"] = "100"
            this["orange_value"] = "2.4"
            this["orange_rate"] = "0.95"
            this["orange_current_ticks"] = 0
            this["orange_unlocked"] = false
        }
    }

    companion object {
        private val log = logger<InitializeGameSystem>()
        private val preferences : Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }
        val GOLD_SCORE_CONFIGURATION = ResourceConfiguration(
            name = "gold_coins",
            amountOwned = BigDecimal(preferences["gold_coins", "5"])
        )
        val RED_CONFIGURATION = ResourceConfiguration(
            name = "red",
            amountOwned = BigDecimal(preferences["red_owned", "0"]),
            baseCost = BigDecimal("1"),
            costScaling = BigDecimal("0.2"),
            baseValue = BigDecimal("0.31"),
            valueScaling = BigDecimal("0.04"),
            baseRate = BigDecimal("1.3"),
            rateScaling = BigDecimal("0.17"),
            currentTicks = preferences["red_current_ticks", 0],
            isUnlocked = preferences["red_unlocked", true]
        )
        val ORANGE_CONFIGURATION = ResourceConfiguration(
            name = "orange",
            amountOwned = BigDecimal(preferences["orange_owned", "0"]),
            baseCost = BigDecimal("100"),
            costScaling = BigDecimal("0.225"),
            baseValue = BigDecimal("2.4"),
            valueScaling = BigDecimal("0.09"),
            baseRate = BigDecimal("0.95"),
            rateScaling = BigDecimal("0.11"),
            currentTicks = preferences["orange_current_ticks", 0],
            isUnlocked = preferences["orange_unlocked", false]
        )
        val YELLOW_CONFIGURATION = ResourceConfiguration(
            name = "yellow",
            amountOwned = BigDecimal(preferences["yellow_owned", "0"]),
            baseCost = BigDecimal("1000"),
            costScaling = BigDecimal("0.25"),
            baseValue = BigDecimal("18.9"),
            valueScaling = BigDecimal("0.23"),
            baseRate = BigDecimal("0.67"),
            rateScaling = BigDecimal("0.07"),
            currentTicks = preferences["yellow_current_ticks", 0],
            isUnlocked = preferences["yellow_unlocked", false]
        )
        val GREEN_CONFIGURATION = ResourceConfiguration(
            name = "green",
            amountOwned = BigDecimal(preferences["green_owned", "0"]),
            baseCost = BigDecimal("50000"),
            costScaling = BigDecimal("0.275"),
            baseValue = BigDecimal("147.1"),
            valueScaling = BigDecimal("3.21"),
            baseRate = BigDecimal("0.43"),
            rateScaling = BigDecimal("0.06"),
            isUnlocked = preferences["green_unlocked", false]
        )
        val BLUE_CONFIGURATION = ResourceConfiguration(
            name = "blue",
            amountOwned = BigDecimal(preferences["blue_owned", "0"]),
            baseCost = BigDecimal("1000000"),
            costScaling = BigDecimal("0.3"),
            baseValue = BigDecimal("1147"),
            valueScaling = BigDecimal("12.5"),
            baseRate = BigDecimal("0.21"),
            rateScaling = BigDecimal("0.05"),
            isUnlocked = preferences["blue_unlocked", false]
        )
        val PURPLE_CONFIGURATION = ResourceConfiguration(
            name = "purple",
            amountOwned = BigDecimal(preferences["purple_owned", "0"]),
            baseCost = BigDecimal("500000000"),
            costScaling = BigDecimal("0.325"),
            baseValue = BigDecimal("8952"),
            valueScaling = BigDecimal("46.3"),
            baseRate = BigDecimal("0.12"),
            rateScaling = BigDecimal("0.04"),
            isUnlocked = preferences["purple_unlocked", false]
        )
        val PINK_CONFIGURATION = ResourceConfiguration(
            name = "pink",
            amountOwned = BigDecimal(preferences["pink_owned", "0"]),
            baseCost = BigDecimal("10000000000"),
            costScaling = BigDecimal("0.35"),
            baseValue = BigDecimal("69811"),
            valueScaling = BigDecimal("374.8"),
            baseRate = BigDecimal("0.08"),
            rateScaling = BigDecimal("0.03"),
            isUnlocked = preferences["pink_unlocked", false]
        )
        val BROWN_CONFIGURATION = ResourceConfiguration(
            name = "brown",
            amountOwned = BigDecimal(preferences["brown_owned", "0"]),
            baseCost = BigDecimal("100000000000000"),
            costScaling = BigDecimal("0.375"),
            baseValue = BigDecimal("544532"),
            valueScaling = BigDecimal("2567"),
            baseRate = BigDecimal("0.05"),
            rateScaling = BigDecimal("0.02"),
            isUnlocked = preferences["brown_unlocked", false]
        )
        val WHITE_CONFIGURATION = ResourceConfiguration(
            name = "white",
            amountOwned = BigDecimal(preferences["white_owned", "0"]),
            baseCost = BigDecimal("1000000000000000000"),
            costScaling = BigDecimal("0.4"),
            baseValue = BigDecimal("4247354"),
            valueScaling = BigDecimal("74502"),
            baseRate = BigDecimal("0.03"),
            rateScaling = BigDecimal("0.01"),
            isUnlocked = preferences["white_unlocked", false]
        )
        val BLACK_CONFIGURATION = ResourceConfiguration(
            name = "black",
            amountOwned = BigDecimal(preferences["black_owned", "0"]),
            baseCost = BigDecimal("1000000000000000000000000"),
            costScaling = BigDecimal("0.5"),
            baseValue = BigDecimal("33129365"),
            valueScaling = BigDecimal("3312936"),
            baseRate = BigDecimal("0.01"),
            rateScaling = BigDecimal("0.005"),
            isUnlocked = preferences["black_unlocked", false]
        )
    }
}
