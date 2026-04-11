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
import com.github.jacks.planetaryIdle.systems.AudioSystem
import com.github.jacks.planetaryIdle.systems.AutomationSystem
import com.github.jacks.planetaryIdle.systems.FloatingTextSystem
import com.github.jacks.planetaryIdle.systems.InitializeGameSystem
import com.github.jacks.planetaryIdle.systems.ObservatorySystem
import com.github.jacks.planetaryIdle.systems.RenderSystem
import com.github.jacks.planetaryIdle.systems.ResourceUpdateSystem
import com.github.jacks.planetaryIdle.systems.SettingsSystem
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.get
import com.github.jacks.planetaryIdle.ui.models.AchievementsModel
import com.github.jacks.planetaryIdle.ui.models.AutomationModel
import com.github.jacks.planetaryIdle.ui.models.BarnViewModel
import com.github.jacks.planetaryIdle.ui.models.CodexModel
import com.github.jacks.planetaryIdle.ui.models.FarmModel
import com.github.jacks.planetaryIdle.ui.models.HelpViewModel
import com.github.jacks.planetaryIdle.ui.models.KitchenViewModel
import com.github.jacks.planetaryIdle.ui.models.MenuModel
import com.github.jacks.planetaryIdle.ui.models.NotificationModel
import com.github.jacks.planetaryIdle.ui.models.ObservatoryViewModel
import com.github.jacks.planetaryIdle.ui.models.SettingsModel
import com.github.jacks.planetaryIdle.ui.views.BackgroundView
import com.github.jacks.planetaryIdle.ui.views.HeaderView
import com.github.jacks.planetaryIdle.ui.views.achievementsView
import com.github.jacks.planetaryIdle.ui.views.automationView
import com.github.jacks.planetaryIdle.ui.views.backgroundView
import com.github.jacks.planetaryIdle.ui.views.barnView
import com.github.jacks.planetaryIdle.ui.views.codexView
import com.github.jacks.planetaryIdle.ui.views.farmView
import com.github.jacks.planetaryIdle.ui.views.headerView
import com.github.jacks.planetaryIdle.ui.views.helpToastView
import com.github.jacks.planetaryIdle.ui.views.helpView
import com.github.jacks.planetaryIdle.ui.views.kitchenView
import com.github.jacks.planetaryIdle.ui.views.menuView
import com.github.jacks.planetaryIdle.ui.views.notificationView
import com.github.jacks.planetaryIdle.ui.views.observatoryView
import com.github.jacks.planetaryIdle.ui.views.settingsView
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

    // AutomationModel must be created before the world so it can be injected
    // into AutomationSystem. FarmModel and KitchenViewModel refs are wired in init{}.
    private val automationModel = AutomationModel(stage)

    private val entityWorld: World = world {
        injectables {
            add(stage)
            add(isometricMapRenderer)
            add(automationModel)
        }
        components {
            add<FloatingTextComponentListener>()
        }
        systems {
            add<InitializeGameSystem>()
            add<RenderSystem>()
            add<ResourceUpdateSystem>()
            add<FloatingTextSystem>()
            add<SettingsSystem>()
            add<AudioSystem>()
            add<ObservatorySystem>()
            add<AutomationSystem>()
        }
    }

    private val farmModel             = FarmModel(entityWorld, stage)
    private val kitchenViewModel      = KitchenViewModel(entityWorld, stage, farmModel)
    private val barnViewModel         = BarnViewModel(entityWorld, stage, farmModel)
    private val observatoryViewModel  = ObservatoryViewModel(entityWorld, stage, farmModel)
    private val helpViewModel         = HelpViewModel(stage)
    private val settingsModel         = SettingsModel(
        stage,
        entityWorld.system<SettingsSystem>(),
        entityWorld.system<AudioSystem>(),
    )

    private lateinit var bgView: BackgroundView

    init {
        // Wire cross-references that couldn't be set at construction time
        barnViewModel.kitchenViewModel = kitchenViewModel
        automationModel.farmModel = farmModel
        automationModel.kitchenViewModel = kitchenViewModel

        stage.actors {
            bgView = backgroundView()

            table {
                setFillParent(true)

                val achievementsModel = AchievementsModel(entityWorld, stage, farmModel, kitchenViewModel)
                var hdrView: HeaderView? = null
                hdrView = headerView(farmModel, achievementsModel, stage) { cell ->
                    cell.expandX().fillX().height(HEADER_HEIGHT).colspan(3)
                }
                row()

                image(skin[Drawables.BAR_BLACK_THIN]) { cell ->
                    cell.expandX().fillX().height(2f).colspan(3)
                }
                row()

                var fView:  com.badlogic.gdx.scenes.scene2d.ui.Table? = null
                var bView:  com.badlogic.gdx.scenes.scene2d.ui.Table? = null
                var kView:  com.badlogic.gdx.scenes.scene2d.ui.Table? = null
                var cView:  com.badlogic.gdx.scenes.scene2d.ui.Table? = null
                var aView:  com.badlogic.gdx.scenes.scene2d.ui.Table? = null
                var sView:  com.badlogic.gdx.scenes.scene2d.ui.Table? = null
                var oView:  com.badlogic.gdx.scenes.scene2d.ui.Table? = null
                var hlView: com.badlogic.gdx.scenes.scene2d.ui.Table? = null
                var autoView: com.badlogic.gdx.scenes.scene2d.ui.Table? = null

                val codexModel = CodexModel(kitchenViewModel)
                achievementsModel.observatoryViewModel = observatoryViewModel

                stack { stackCell ->
                    fView    = farmView(farmModel, kitchenViewModel, stage, hdrView!!.goldLabel) { isVisible = true }
                    bView    = barnView(barnViewModel, stage) { isVisible = false }
                    kView    = kitchenView(kitchenViewModel) { isVisible = false }
                    cView    = codexView(codexModel) { isVisible = false }
                    aView    = achievementsView(achievementsModel) { isVisible = false }
                    sView    = settingsView(settingsModel) { isVisible = false }
                    oView    = observatoryView(observatoryViewModel) { isVisible = false }
                    hlView   = helpView(helpViewModel) { isVisible = false }
                    autoView = automationView(automationModel, kitchenViewModel) { isVisible = false }
                    notificationView(NotificationModel(entityWorld, stage))
                    helpToastView(helpViewModel)
                    stackCell.expand().fill()
                }

                image(skin[Drawables.BAR_BLACK_THIN]) { cell ->
                    cell.fillY().width(2f)
                }

                menuView(
                    model            = MenuModel(stage),
                    helpViewModel    = helpViewModel,
                    stage            = stage,
                    farmView         = fView!!,
                    barnView         = bView!!,
                    kitchenView      = kView!!,
                    codexView        = cView!!,
                    achievementsView = aView!!,
                    settingsView     = sView!!,
                    observatoryView  = oView!!,
                    helpView         = hlView!!,
                    automationView   = autoView!!,
                ) { cell ->
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
        stage.addListener(observatoryViewModel)

        stage.fire(InitializeGameEvent())

        // Fire initial barn effects after all listeners are registered
        barnViewModel.fireInitialEffects()
        // Fire initial observatory effects after all listeners are registered
        observatoryViewModel.fireInitialEffects()

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
