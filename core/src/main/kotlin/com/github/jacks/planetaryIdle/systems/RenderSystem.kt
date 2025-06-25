package com.github.jacks.planetaryIdle.systems

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.IteratingSystem
import ktx.log.logger

class RenderSystem(
    private val stage : Stage
) : EventListener, IntervalSystem() {

    override fun onTick() {
        with(stage) {
            viewport.apply()
            act(deltaTime)
            draw()
        }
    }

    override fun handle(event: Event): Boolean {
        return true
    }

    companion object {
        private val log = logger<RenderSystem>()
    }
}
