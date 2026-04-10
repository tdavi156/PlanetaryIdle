package com.github.jacks.planetaryIdle.systems

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.PlanetaryIdle.Companion.FRAMES_PER_SECOND_FLOAT
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.components.ScoreResources
import com.github.jacks.planetaryIdle.components.UpgradeComponent
import com.github.jacks.planetaryIdle.events.BarnEffectsChangedEvent
import com.github.jacks.planetaryIdle.events.ObservatoryEffectsChangedEvent
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
    private val stage: Stage,
    private val resourceComponents: ComponentMapper<ResourceComponent>,
    private val upgradeComponents: ComponentMapper<UpgradeComponent>,
) : IteratingSystem(interval = Fixed(1 / FRAMES_PER_SECOND_FLOAT)), EventListener {

    private val upgradeEntities = world.family(allOf = arrayOf(UpgradeComponent::class))

    /** Speed multiplier from Barn's Improved Seeds upgrade. */
    private var barnSpeedMultiplier: BigDecimal = BigDecimal.ONE

    /** Speed multiplier from Observatory's Orbital Survey discovery. */
    private var observatorySpeedMultiplier: BigDecimal = BigDecimal.ONE

    override fun onTickEntity(entity: Entity) {
        val rscComp = resourceComponents[entity]
        if (rscComp.name == ScoreResources.GOLD_COINS.resourceName) return
        if (rscComp.amountOwned.toInt() < 1) return
        val tickCount = getAdjustedTickCount(rscComp.tickCount)
        if (rscComp.currentTicks < tickCount) rscComp.currentTicks++
        if (rscComp.currentTicks >= tickCount) {
            rscComp.currentTicks = 0
            stage.fire(ResourceUpdateEvent(rscComp))
        }
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is BarnEffectsChangedEvent -> barnSpeedMultiplier = event.speedMultiplier
            is ObservatoryEffectsChangedEvent -> observatorySpeedMultiplier = event.effects.cycleSpeedMultiplier
        }
        return false
    }

    private fun getAdjustedTickCount(tickCount: Int): Int {
        var soilMultiplier = BigDecimal.ONE
        upgradeEntities.forEach { entity ->
            soilMultiplier = soilMultiplier * upgradeComponents[entity].multiplier
        }
        val combined = soilMultiplier.multiply(barnSpeedMultiplier).multiply(observatorySpeedMultiplier)
        if (combined <= BigDecimal.ZERO) return tickCount
        return BigDecimal(tickCount).divide(combined, 2, RoundingMode.UP).toInt()
    }

    companion object {
        private val log = logger<ResourceUpdateSystem>()
    }
}
