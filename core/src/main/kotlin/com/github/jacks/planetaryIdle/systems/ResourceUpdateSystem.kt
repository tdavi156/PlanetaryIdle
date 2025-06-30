package com.github.jacks.planetaryIdle.systems

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.events.ResourceUpdateEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import ktx.log.logger
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@AllOf([ResourceComponent::class])
class ResourceUpdateSystem(
    private val stage : Stage,
    private val resourceComponents : ComponentMapper<ResourceComponent>,
) : IntervalSystem(interval = Fixed(1 / 15f)) {

    val resourceEntities =  world.family(allOf = arrayOf(ResourceComponent::class))

//    override fun onTickEntity(entity: Entity) {
//        val rscComp  = resourceComponents[entity]
//
//        if (rscComp.baseUpdateDuration < -1f || rscComp.amountOwned == BigInteger("0")) {
//            return
//        }
//
//        if (rscComp.currentUpdateDuration <= 0f) {
//            rscComp.currentUpdateDuration = rscComp.baseUpdateDuration
//            stage.fire(ResourceUpdateEvent(entity))
//        }
//
//        rscComp.currentUpdateDuration = (rscComp.currentUpdateDuration - deltaTime).coerceAtLeast(0f)
//    }

    override fun onTick() {
        var popGain = BigDecimal(0)
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            if (rscComp.name == "population") return@forEach
            if (rscComp.amountOwned.toInt() < 1) return@forEach
            popGain += ((rscComp.baseValue * rscComp.multiplier * rscComp.amountOwned.toBigDecimal()).divide(BigDecimal(15), 2, RoundingMode.HALF_UP))
        }
        stage.fire(ResourceUpdateEvent(popGain))
    }

    companion object {
        private val log = logger<ResourceUpdateSystem>()
    }
}
