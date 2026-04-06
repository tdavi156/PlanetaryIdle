package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.jacks.planetaryIdle.events.ViewStateChangeEvent
import com.github.jacks.planetaryIdle.ui.ViewState
import ktx.log.logger
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor

class BackgroundView(skin: Skin) : Table(skin), KTable, EventListener {

    private val greyDrawable: TextureRegionDrawable
    private val barnDrawable: TextureRegionDrawable?
    private val kitchenDrawable: TextureRegionDrawable?

    init {
        setFillParent(true)

        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            drawPixel(0, 0, Color.rgba8888(0.23f, 0.23f, 0.23f, 1f))
        }
        greyDrawable = TextureRegionDrawable(Texture(pixmap))
        pixmap.dispose()

        barnDrawable    = tryLoadTexture("graphics/barn_background.png")
        kitchenDrawable = tryLoadTexture("graphics/kitchen_background.png")

        // Default: grey (farm view switches to null so the isometric map shows through)
        background = greyDrawable
    }

    override fun handle(event: Event): Boolean {
        if (event is ViewStateChangeEvent) {
            background = when (event.state) {
                ViewState.BARN    -> barnDrawable ?: greyDrawable
                ViewState.KITCHEN -> kitchenDrawable ?: greyDrawable
                // Farm: no background — isometric map renders underneath via RenderSystem
                ViewState.FARM    -> null
                else              -> greyDrawable
            }
        }
        return false
    }

    private fun tryLoadTexture(path: String): TextureRegionDrawable? {
        return if (Gdx.files.internal(path).exists()) {
            try {
                TextureRegionDrawable(Texture(Gdx.files.internal(path)))
            } catch (e: Exception) {
                log.debug { "Could not load background texture: $path" }
                null
            }
        } else {
            log.debug { "Background image not found: $path" }
            null
        }
    }

    companion object {
        private val log = logger<BackgroundView>()
    }
}

@Scene2dDsl
fun <S> KWidget<S>.backgroundView(
    skin: Skin = Scene2DSkin.defaultSkin,
    init: BackgroundView.(S) -> Unit = {},
): BackgroundView = actor(BackgroundView(skin), init)
