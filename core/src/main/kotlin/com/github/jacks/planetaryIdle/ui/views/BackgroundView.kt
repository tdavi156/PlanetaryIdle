package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor

class BackgroundView(skin : Skin) : KTable, Table(skin) {

    init {
        setFillParent(true)

        if (!skin.has(PIXMAP_KEY, TextureRegionDrawable::class.java)) {
            skin.add(PIXMAP_KEY, TextureRegionDrawable(
                Texture(
                    Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
                        this.drawPixel(0, 0, Color.rgba8888(0.12f, 0.12f, 0.12f, 1f))
                    }
                )
            ))
        }

        background = skin.get(PIXMAP_KEY, TextureRegionDrawable::class.java)
    }

    companion object {
        private const val PIXMAP_KEY = "pauseTexturePixmap"
    }
}

@Scene2dDsl
fun <S> KWidget<S>.backgroundView(
    skin : Skin = Scene2DSkin.defaultSkin,
    init : BackgroundView.(S) -> Unit = { }
) : BackgroundView = actor(BackgroundView(skin), init)
