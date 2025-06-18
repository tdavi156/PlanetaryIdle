package com.github.jacks.planetaryIdle.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys.W
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.World
import ktx.app.KtxInputAdapter
import ktx.log.logger

fun gdxInputProcessor(processor : InputProcessor) {
    val currentProcessor = Gdx.input.inputProcessor
    if (currentProcessor == null) {
        Gdx.input.inputProcessor = processor
    } else {
        if (currentProcessor is InputMultiplexer) {
            if (processor !in currentProcessor.processors) {
                currentProcessor.addProcessor(processor)
            }
        } else {
            Gdx.input.inputProcessor = InputMultiplexer(currentProcessor, processor)
        }
    }
}

class KeyboardInputProcessor(
    private val world : World,
    private val stage : Stage,
) : KtxInputAdapter {

    init {
        gdxInputProcessor(this)
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            W -> {
                log.debug { "W key is pressed" }
            }
        }
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        return super.keyUp(keycode)
    }

    companion object {
        private val log = logger<KeyboardInputProcessor>()
    }
}
