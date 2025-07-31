package com.github.jacks.planetaryIdle.screens

import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.jacks.planetaryIdle.PlanetaryIdle
import com.github.jacks.planetaryIdle.events.InitializeGameEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.jacks.planetaryIdle.input.KeyboardInputProcessor
import com.github.jacks.planetaryIdle.input.gdxInputProcessor
import com.github.jacks.planetaryIdle.systems.InitializeGameSystem
import com.github.jacks.planetaryIdle.systems.RenderSystem
import com.github.jacks.planetaryIdle.systems.ResourceUpdateSystem
import com.github.jacks.planetaryIdle.ui.models.MenuModel
import com.github.jacks.planetaryIdle.ui.models.PlanetModel
import com.github.jacks.planetaryIdle.ui.views.backgroundView
import com.github.jacks.planetaryIdle.ui.views.menuView
import com.github.jacks.planetaryIdle.ui.views.planetView
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import ktx.app.KtxScreen
import ktx.log.logger
import ktx.scene2d.actors

class PlanetScreen(game : PlanetaryIdle) : KtxScreen {

    private val stage = game.stage

    private val entityWorld : World = world {
        injectables {
            add(stage)
        }

        components {
            // add<ComponentListenerName>
        }

        systems {
            add<InitializeGameSystem>()
            add<RenderSystem>()
            add<ResourceUpdateSystem>()
        }
    }

    init {
        stage.actors {
            log.debug { "Stage is initialized." }
            backgroundView()
            menuView(MenuModel(stage))
            planetView(PlanetModel(entityWorld, stage))
        }
        stage.isDebugAll = false
    }

    override fun show() {
        log.debug { "PlanetScreen is active" }

        entityWorld.systems.forEach { system ->
            if (system is EventListener) {
                stage.addListener(system)
            }
        }

        stage.fire(InitializeGameEvent())
        KeyboardInputProcessor(entityWorld, stage)
        gdxInputProcessor(stage)
    }

    override fun render(delta: Float) {
        val deltaTime = delta.coerceAtMost(0.25f)
        entityWorld.update(deltaTime)
    }

    override fun dispose() {
        entityWorld.dispose()
    }

    companion object {
        private val log = logger<PlanetaryIdle>()
    }
}
