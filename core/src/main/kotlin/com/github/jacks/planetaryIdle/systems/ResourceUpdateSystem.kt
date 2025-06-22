package com.github.jacks.planetaryIdle.systems

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.events.ResourceUpdateEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.log.logger

@AllOf([ResourceComponent::class])
class ResourceUpdateSystem(
    private val stage : Stage,
    private val resourceComponents : ComponentMapper<ResourceComponent>
) : IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        val resourceComponent  = resourceComponents[entity]

        if (resourceComponent.baseUpdateDuration < -9f || resourceComponent.amountOwned == 0) {
            return
        }

        if (resourceComponent.currentUpdateDuration <= 0f) {
            resourceComponent.currentUpdateDuration = resourceComponent.baseUpdateDuration
            log.debug { "${resourceComponent.resourceName} fired resource update event" }
            stage.fire(ResourceUpdateEvent(entity))
        }

        resourceComponent.currentUpdateDuration -= deltaTime
    }

    companion object {
        private val log = logger<ResourceUpdateSystem>()
    }
}
