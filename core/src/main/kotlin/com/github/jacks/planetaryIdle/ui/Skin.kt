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
import ktx.style.get
import ktx.style.label
import ktx.style.set
import ktx.style.skin
import ktx.style.textButton
import ktx.style.textTooltip

enum class Drawables(
    val atlasKey : String
) {
    BAR_GREEN_THIN("bar_green_thin"),
    BAR_GREEN_THICK("bar_green_thick"),
    BAR_GREEN_THICK_A25("bar_green_thick_a25"),
    BAR_GREY_THICK("bar_grey_thick"),
    BAR_BLACK_THIN("bar_black_thin"),

    BACKGROUND_GREY("button_grey_up"),

    BUTTON_RED_UP("button_red_up"),
    BUTTON_RED_OVER("button_red_over"),
    BUTTON_RED_DISABLED("button_red_disabled"),

    BUTTON_ORANGE_UP("button_orange_up"),
    BUTTON_ORANGE_OVER("button_orange_over"),
    BUTTON_ORANGE_DISABLED("button_orange_disabled"),

    BUTTON_YELLOW_UP("button_yellow_up"),
    BUTTON_YELLOW_OVER("button_yellow_over"),
    BUTTON_YELLOW_DISABLED("button_yellow_disabled"),

    BUTTON_GREEN_UP("button_green_up"),
    BUTTON_GREEN_OVER("button_green_over"),
    BUTTON_GREEN_DISABLED("button_green_disabled"),

    BUTTON_LIGHT_BLUE_UP("button_light_blue_up"),
    BUTTON_LIGHT_BLUE_OVER("button_light_blue_over"),
    BUTTON_LIGHT_BLUE_DISABLED("button_light_blue_disabled"),

    BUTTON_BLUE_UP("button_blue_up"),
    BUTTON_BLUE_OVER("button_blue_over"),
    BUTTON_BLUE_DISABLED("button_blue_disabled"),

    BUTTON_PURPLE_UP("button_purple_up"),
    BUTTON_PURPLE_OVER("button_purple_over"),
    BUTTON_PURPLE_DISABLED("button_purple_disabled"),

    BUTTON_PINK_UP("button_pink_up"),
    BUTTON_PINK_OVER("button_pink_over"),
    BUTTON_PINK_DISABLED("button_pink_disabled"),

    BUTTON_BROWN_UP("button_brown_up"),
    BUTTON_BROWN_OVER("button_brown_over"),
    BUTTON_BROWN_DISABLED("button_brown_disabled"),

    BUTTON_WHITE_UP("button_white_up"),
    BUTTON_WHITE_OVER("button_white_over"),
    BUTTON_WHITE_DISABLED("button_white_disabled"),

    BUTTON_BLACK_UP("button_black_up"),
    BUTTON_BLACK_OVER("button_black_over"),
    BUTTON_BLACK_DISABLED("button_black_disabled"),

    BUTTON_GREY_UP("button_grey_up"),
}

enum class Labels {
    TINY,
    SMALL,
    MEDIUM,
    DEFAULT,
    LARGE,
    X_LARGE,
    XX_LARGE,
    TITLE;

    val skinKey = this.name.lowercase()
}

enum class Fonts(
    val atlasRegionKey : String,
    val scaling : Float
) {
    TINY("default_12pt", 1f),
    SMALL("default_16pt", 1f),
    MEDIUM("default_20pt", 1f),
    DEFAULT("default_24pt", 1f),
    LARGE("default_28pt", 1f),
    X_LARGE("default_32pt", 1f),
    XX_LARGE("default_40pt", 1f),
    TITLE("default_64pt", 1f);

    val skinKey = "Font_${this.name.lowercase()}"
    val fontPath = "fonts/${this.atlasRegionKey}.fnt"
}

enum class Tooltips {
    DEFAULT_GREY;

    val skinKey = this.name.lowercase()
}

