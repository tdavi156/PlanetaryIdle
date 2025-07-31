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
                POPULATION -> {
                    val config = POPULATION_CONFIGURATION
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
                            tier = config.tier
                            baseValue = config.baseValue
                            baseCost = config.baseCost
                            amountOwned = config.amountOwned
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
                world.entity {
                    add<ConfigurationComponent> {
                        configurationName = "population"
                        configurationType = POPULATION
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

    private fun getPlanetResourceConfigurations(configName : String) : ResourceConfiguration {
        return when (configName) {
            PlanetResources.WHEAT.resourceName -> WHEAT_CONFIGURATION
            PlanetResources.CORN.resourceName -> CORN_CONFIGURATION
            PlanetResources.LETTUCE.resourceName -> LETTUCE_CONFIGURATION
            PlanetResources.CARROTS.resourceName -> CARROTS_CONFIGURATION
            PlanetResources.TOMATOES.resourceName -> TOMATOES_CONFIGURATION
            PlanetResources.BROCCOLI.resourceName -> BROCCOLI_CONFIGURATION
            PlanetResources.ONIONS.resourceName -> ONIONS_CONFIGURATION
            PlanetResources.POTATOES.resourceName -> POTATOES_CONFIGURATION
            else -> ResourceConfiguration()
        }
    }

    private fun setupPreferences() {
        preferences.flush {
            this["isGameInitialized"] = true

            this["totalPopulation"] = "10"
            this["availablePopulation"] = "10"
            this["populationGainRate"] = "0"
            this["buyAmount"] = 1

            this["wheat_amount"] = "0"
            this["wheat_multiplier"] = "1"
            this["wheat_cost"] = "10"
            this["wheat_unlocked"] = true

            this["corn_amount"] = "0"
            this["corn_multiplier"] = "1"
            this["corn_cost"] = "100"
            this["corn_unlocked"] = false

            this["lettuce_amount"] = "0"
            this["lettuce_multiplier"] = "1"
            this["lettuce_cost"] = "1000"
            this["lettuce_unlocked"] = false

            this["carrots_amount"] = "0"
            this["carrots_multiplier"] = "1"
            this["carrots_cost"] = "10000"
            this["carrots_unlocked"] = false

            this["tomatoes_amount"] = "0"
            this["tomatoes_multiplier"] = "1"
            this["tomatoes_cost"] = "100000"
            this["tomatoes_unlocked"] = false

            this["broccoli_amount"] = "0"
            this["broccoli_multiplier"] = "1"
            this["broccoli_cost"] = "1000000"
            this["broccoli_unlocked"] = false

            this["onions_amount"] = "0"
            this["onions_multiplier"] = "1"
            this["onions_cost"] = "10000000"
            this["onions_unlocked"] = false

            this["potatoes_amount"] = "0"
            this["potatoes_multiplier"] = "1"
            this["potatoes_cost"] = "100000000"
            this["potatoes_unlocked"] = false
        }
    }

    companion object {
        private val log = logger<InitializeGameSystem>()
        private val preferences : Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }
        val POPULATION_CONFIGURATION = ResourceConfiguration(
            name = "population",
            amountOwned = BigInteger(preferences["totalPopulation", "10"])
        )
        val WHEAT_CONFIGURATION = ResourceConfiguration(
            name = "wheat",
            tier = 0,
            baseCost = BigDecimal("10"),
            baseValue = BigDecimal("1"),
            amountOwned = BigInteger(preferences["wheat_amount", "0"]),
            isUnlocked = preferences["wheat_unlocked", true]
        )
        val CORN_CONFIGURATION = ResourceConfiguration(
            name = "corn",
            tier = 1,
            baseCost = BigDecimal("100"),
            baseValue = BigDecimal("6.25"),
            amountOwned = BigInteger(preferences["corn_amount", "0"]),
            isUnlocked = preferences["corn_unlocked", false]
        )
        val LETTUCE_CONFIGURATION = ResourceConfiguration(
            name = "lettuce",
            tier = 2,
            baseCost = BigDecimal("1000"),
            baseValue = BigDecimal("13.61"),
            amountOwned = BigInteger(preferences["lettuce_amount", "0"]),
            isUnlocked = preferences["lettuce_unlocked", false]
        )
        val CARROTS_CONFIGURATION = ResourceConfiguration(
            name = "carrots",
            tier = 3,
            baseCost = BigDecimal("10000"),
            baseValue = BigDecimal("41.31"),
            amountOwned = BigInteger(preferences["carrots_amount", "0"]),
            isUnlocked = preferences["carrots_unlocked", false]
        )
        val TOMATOES_CONFIGURATION = ResourceConfiguration(
            name = "tomatoes",
            tier = 4,
            baseCost = BigDecimal("100000"),
            baseValue = BigDecimal("200.91"),
            amountOwned = BigInteger(preferences["tomatoes_amount", "0"]),
            isUnlocked = preferences["tomatoes_unlocked", false]
        )
        val BROCCOLI_CONFIGURATION = ResourceConfiguration(
            name = "broccoli",
            tier = 5,
            baseCost = BigDecimal("1000000"),
            baseValue = BigDecimal("1913.21"),
            amountOwned = BigInteger(preferences["broccoli_amount", "0"]),
            isUnlocked = preferences["broccoli_unlocked", false]
        )
        val ONIONS_CONFIGURATION = ResourceConfiguration(
            name = "onions",
            tier = 6,
            baseCost = BigDecimal("10000000"),
            baseValue = BigDecimal("47479.98"),
            amountOwned = BigInteger(preferences["onions_amount", "0"]),
            isUnlocked = preferences["onions_unlocked", false]
        )
        val POTATOES_CONFIGURATION = ResourceConfiguration(
            name = "potatoes",
            tier = 7,
            baseCost = BigDecimal("100000000"),
            baseValue = BigDecimal("4613468.88"),
            amountOwned = BigInteger(preferences["potatoes_amount", "0"]),
            isUnlocked = preferences["potatoes_unlocked", false]
        )
    }
}
