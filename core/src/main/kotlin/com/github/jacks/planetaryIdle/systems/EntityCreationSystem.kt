package com.github.jacks.planetaryIdle.systems

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.jacks.planetaryIdle.components.CreateComponent
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.events.InitializeGameEvent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

@AllOf([CreateComponent::class])
class EntityCreationSystem(
    private val createComponents : ComponentMapper<CreateComponent>
) : EventListener, IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        with(createComponents[entity]) {
            val createEntity = world.entity {
                add<ResourceComponent> {
                    resourceName = c_resourceName
                    resourceTier = c_resourceTier
                    baseResourceCost = c_baseResourceCost
                    baseUpdateDuration = c_baseUpdateDuration
                    resourceValue = c_resourceValue
                    amountOwned = c_amountOwned
                }
            }
        }
        world.remove(entity)
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is InitializeGameEvent -> {
                world.entity {
                    add<CreateComponent> {
                        this.c_resourceName = "population"
                        this.c_amountOwned = 10
                    }
                }
                world.entity {
                    add<CreateComponent> {
                        this.c_resourceName = "wheat"
                        this.c_resourceTier = 1f
                        this.c_baseResourceCost = 10f
                        this.c_baseUpdateDuration = 1f
                        this.c_resourceValue = 1f
                    }
                }
                world.entity {
                    add<CreateComponent> {
                        this.c_resourceName = "corn"
                        this.c_resourceTier = 2f
                        this.c_baseResourceCost = 100f
                        this.c_baseUpdateDuration = 2.5f
                        this.c_resourceValue = 25f
                    }
                }
                world.entity {
                    add<CreateComponent> {
                        this.c_resourceName = "cabbage"
                        this.c_resourceTier = 3f
                        this.c_baseResourceCost = 1000f
                        this.c_baseUpdateDuration = 7f
                        this.c_resourceValue = 350f
                    }
                }
                world.entity {
                    add<CreateComponent> {
                        this.c_resourceName = "potatoes"
                        this.c_resourceTier = 4f
                        this.c_baseResourceCost = 10000f
                        this.c_baseUpdateDuration = 25f
                        this.c_resourceValue = 5000f
                    }
                }
            }
        }
        return true
    }
}
