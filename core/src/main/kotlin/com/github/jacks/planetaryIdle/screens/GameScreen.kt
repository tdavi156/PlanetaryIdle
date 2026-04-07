package com.github.jacks.planetaryIdle.screens

import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.jacks.planetaryIdle.PlanetaryIdle
import com.github.jacks.planetaryIdle.components.FloatingTextComponent
import com.github.jacks.planetaryIdle.components.FloatingTextComponentListener
import com.github.jacks.planetaryIdle.events.InitializeGameEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.jacks.planetaryIdle.input.KeyboardInputProcessor
import com.github.jacks.planetaryIdle.input.gdxInputProcessor
import com.github.jacks.planetaryIdle.rendering.IsometricMapRenderer
import com.github.jacks.planetaryIdle.systems.FloatingTextSystem
import com.github.jacks.planetaryIdle.systems.InitializeGameSystem
import com.github.jacks.planetaryIdle.systems.RenderSystem
import com.github.jacks.planetaryIdle.systems.ResourceUpdateSystem
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.get
import com.github.jacks.planetaryIdle.ui.models.AchievementsModel
import com.github.jacks.planetaryIdle.ui.models.BarnViewModel
import com.github.jacks.planetaryIdle.ui.models.FarmModel
import com.github.jacks.planetaryIdle.ui.models.KitchenViewModel
import com.github.jacks.planetaryIdle.ui.models.MenuModel
import com.github.jacks.planetaryIdle.ui.models.NotificationModel
import com.github.jacks.planetaryIdle.ui.views.BackgroundView
import com.github.jacks.planetaryIdle.ui.views.HeaderView
import com.github.jacks.planetaryIdle.ui.views.achievementsView
import com.github.jacks.planetaryIdle.ui.views.backgroundView
import com.github.jacks.planetaryIdle.ui.views.barnView
import com.github.jacks.planetaryIdle.ui.views.farmView
import com.github.jacks.planetaryIdle.ui.views.headerView
import com.github.jacks.planetaryIdle.ui.views.kitchenView
import com.github.jacks.planetaryIdle.ui.views.menuView
import com.github.jacks.planetaryIdle.ui.views.notificationView
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import ktx.app.KtxScreen
import ktx.log.logger
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actors
import ktx.scene2d.image
import ktx.scene2d.stack
import ktx.scene2d.table

class GameScreen(game: PlanetaryIdle) : KtxScreen {

    private val stage = game.stage
    private val skin = Scene2DSkin.defaultSkin

    private val isometricMapRenderer = IsometricMapRenderer()

    private val entityWorld: World = world {
        injectables {
            add(stage)
            add(isometricMapRenderer)
        }
        components {
            add<FloatingTextComponentListener>()
        }
        systems {
            add<InitializeGameSystem>()
            add<RenderSystem>()
            add<ResourceUpdateSystem>()
            add<FloatingTextSystem>()
        }
    }

    private val farmModel      = FarmModel(entityWorld, stage)
    private val kitchenViewModel = KitchenViewModel(entityWorld, stage, farmModel)
    private val barnViewModel  = BarnViewModel(entityWorld, stage, farmModel)

    private lateinit var bgView: BackgroundView

    init {
        // Wire cross-references that couldn't be set at construction time
        barnViewModel.kitchenViewModel = kitchenViewModel

        stage.actors {
            bgView = backgroundView()

            table {
                setFillParent(true)

                var hdrView: HeaderView? = null
                hdrView = headerView(farmModel) { cell ->
                    cell.expandX().fillX().height(HEADER_HEIGHT).colspan(3)
                }
                row()

                image(skin[Drawables.BAR_BLACK_THIN]) { cell ->
                    cell.expandX().fillX().height(2f).colspan(3)
                }
                row()

                var fView: com.badlogic.gdx.scenes.scene2d.ui.Table? = null
                var bView: com.badlogic.gdx.scenes.scene2d.ui.Table? = null
                var kView: com.badlogic.gdx.scenes.scene2d.ui.Table? = null
                var aView: com.badlogic.gdx.scenes.scene2d.ui.Table? = null

                stack { stackCell ->
                    fView = farmView(farmModel, kitchenViewModel, stage, hdrView!!.goldLabel) { isVisible = true }
                    bView = barnView(barnViewModel, stage) { isVisible = false }
                    kView = kitchenView(kitchenViewModel) { isVisible = false }
                    aView = achievementsView(AchievementsModel(entityWorld, stage)) { isVisible = false }
                    notificationView(NotificationModel(entityWorld, stage))
                    stackCell.expand().fill()
                }

                image(skin[Drawables.BAR_BLACK_THIN]) { cell ->
                    cell.fillY().width(2f)
                }

                menuView(MenuModel(stage), fView!!, bView!!, kView!!, aView!!) { cell ->
                    cell.top().fillY().width(MENU_WIDTH)
                }
            }
        }
        stage.isDebugAll = false
    }

    override fun show() {
        log.debug { "GameScreen is active" }

        entityWorld.systems.forEach { system ->
            if (system is EventListener) {
                stage.addListener(system)
            }
        }

        stage.addListener(bgView)
        stage.addListener(isometricMapRenderer)

        stage.fire(InitializeGameEvent())

        // Fire initial barn effects after all listeners are registered
        barnViewModel.fireInitialEffects()

        KeyboardInputProcessor(entityWorld, stage)
        gdxInputProcessor(stage)
    }

    override fun render(delta: Float) {
        val deltaTime = delta.coerceAtMost(0.25f)
        entityWorld.update(deltaTime)
    }

    override fun dispose() {
        entityWorld.dispose()
        isometricMapRenderer.dispose()
    }

    companion object {
        val log = logger<PlanetaryIdle>()
        const val MENU_WIDTH    = 204f
        const val HEADER_HEIGHT = 44f
    }
}
