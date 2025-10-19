package com.github.jacks.planetaryIdle.systems

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.PlanetaryIdle.Companion.FRAMES_PER_SECOND_FLOAT
import com.github.jacks.planetaryIdle.components.AchievementComponent
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.components.UpgradeComponent
import com.github.jacks.planetaryIdle.events.ResourceUpdateEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import ktx.log.logger
import java.math.BigDecimal
import java.math.RoundingMode

@AllOf([ResourceComponent::class])
class ResourceUpdateSystem(
    private val stage : Stage,
    private val resourceComponents : ComponentMapper<ResourceComponent>,
) : IteratingSystem(interval = Fixed(1 / FRAMES_PER_SECOND_FLOAT)) {

    override fun onTickEntity(entity: Entity) {
        val rscComp = resourceComponents[entity]
        if (rscComp.name == "gold_coins") { return }
        if (rscComp.amountOwned.toInt() < 1) { return }
        if (rscComp.currentTicks < rscComp.tickCount) { rscComp.currentTicks++ }
        if (rscComp.currentTicks >= rscComp.tickCount) {
            // may need to set it to 1 here to account for the tick that we process on
            rscComp.amountSold = rscComp.amountSold.plus(BigDecimal("1"))
            rscComp.currentTicks = 0
            stage.fire(ResourceUpdateEvent(rscComp))
        }
    }

    companion object {
        private val log = logger<ResourceUpdateSystem>()
    }
}
