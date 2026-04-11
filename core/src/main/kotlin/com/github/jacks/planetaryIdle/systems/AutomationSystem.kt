package com.github.jacks.planetaryIdle.systems

import com.github.jacks.planetaryIdle.PlanetaryIdle.Companion.FRAMES_PER_SECOND_FLOAT
import com.github.jacks.planetaryIdle.components.AchievementComponent
import com.github.jacks.planetaryIdle.ui.models.AutomationModel
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem

/**
 * Drives the automation tick loop.
 *
 * Uses [AchievementComponent] as a dummy family (same pattern as [ObservatorySystem])
 * so the system always has at least one entity. All actual work is in [onTick].
 *
 * [automationModel] is injected from the world; it holds all automation state and
 * handles the per-tick buying logic via [AutomationModel.performTick].
 */
@AllOf([AchievementComponent::class])
class AutomationSystem(
    private val automationModel: AutomationModel,
    @Suppress("UNUSED_PARAMETER") private val achievementComponents: ComponentMapper<AchievementComponent>,
) : IteratingSystem(interval = Fixed(1f / FRAMES_PER_SECOND_FLOAT)) {

    private var tickAccumulator: Int = 0
    private var totalTickCount: Int = 0

    override fun onTick() {
        if (!automationModel.automationUnlocked) return
        tickAccumulator++
        if (tickAccumulator >= automationModel.tickIntervalFrames) {
            tickAccumulator = 0
            automationModel.performTick(totalTickCount)
            totalTickCount++
        }
    }

    override fun onTickEntity(entity: Entity) {
        // Intentionally empty — all work is done in onTick().
    }
}
