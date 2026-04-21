package com.github.jacks.planetaryIdle.systems

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.rendering.IsometricMapRenderer
import com.github.quillraven.fleks.IntervalSystem
import ktx.log.logger

class RenderSystem(
    private val stage: Stage,
    private val mapRenderer: IsometricMapRenderer,
) : EventListener, IntervalSystem() {

    override fun onTick() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        with(stage) {
            viewport.apply()
            // Render isometric map before the stage so it appears behind all UI
            mapRenderer.render(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
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
