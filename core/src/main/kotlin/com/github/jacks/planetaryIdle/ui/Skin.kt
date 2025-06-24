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
    BAR_GREEN_THIN("bar_green_thin"),
    BAR_GREEN_THICK("bar_green_thick"),
    BAR_GREY_THICK("bar_grey_thick"),

    BUTTON_BLUE_UP("button_blue_up"),
    BUTTON_BLUE_DOWN("button_blue_down"),
    BUTTON_BLUE_OVER("button_blue_over"),
    BUTTON_GREEN_UP("button_green_up"),
    BUTTON_GREEN_DOWN("button_green_down"),
    BUTTON_GREEN_OVER("button_green_over"),
    BUTTON_RED_UP("button_red_up"),
    BUTTON_RED_OVER("button_red_over"),
}

enum class Labels {
    SMALL,
    SMALL_BOLD,
    MEDIUM,
    MEDIUM_BOLD,
    DEFAULT,
    DEFAULT_BOLD,
    LARGE,
    LARGE_BOLD,
    X_LARGE,
    X_LARGE_BOLD,
    TITLE,
    TITLE_BOLD;

    val skinKey = this.name.lowercase()
}

enum class Fonts(
    val atlasRegionKey : String,
    val scaling : Float
) {
    SMALL("default_16pt", 1f),
    SMALL_BOLD("default_16pt", 1f),
    MEDIUM("default_20pt", 1f),
    MEDIUM_BOLD("default_bold_20pt", 1f),
    DEFAULT("default_24pt", 1f),
    DEFAULT_BOLD("default_bold_24pt", 1f),
    LARGE("default_28pt", 1f),
    LARGE_BOLD("default_bold_28pt", 1f),
    X_LARGE("default_32pt", 1f),
    X_LARGE_BOLD("default_bold_32pt", 1f),
    XX_LARGE("default_40pt", 1f),
    XX_LARGE_BOLD("default_bold_40pt", 1f);

    val skinKey = "Font_${this.name.lowercase()}"
    val fontPath = "fonts/${this.atlasRegionKey}.fnt"
}

enum class Buttons {
    BLUE_TEXT_BUTTON_MEDIUM,
    GREEN_TEXT_BUTTON_MEDIUM,
    RED_TEXT_BUTTON_MEDIUM,
    BLUE_TEXT_BUTTON_DEFAULT,
    GREEN_TEXT_BUTTON_DEFAULT,
    RED_TEXT_BUTTON_DEFAULT;

    val skinKey = this.name.lowercase()
}

operator fun Skin.get(drawable : Drawables) : Drawable = this.getDrawable(drawable.atlasKey)
operator fun Skin.get(font : Fonts) : BitmapFont = this.getFont(font.skinKey)

