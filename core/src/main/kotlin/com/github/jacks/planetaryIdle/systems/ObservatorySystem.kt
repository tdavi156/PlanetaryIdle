package com.github.jacks.planetaryIdle.systems

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.PlanetaryIdle.Companion.FRAMES_PER_SECOND_FLOAT
import com.github.jacks.planetaryIdle.components.AchievementComponent
import com.github.jacks.planetaryIdle.events.InsightTickEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem

/**
 * Fires [InsightTickEvent] once per second via [onTick].
 *
 * Uses [AchievementComponent] as a dummy family so it always has at least one
 * entity to iterate (the single achievement entity). The actual work is done
 * in [onTick], not [onTickEntity].
 */
@AllOf([AchievementComponent::class])
class ObservatorySystem(
    private val stage: Stage,
    @Suppress("UNUSED_PARAMETER") private val achievementComponents: ComponentMapper<AchievementComponent>,
) : IteratingSystem(interval = Fixed(1f / FRAMES_PER_SECOND_FLOAT)) {

    private var tickAccumulator: Int = 0

    override fun onTick() {
        tickAccumulator++
        if (tickAccumulator >= FRAMES_PER_SECOND_FLOAT.toInt()) {
            tickAccumulator = 0
            stage.fire(InsightTickEvent())
        }
    }

    override fun onTickEntity(entity: Entity) {
        // Intentionally empty — work is done in onTick().
    }
}