enum class Buttons {
    RED_BUTTON_SMALL,
    RED_BUTTON_MEDIUM,
    ORANGE_BUTTON_SMALL,
    ORANGE_BUTTON_MEDIUM,
    YELLOW_BUTTON_SMALL,
    YELLOW_BUTTON_MEDIUM,
    GREEN_BUTTON_SMALL,
    GREEN_BUTTON_MEDIUM,
    LIGHT_BLUE_BUTTON_SMALL,
    LIGHT_BLUE_BUTTON_MEDIUM,
    BLUE_BUTTON_SMALL,
    BLUE_BUTTON_MEDIUM,
    PURPLE_BUTTON_SMALL,
    PURPLE_BUTTON_MEDIUM,
    PINK_BUTTON_SMALL,
    PINK_BUTTON_MEDIUM,
    BROWN_BUTTON_SMALL,
    BROWN_BUTTON_MEDIUM,
    WHITE_BUTTON_SMALL,
    WHITE_BUTTON_MEDIUM,
    BLACK_BUTTON_SMALL,
    BLACK_BUTTON_MEDIUM,
    GREY_BUTTON_SMALL,
    GREY_BUTTON_MEDIUM;

    val skinKey = this.name.lowercase()
}

operator fun Skin.get(drawable : Drawables) : Drawable = this.getDrawable(drawable.atlasKey)
operator fun Skin.get(font : Fonts) : BitmapFont = this.getFont(font.skinKey)

