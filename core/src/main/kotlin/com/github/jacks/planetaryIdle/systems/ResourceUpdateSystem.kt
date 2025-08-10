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
    private val upgradeComponents : ComponentMapper<UpgradeComponent>,
    private val achievementComponents : ComponentMapper<AchievementComponent>
) : IteratingSystem(interval = Fixed(1 / FRAMES_PER_SECOND_FLOAT)) {

    private val resourceEntities =  world.family(allOf = arrayOf(ResourceComponent::class))
    //private val multiplierEntity = world.family(allOf = arrayOf(UpgradeComponent::class, AchievementComponent::class)).firstOrNull()

    /*
    override fun onTick() {
        var goldCoins = BigDecimal(0)
        //val upgMultiplier = multiplierEntity?.let { upgradeComponents[it].multiplier.toBigDecimal() }
        //val achMultiplier = multiplierEntity?.let { achievementComponents[it].multiplier.toBigDecimal() }
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            if (rscComp.name == "gold_coins") return@forEach
            if (rscComp.amountOwned.toInt() < 1) return@forEach
            goldCoins += (rscComp.value).divide(BigDecimal(20), 2, RoundingMode.HALF_UP)
        }
        stage.fire(ResourceUpdateEvent(goldCoins))
    }
     */

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
