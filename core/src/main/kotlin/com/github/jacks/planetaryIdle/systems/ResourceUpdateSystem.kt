package com.github.jacks.planetaryIdle.systems

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.AchievementComponent
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.components.UpgradeComponent
import com.github.jacks.planetaryIdle.events.ResourceUpdateEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import ktx.log.logger
import java.math.BigDecimal
import java.math.RoundingMode

//@AllOf([ResourceComponent::class])
class ResourceUpdateSystem(
    private val stage : Stage,
    private val resourceComponents : ComponentMapper<ResourceComponent>,
    private val upgradeComponents : ComponentMapper<UpgradeComponent>,
    private val achievementComponents : ComponentMapper<AchievementComponent>
) : IntervalSystem(interval = Fixed(1 / 20f)) {

    private val resourceEntities =  world.family(allOf = arrayOf(ResourceComponent::class))
    private val multiplierEntity = world.family(allOf = arrayOf(UpgradeComponent::class, AchievementComponent::class)).firstOrNull()

    override fun onTick() {
        var popGain = BigDecimal(0)
        val upgMultiplier = multiplierEntity?.let { upgradeComponents[it].multiplier.toBigDecimal() }
        val achMultiplier = multiplierEntity?.let { achievementComponents[it].multiplier.toBigDecimal() }
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            if (rscComp.name == "population") return@forEach
            if (rscComp.amountOwned.toInt() < 1) return@forEach
            popGain += (
                    rscComp.baseValue
                    * (rscComp.multiplier + (upgMultiplier ?: BigDecimal(0)) + achMultiplier)
                    * rscComp.amountOwned.toBigDecimal()
                ).divide(BigDecimal(20), 2, RoundingMode.HALF_UP)
        }
        stage.fire(ResourceUpdateEvent(popGain))
    }

    companion object {
        private val log = logger<ResourceUpdateSystem>()
    }
}
