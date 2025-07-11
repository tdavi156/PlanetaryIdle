package com.github.jacks.planetaryIdle

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.github.jacks.planetaryIdle.events.QuitGameEvent
import com.github.jacks.planetaryIdle.screens.PlanetScreen
import com.github.jacks.planetaryIdle.ui.disposeSkin
import com.github.jacks.planetaryIdle.ui.loadSkin
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.preferences.flush

class PlanetaryIdle : KtxGame<KtxScreen>(), EventListener {

    private val batch : Batch by lazy { SpriteBatch() }
    val stage : Stage by lazy { Stage(ScreenViewport(), batch) }
    private val preferences : Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }
    private val resetOnStart : Boolean = true

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        if (resetOnStart) {
            preferences.flush {
                this.clear()
            }
        }
        loadSkin()

        stage.addListener(this)
        addScreen(PlanetScreen(this))
        setScreen<PlanetScreen>()
    }

    override fun render() {
        clearScreen(0f, 0f, 0f, 1f)
        currentScreen.render(Gdx.graphics.deltaTime)
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        super.dispose()
        stage.disposeSafely()
        batch.disposeSafely()
        disposeSkin()
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is QuitGameEvent -> {
                dispose()
                Gdx.app.exit()
            }
        }
        return true
    }

}
