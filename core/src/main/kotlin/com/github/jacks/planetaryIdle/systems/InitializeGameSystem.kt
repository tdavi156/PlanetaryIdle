package com.github.jacks.planetaryIdle.systems

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.jacks.planetaryIdle.components.ConfigurationComponent
import com.github.jacks.planetaryIdle.components.ConfigurationType.*
import com.github.jacks.planetaryIdle.components.PlanetResources
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.components.ResourceConfiguration
import com.github.jacks.planetaryIdle.events.InitializeGameEvent
import com.github.jacks.planetaryIdle.events.LoadGameEvent
import com.github.jacks.planetaryIdle.systems.InitializeGameSystem.Companion.preferences
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set

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
                            baseUpdateDuration = config.baseUpdateDuration
                            amountOwned = config.amountOwned
                            isUnlocked = config.isUnlocked
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
            PlanetResources.CABBAGE.resourceName -> CABBAGE_CONFIGURATION
            PlanetResources.POTATOES.resourceName -> POTATOES_CONFIGURATION
            else -> ResourceConfiguration()
        }
    }

    private fun setupPreferences() {
        preferences.flush {
            this["isGameInitialized"] = true

            this["totalPopulation"] = 10
            this["availablePopulation"] = 10f
            this["populationGainRate"] = 0f
            this["buyAmount"] = 1f

            this["wheat_amount"] = 0
            this["wheat_multiplier"] = 1f
            this["wheat_cost"] = 10f
            this["wheat_updateDuration"] = 1f
            this["wheat_unlocked"] = true

            this["corn_amount"] = 0
            this["corn_multiplier"] = 1f
            this["corn_cost"] = 100f
            this["corn_updateDuration"] = 3f
            this["corn_unlocked"] = false

            this["cabbage_Amount"] = 0
            this["cabbage_multiplier"] = 1f
            this["cabbage_cost"] = 1_000f
            this["cabbage_updateDuration"] = 7.5f
            this["cabbage_unlocked"] = false

            this["potatoes_amount"] = 0
            this["potatoes_multiplier"] = 1f
            this["potatoes_cost"] = 10_000f
            this["potatoes_updateDuration"] = 25f
            this["potatoes_unlocked"] = false
        }
    }

    companion object {
        private val log = logger<InitializeGameSystem>()
        private val preferences : Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }
        val POPULATION_CONFIGURATION = ResourceConfiguration(
            name = "population",
            amountOwned = preferences["totalPopulation", 10]
        )
        val WHEAT_CONFIGURATION = ResourceConfiguration(
            name = "wheat",
            tier = 1f,
            baseCost = 10f,
            baseValue = 1f,
            baseUpdateDuration = 1f,
            currentUpdateDuration = preferences["wheat_updateDuration", 1f],
            amountOwned = preferences["wheat_amount", 0],
            isUnlocked = preferences["wheat_unlocked", true]
        )
        val CORN_CONFIGURATION = ResourceConfiguration(
            name = "corn",
            tier = 2f,
            baseCost = 100f,
            baseValue = 25f,
            baseUpdateDuration = 3f,
            currentUpdateDuration = preferences["corn_updateDuration", 3f],
            amountOwned = preferences["corn_amount", 0],
            isUnlocked = preferences["corn_unlocked", false]
        )
        val CABBAGE_CONFIGURATION = ResourceConfiguration(
            name = "cabbage",
            tier = 3f,
            baseCost = 1_000f,
            baseValue = 350f,
            baseUpdateDuration = 7.5f,
            currentUpdateDuration = preferences["cabbage_updateDuration", 7.5f],
            amountOwned = preferences["cabbage_amount", 0],
            isUnlocked = preferences["cabbage_unlocked", false]
        )
        val POTATOES_CONFIGURATION = ResourceConfiguration(
            name = "potatoes",
            tier = 4f,
            baseCost = 10_000f,
            baseValue = 4_250f,
            baseUpdateDuration = 25f,
            currentUpdateDuration = preferences["potatoes_updateDuration", 25f],
            amountOwned = preferences["potatoes_amount", 0],
            isUnlocked = preferences["potatoes_unlocked", false]
        )
    }
}