fun loadSkin() {
    Scene2DSkin.defaultSkin = skin(TextureAtlas("ui/ui.atlas")) { skin ->
        loadFonts(skin)
        loadLabels(skin)
        loadTooltips(skin)
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
    label(Labels.TINY.skinKey) {
        font = skin[Fonts.TINY]
        fontColor = Color.WHITE
    }
    label(Labels.SMALL.skinKey) {
        font = skin[Fonts.SMALL]
        fontColor = Color.WHITE
    }
    label(Labels.MEDIUM.skinKey) {
        font = skin[Fonts.MEDIUM]
        fontColor = Color.WHITE
    }
    label(Labels.DEFAULT.skinKey) {
        font = skin[Fonts.DEFAULT]
        fontColor = Color.WHITE
    }
    label(Labels.LARGE.skinKey) {
        font = skin[Fonts.LARGE]
        fontColor = Color.WHITE
    }
    label(Labels.X_LARGE.skinKey) {
        font = skin[Fonts.X_LARGE]
        fontColor = Color.WHITE
    }
    label(Labels.XX_LARGE.skinKey) {
        font = skin[Fonts.XX_LARGE]
        fontColor = Color.WHITE
    }
    label(Labels.TITLE.skinKey) {
        font = skin[Fonts.TITLE]
        fontColor = Color.WHITE
    }
}

private fun @SkinDsl Skin.loadTooltips(skin: Skin) {
    textTooltip(Tooltips.DEFAULT_GREY.skinKey) {
        background = skin[Drawables.BACKGROUND_GREY]
        label = skin[Labels.DEFAULT.skinKey]
    }
}

private fun @SkinDsl Skin.loadButtons(skin: Skin) {
    textButton(Buttons.RED_BUTTON_SMALL.skinKey) {
        up = skin[Drawables.BUTTON_RED_UP]
        down = skin[Drawables.BUTTON_RED_DISABLED]
        over = skin[Drawables.BUTTON_RED_OVER]
        disabled = skin[Drawables.BUTTON_RED_DISABLED]
        font = skin[Fonts.SMALL]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.RED_BUTTON_MEDIUM.skinKey) {
        up = skin[Drawables.BUTTON_RED_UP]
        down = skin[Drawables.BUTTON_RED_DISABLED]
        over = skin[Drawables.BUTTON_RED_OVER]
        disabled = skin[Drawables.BUTTON_RED_DISABLED]
        font = skin[Fonts.MEDIUM]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.ORANGE_BUTTON_SMALL.skinKey) {
        up = skin[Drawables.BUTTON_ORANGE_UP]
        down = skin[Drawables.BUTTON_ORANGE_DISABLED]
        over = skin[Drawables.BUTTON_ORANGE_OVER]
        disabled = skin[Drawables.BUTTON_ORANGE_DISABLED]
        font = skin[Fonts.SMALL]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.ORANGE_BUTTON_MEDIUM.skinKey) {
        up = skin[Drawables.BUTTON_ORANGE_UP]
        down = skin[Drawables.BUTTON_ORANGE_DISABLED]
        over = skin[Drawables.BUTTON_ORANGE_OVER]
        disabled = skin[Drawables.BUTTON_ORANGE_DISABLED]
        font = skin[Fonts.MEDIUM]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.YELLOW_BUTTON_SMALL.skinKey) {
        up = skin[Drawables.BUTTON_YELLOW_UP]
        down = skin[Drawables.BUTTON_YELLOW_DISABLED]
        over = skin[Drawables.BUTTON_YELLOW_OVER]
        disabled = skin[Drawables.BUTTON_YELLOW_DISABLED]
        font = skin[Fonts.SMALL]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.YELLOW_BUTTON_MEDIUM.skinKey) {
        up = skin[Drawables.BUTTON_YELLOW_UP]
        down = skin[Drawables.BUTTON_YELLOW_DISABLED]
        over = skin[Drawables.BUTTON_YELLOW_OVER]
        disabled = skin[Drawables.BUTTON_YELLOW_DISABLED]
        font = skin[Fonts.MEDIUM]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.GREEN_BUTTON_SMALL.skinKey) {
        up = skin[Drawables.BUTTON_GREEN_UP]
        down = skin[Drawables.BUTTON_GREEN_DISABLED]
        over = skin[Drawables.BUTTON_GREEN_OVER]
        disabled = skin[Drawables.BUTTON_GREEN_DISABLED]
        font = skin[Fonts.SMALL]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.GREEN_BUTTON_MEDIUM.skinKey) {
        up = skin[Drawables.BUTTON_GREEN_UP]
        down = skin[Drawables.BUTTON_GREEN_DISABLED]
        over = skin[Drawables.BUTTON_GREEN_OVER]
        disabled = skin[Drawables.BUTTON_GREEN_DISABLED]
        font = skin[Fonts.MEDIUM]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.LIGHT_BLUE_BUTTON_SMALL.skinKey) {
        up = skin[Drawables.BUTTON_LIGHT_BLUE_UP]
        down = skin[Drawables.BUTTON_LIGHT_BLUE_DISABLED]
        over = skin[Drawables.BUTTON_LIGHT_BLUE_OVER]
        disabled = skin[Drawables.BUTTON_LIGHT_BLUE_DISABLED]
        font = skin[Fonts.SMALL]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.LIGHT_BLUE_BUTTON_MEDIUM.skinKey) {
        up = skin[Drawables.BUTTON_LIGHT_BLUE_UP]
        down = skin[Drawables.BUTTON_LIGHT_BLUE_DISABLED]
        over = skin[Drawables.BUTTON_LIGHT_BLUE_OVER]
        disabled = skin[Drawables.BUTTON_LIGHT_BLUE_DISABLED]
        font = skin[Fonts.MEDIUM]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.BLUE_BUTTON_SMALL.skinKey) {
        up = skin[Drawables.BUTTON_BLUE_UP]
        down = skin[Drawables.BUTTON_BLUE_DISABLED]
        over = skin[Drawables.BUTTON_BLUE_OVER]
        disabled = skin[Drawables.BUTTON_BLUE_DISABLED]
        font = skin[Fonts.SMALL]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.BLUE_BUTTON_MEDIUM.skinKey) {
        up = skin[Drawables.BUTTON_BLUE_UP]
        down = skin[Drawables.BUTTON_BLUE_DISABLED]
        over = skin[Drawables.BUTTON_BLUE_OVER]
        disabled = skin[Drawables.BUTTON_BLUE_DISABLED]
        font = skin[Fonts.MEDIUM]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.PURPLE_BUTTON_SMALL.skinKey) {
        up = skin[Drawables.BUTTON_PURPLE_UP]
        down = skin[Drawables.BUTTON_PURPLE_DISABLED]
        over = skin[Drawables.BUTTON_PURPLE_OVER]
        disabled = skin[Drawables.BUTTON_PURPLE_DISABLED]
        font = skin[Fonts.SMALL]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.PURPLE_BUTTON_MEDIUM.skinKey) {
        up = skin[Drawables.BUTTON_PURPLE_UP]
        down = skin[Drawables.BUTTON_PURPLE_DISABLED]
        over = skin[Drawables.BUTTON_PURPLE_OVER]
        disabled = skin[Drawables.BUTTON_PURPLE_DISABLED]
        font = skin[Fonts.MEDIUM]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.PINK_BUTTON_SMALL.skinKey) {
        up = skin[Drawables.BUTTON_PINK_UP]
        down = skin[Drawables.BUTTON_PINK_DISABLED]
        over = skin[Drawables.BUTTON_PINK_OVER]
        disabled = skin[Drawables.BUTTON_PINK_DISABLED]
        font = skin[Fonts.SMALL]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.PINK_BUTTON_MEDIUM.skinKey) {
        up = skin[Drawables.BUTTON_PINK_UP]
        down = skin[Drawables.BUTTON_PINK_DISABLED]
        over = skin[Drawables.BUTTON_PINK_OVER]
        disabled = skin[Drawables.BUTTON_PINK_DISABLED]
        font = skin[Fonts.MEDIUM]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.BROWN_BUTTON_SMALL.skinKey) {
        up = skin[Drawables.BUTTON_BROWN_UP]
        down = skin[Drawables.BUTTON_BROWN_DISABLED]
        over = skin[Drawables.BUTTON_BROWN_OVER]
        disabled = skin[Drawables.BUTTON_BROWN_DISABLED]
        font = skin[Fonts.SMALL]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.BROWN_BUTTON_MEDIUM.skinKey) {
        up = skin[Drawables.BUTTON_BROWN_UP]
        down = skin[Drawables.BUTTON_BROWN_DISABLED]
        over = skin[Drawables.BUTTON_BROWN_OVER]
        disabled = skin[Drawables.BUTTON_BROWN_DISABLED]
        font = skin[Fonts.MEDIUM]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.WHITE_BUTTON_SMALL.skinKey) {
        up = skin[Drawables.BUTTON_WHITE_UP]
        down = skin[Drawables.BUTTON_WHITE_DISABLED]
        over = skin[Drawables.BUTTON_WHITE_OVER]
        disabled = skin[Drawables.BUTTON_WHITE_DISABLED]
        font = skin[Fonts.SMALL]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.WHITE_BUTTON_MEDIUM.skinKey) {
        up = skin[Drawables.BUTTON_WHITE_UP]
        down = skin[Drawables.BUTTON_WHITE_DISABLED]
        over = skin[Drawables.BUTTON_WHITE_OVER]
        disabled = skin[Drawables.BUTTON_WHITE_DISABLED]
        font = skin[Fonts.MEDIUM]
        fontColor = Color(0f, 0f, 0f, 1f)
    }
    textButton(Buttons.BLACK_BUTTON_SMALL.skinKey) {
        up = skin[Drawables.BUTTON_BLACK_UP]
        down = skin[Drawables.BUTTON_BLACK_DISABLED]
        over = skin[Drawables.BUTTON_BLACK_OVER]
        disabled = skin[Drawables.BUTTON_BLACK_DISABLED]
        font = skin[Fonts.SMALL]
        fontColor = Color(1f, 1f, 1f, 1f)
    }
    textButton(Buttons.BLACK_BUTTON_MEDIUM.skinKey) {
        up = skin[Drawables.BUTTON_BLACK_UP]
        down = skin[Drawables.BUTTON_BLACK_DISABLED]
        over = skin[Drawables.BUTTON_BLACK_OVER]
        disabled = skin[Drawables.BUTTON_BLACK_DISABLED]
        font = skin[Fonts.MEDIUM]
        fontColor = Color(1f, 1f, 1f, 1f)
    }
    textButton(Buttons.GREY_BUTTON_SMALL.skinKey) {
        up = skin[Drawables.BUTTON_GREY_UP]
        down = skin[Drawables.BUTTON_GREY_UP]
        over = skin[Drawables.BUTTON_GREY_UP]
        disabled = skin[Drawables.BUTTON_GREY_UP]
        font = skin[Fonts.SMALL]
        fontColor = Color(1f, 1f, 1f, 1f)
    }
    textButton(Buttons.GREY_BUTTON_MEDIUM.skinKey) {
        up = skin[Drawables.BUTTON_GREY_UP]
        down = skin[Drawables.BUTTON_GREY_UP]
        over = skin[Drawables.BUTTON_GREY_UP]
        disabled = skin[Drawables.BUTTON_GREY_UP]
        font = skin[Fonts.MEDIUM]
        fontColor = Color(1f, 1f, 1f, 1f)
    }
}

fun disposeSkin() {
    Scene2DSkin.defaultSkin.disposeSafely()
}