fun loadSkin() {
    Scene2DSkin.defaultSkin = skin(TextureAtlas("ui/ui.atlas")) { skin ->
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
    label(Labels.SMALL.skinKey) {
        font = skin[Fonts.SMALL]
        fontColor = Color.WHITE
    }
    label(Labels.SMALL_BOLD.skinKey) {
        font = skin[Fonts.SMALL_BOLD]
        fontColor = Color.WHITE
    }
    label(Labels.MEDIUM.skinKey) {
        font = skin[Fonts.MEDIUM]
        fontColor = Color.WHITE
    }
    label(Labels.MEDIUM_BOLD.skinKey) {
        font = skin[Fonts.MEDIUM_BOLD]
        fontColor = Color.WHITE
    }
    label(Labels.DEFAULT.skinKey) {
        font = skin[Fonts.DEFAULT]
        fontColor = Color.WHITE
    }
    label(Labels.DEFAULT_BOLD.skinKey) {
        font = skin[Fonts.DEFAULT_BOLD]
        fontColor = Color.WHITE
    }
    label(Labels.LARGE.skinKey) {
        font = skin[Fonts.LARGE]
        fontColor = Color.WHITE
    }
    label(Labels.LARGE_BOLD.skinKey) {
        font = skin[Fonts.LARGE_BOLD]
        fontColor = Color.WHITE
    }
    label(Labels.X_LARGE.skinKey) {
        font = skin[Fonts.X_LARGE]
        fontColor = Color.WHITE
    }
    label(Labels.X_LARGE_BOLD.skinKey) {
        font = skin[Fonts.X_LARGE_BOLD]
        fontColor = Color.WHITE
    }
    label(Labels.TITLE.skinKey) {
        font = skin[Fonts.XX_LARGE]
        fontColor = Color.WHITE
        background = skin[Drawables.BUTTON_BLUE_UP].apply {
            leftWidth = 3f
            rightWidth = 3f
            topHeight = 3f
            bottomHeight = 3f
        }
    }
    label(Labels.TITLE_BOLD.skinKey) {
        font = skin[Fonts.XX_LARGE_BOLD]
        fontColor = Color.WHITE
        background = skin[Drawables.BUTTON_BLUE_UP].apply {
            leftWidth = 3f
            rightWidth = 3f
            topHeight = 3f
            bottomHeight = 3f
        }
    }
}

private fun @SkinDsl Skin.loadButtons(skin: Skin) {
    textButton(Buttons.BLUE_TEXT_BUTTON_MEDIUM.skinKey) {
        up = skin[Drawables.BUTTON_BLUE_UP]
        down = skin[Drawables.BUTTON_BLUE_DOWN]
        over = skin[Drawables.BUTTON_BLUE_OVER]
        disabled = skin[Drawables.BUTTON_RED_UP]
        font = skin[Fonts.MEDIUM]
        fontColor = Color(1f, 1f, 1f, 1f)
        overFontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.GREEN_TEXT_BUTTON_MEDIUM.skinKey) {
        up = skin[Drawables.BUTTON_GREEN_UP]
        down = skin[Drawables.BUTTON_GREEN_DOWN]
        over = skin[Drawables.BUTTON_GREEN_OVER]
        disabled = skin[Drawables.BUTTON_RED_UP]
        font = skin[Fonts.MEDIUM]
        fontColor = Color(1f, 1f, 1f, 1f)
        overFontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.RED_TEXT_BUTTON_MEDIUM.skinKey) {
        up = skin[Drawables.BUTTON_RED_UP]
        over = skin[Drawables.BUTTON_RED_OVER]
        disabled = skin[Drawables.BUTTON_RED_UP]
        font = skin[Fonts.MEDIUM]
        fontColor = Color(1f, 1f, 1f, 1f)
    }
    textButton(Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) {
        up = skin[Drawables.BUTTON_BLUE_UP]
        down = skin[Drawables.BUTTON_BLUE_DOWN]
        over = skin[Drawables.BUTTON_BLUE_OVER]
        disabled = skin[Drawables.BUTTON_RED_UP]
        font = skin[Fonts.DEFAULT]
        fontColor = Color(1f, 1f, 1f, 1f)
        overFontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.GREEN_TEXT_BUTTON_DEFAULT.skinKey) {
        up = skin[Drawables.BUTTON_GREEN_UP]
        down = skin[Drawables.BUTTON_GREEN_DOWN]
        over = skin[Drawables.BUTTON_GREEN_OVER]
        disabled = skin[Drawables.BUTTON_RED_UP]
        font = skin[Fonts.DEFAULT]
        fontColor = Color(1f, 1f, 1f, 1f)
        overFontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.RED_TEXT_BUTTON_DEFAULT.skinKey) {
        up = skin[Drawables.BUTTON_RED_UP]
        over = skin[Drawables.BUTTON_RED_OVER]
        disabled = skin[Drawables.BUTTON_RED_UP]
        font = skin[Fonts.DEFAULT]
        fontColor = Color(1f, 1f, 1f, 1f)
    }
}

fun disposeSkin() {
    Scene2DSkin.defaultSkin.disposeSafely()
}
