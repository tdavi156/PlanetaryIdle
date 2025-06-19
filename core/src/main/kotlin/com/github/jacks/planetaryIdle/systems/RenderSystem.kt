package com.github.jacks.planetaryIdle.systems

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.ImageComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.log.logger

@AllOf([ImageComponent::class])
class RenderSystem(
    private val stage : Stage
) : EventListener, IteratingSystem() {

    override fun onTick() {
        super.onTick()

        with(stage) {
            viewport.apply()
            act(deltaTime)
            draw()
        }
    }

    override fun onTickEntity(entity: Entity) = Unit

    override fun handle(event: Event?): Boolean {
        return true
    }

    companion object {
        private val log = logger<RenderSystem>()
    }
}
