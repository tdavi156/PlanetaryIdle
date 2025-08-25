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
import com.github.jacks.planetaryIdle.ui.models.AchievementsModel
import com.github.jacks.planetaryIdle.ui.models.AutomationModel
import com.github.jacks.planetaryIdle.ui.models.ChallengesModel
import com.github.jacks.planetaryIdle.ui.models.GalaxyModel
import com.github.jacks.planetaryIdle.ui.models.MenuModel
import com.github.jacks.planetaryIdle.ui.models.PlanetModel
import com.github.jacks.planetaryIdle.ui.models.SettingsModel
import com.github.jacks.planetaryIdle.ui.models.ShopModel
import com.github.jacks.planetaryIdle.ui.models.StatisticsModel
import com.github.jacks.planetaryIdle.ui.views.achievementsView
import com.github.jacks.planetaryIdle.ui.views.automationView
import com.github.jacks.planetaryIdle.ui.views.backgroundView
import com.github.jacks.planetaryIdle.ui.views.challengesView
import com.github.jacks.planetaryIdle.ui.views.galaxyView
import com.github.jacks.planetaryIdle.ui.views.menuView
import com.github.jacks.planetaryIdle.ui.views.planetView
import com.github.jacks.planetaryIdle.ui.views.settingsView
import com.github.jacks.planetaryIdle.ui.views.shopView
import com.github.jacks.planetaryIdle.ui.views.statisticsView
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import ktx.app.KtxScreen
import ktx.log.logger
import ktx.scene2d.actors

class GameScreen(game : PlanetaryIdle) : KtxScreen {

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

            // background, actor.get(0)
            backgroundView()

            // planetView, actor.get(1), default view on game start
            planetView(PlanetModel(entityWorld, stage)) {
                //debugAll()
                isVisible = true
            }

            // galaxyView, actor.get(2)
            galaxyView(GalaxyModel(entityWorld, stage)) { isVisible = false }

            // automationView, actor.get(3)
            automationView(AutomationModel(entityWorld, stage)) { isVisible = false }

            // challengesView, actor.get(4)
            challengesView(ChallengesModel(entityWorld, stage)) { isVisible = false }

            // shopView, actor.get(5)
            shopView(ShopModel(entityWorld, stage)) { isVisible = false }

            // achievementsView, actor.get(6)
            achievementsView(AchievementsModel(entityWorld, stage)) { isVisible = false }

            // statisticsView, actor.get(7)
            statisticsView(StatisticsModel(entityWorld, stage)) { isVisible = false }

            // settingsView, actor.get(8)
            settingsView(SettingsModel(entityWorld, stage)) { isVisible = false }

            // right side menu (always present)
            menuView(MenuModel(stage))
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
        const val ACTOR_COUNT = 8
    }
}
