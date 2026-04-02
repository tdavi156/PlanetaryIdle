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
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.get
import com.github.jacks.planetaryIdle.ui.models.AchievementsModel
import com.github.jacks.planetaryIdle.ui.models.MenuModel
import com.github.jacks.planetaryIdle.ui.models.NotificationModel
import com.github.jacks.planetaryIdle.ui.models.PlanetModel
import com.github.jacks.planetaryIdle.ui.models.ShopModel
import com.github.jacks.planetaryIdle.ui.views.achievementsView
import com.github.jacks.planetaryIdle.ui.views.backgroundView
import com.github.jacks.planetaryIdle.ui.views.menuView
import com.github.jacks.planetaryIdle.ui.views.notificationView
import com.github.jacks.planetaryIdle.ui.views.planetView
import com.github.jacks.planetaryIdle.ui.views.shopView
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import ktx.app.KtxScreen
import ktx.log.logger
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actors
import ktx.scene2d.image
import ktx.scene2d.stack
import ktx.scene2d.table

class GameScreen(game : PlanetaryIdle) : KtxScreen {

    private val stage = game.stage
    private val skin = Scene2DSkin.defaultSkin

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
            // Background fills the entire stage behind everything
            backgroundView()

            // Root layout
            table {
                setFillParent(true)

                // Row 1: full-width header bar (spans all 3 columns)
                table { headerCell ->
                    headerCell.expandX().fillX().height(HEADER_HEIGHT).colspan(3)
                }
                row()

                // Row 2: horizontal separator under the header (spans all 3 columns)
                image(skin[Drawables.BAR_BLACK_THIN]) { cell ->
                    cell.expandX().fillX().height(2f).colspan(3)
                }
                row()

                // Row 3: content stack | vertical divider | menu column
                var pView: com.badlogic.gdx.scenes.scene2d.ui.Table? = null
                var sView: com.badlogic.gdx.scenes.scene2d.ui.Table? = null
                var aView: com.badlogic.gdx.scenes.scene2d.ui.Table? = null

                stack { stackCell ->
                    pView = planetView(PlanetModel(entityWorld, stage)) { isVisible = true }
                    sView = shopView(ShopModel(entityWorld, stage)) { isVisible = false }
                    aView = achievementsView(AchievementsModel(entityWorld, stage)) { isVisible = false }
                    notificationView(NotificationModel(entityWorld, stage))
                    stackCell.expand().fill()
                }

                image(skin[Drawables.BAR_BLACK_THIN]) { cell ->
                    cell.fillY().width(2f)
                }

                menuView(MenuModel(stage), pView!!, sView!!, aView!!) { cell ->
                    cell.top().fillY().width(MENU_WIDTH)
                }
            }
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
        val log = logger<PlanetaryIdle>()
        const val MENU_WIDTH = 204f
        const val HEADER_HEIGHT = 44f
    }
}
