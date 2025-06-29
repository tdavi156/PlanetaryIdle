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
import java.math.BigInteger

@AllOf([ResourceComponent::class])
class ResourceUpdateSystem(
    private val stage : Stage,
    private val resourceComponents : ComponentMapper<ResourceComponent>
) : IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        val rscComp  = resourceComponents[entity]

        if (rscComp.baseUpdateDuration < -1f || rscComp.amountOwned == BigInteger("0")) {
            return
        }

        if (rscComp.currentUpdateDuration <= 0f) {
            rscComp.currentUpdateDuration = rscComp.baseUpdateDuration
            log.debug { "${rscComp.name} fired resource update event in ${rscComp.baseUpdateDuration} seconds." }
            stage.fire(ResourceUpdateEvent(entity))
        }

        rscComp.currentUpdateDuration = (rscComp.currentUpdateDuration - deltaTime).coerceAtLeast(0f)
    }

    companion object {
        private val log = logger<ResourceUpdateSystem>()
    }
}
