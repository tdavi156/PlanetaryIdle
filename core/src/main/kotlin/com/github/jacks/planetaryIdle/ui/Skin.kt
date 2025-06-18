package com.github.jacks.planetaryIdle.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import ktx.assets.disposeSafely
import ktx.scene2d.Scene2DSkin
import ktx.style.SkinDsl
import ktx.style.label
import ktx.style.set
import ktx.style.skin
import ktx.style.textButton

enum class Drawables(
    val atlasKey : String
) {
    BUTTON_1_UP("button_1_up"),
    BUTTON_1_OVER("button_1_over"),
    BUTTON_1_DOWN("button_1_down"),
    BUTTON_1_DISABLED("button_1_disabled");
}

enum class Labels {
    TITLE,
    LARGE;

    val skinKey = this.name.lowercase()
}

enum class Fonts(
    val atlasRegionKey : String,
    val scaling : Float
) {
    DEFAULT("default", 1f),
    BIG("default", 2f);

    val skinKey = "Font_${this.name.lowercase()}"
    val fontPath = "assets/fonts/${this.atlasRegionKey}.fnt"
}

enum class Buttons {
    TEXT_BUTTON,
    IMAGE_BUTTON;

    val skinKey = this.name.lowercase()
}

operator fun Skin.get(drawable : Drawables) : Drawable = this.getDrawable(drawable.atlasKey)
operator fun Skin.get(font : Fonts) : BitmapFont = this.getFont(font.skinKey)

fun loadSkin() {
    Scene2DSkin.defaultSkin = skin(TextureAtlas("assets/ui/ui.atlas")) { skin ->
        loadFonts(skin)
        loadLabels(skin)
        loadButtons(skin)
    }
}

private fun loadFonts(skin: Skin) {
    Fonts.entries.forEach { font ->
        skin[font.skinKey] = BitmapFont(Gdx.files.internal(font.fontPath), skin.getRegion(font.atlasRegionKey)).apply {
            data.setScale(font.scaling)
            data.markupEnabled = true
        }
    }
}

private fun @SkinDsl Skin.loadLabels(skin : Skin) {
    label(Labels.TITLE.skinKey) {
        font = skin[Fonts.BIG]
        fontColor = Color.WHITE
        background = skin[Drawables.BUTTON_1_UP].apply {
            leftWidth = 3f
            rightWidth = 3f
            topHeight = 3f
            bottomHeight = 3f
        }
    }
    label(Labels.LARGE.skinKey) {
        font = skin[Fonts.BIG]
        fontColor = Color.WHITE
    }
}

private fun @SkinDsl Skin.loadButtons(skin: Skin) {
    textButton(Buttons.TEXT_BUTTON.skinKey) {
        font = skin[Fonts.DEFAULT]
    }
}

fun disposeSkin() {
    Scene2DSkin.defaultSkin.disposeSafely()
}
