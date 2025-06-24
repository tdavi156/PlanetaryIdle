package com.github.jacks.planetaryIdle.systems

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.jacks.planetaryIdle.components.ConfigurationComponent
import com.github.jacks.planetaryIdle.components.ConfigurationType.*
import com.github.jacks.planetaryIdle.components.PlanetResources
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.components.ResourceConfiguration
import com.github.jacks.planetaryIdle.events.InitializeGameEvent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.log.logger

@AllOf([ConfigurationComponent::class])
class EntityCreationSystem(
    private val configurationComponents : ComponentMapper<ConfigurationComponent>
) : EventListener, IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        with(configurationComponents[entity]) {
            when(configurationType) {
                POPULATION -> {
                    val config = POPULATION_CONFIGURATION
                    world.entity {
                        add<ResourceComponent> {
                            name = config.name
                            amountOwned = config.baseAmountOwned
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

    companion object {
        private val log = logger<EntityCreationSystem>()
        val POPULATION_CONFIGURATION = ResourceConfiguration(
            name = "population",
            baseAmountOwned = 10
        )
        val WHEAT_CONFIGURATION = ResourceConfiguration(
            name = "wheat",
            tier = 1f,
            baseCost = 10f,
            baseValue = 1f,
            baseUpdateDuration = 1f,
            isUnlocked = true
        )
        val CORN_CONFIGURATION = ResourceConfiguration(
            name = "corn",
            tier = 2f,
            baseCost = 100f,
            baseValue = 25f,
            baseUpdateDuration = 3f
        )
        val CABBAGE_CONFIGURATION = ResourceConfiguration(
            name = "cabbage",
            tier = 3f,
            baseCost = 1000f,
            baseValue = 350f,
            baseUpdateDuration = 7.5f
        )
        val POTATOES_CONFIGURATION = ResourceConfiguration(
            name = "potatoes",
            tier = 4f,
            baseCost = 10000f,
            baseValue = 4250f,
            baseUpdateDuration = 25f
        )
    }
}
