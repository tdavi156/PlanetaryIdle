package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.models.HelpViewModel
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor

/**
 * Full-screen transparent overlay that displays auto-dismissing toast messages
 * when a new Help section is unlocked. Sits in the GameScreen stack.
 */
class HelpToastView(
    private val viewModel: HelpViewModel,
    skin: Skin,
) : Table(skin), KTable {

    private val toastTable = Table(skin)
    private val toastLabel = Label("", skin, Labels.SMALL.skinKey)

    init {
        setFillParent(true)

        // Build the dark pill background for the toast
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            drawPixel(0, 0, Color.rgba8888(0.08f, 0.08f, 0.08f, 0.90f))
        }
        val toastBg = TextureRegionDrawable(Texture(pixmap))
        pixmap.dispose()

        toastLabel.setWrap(false)
        toastTable.background = toastBg
        toastTable.add(toastLabel).pad(10f, 24f, 10f, 24f)
        toastTable.isVisible = false

        // Position at bottom-center of the content area
        add(toastTable).expand().bottom().padBottom(70f)

        viewModel.onPropertyChange(HelpViewModel::toastMessage) { message ->
            if (message.isNotEmpty()) showToast(message)
        }
    }

    private fun showToast(message: String) {
        toastLabel.setText(message)
        toastTable.clearActions()
        toastTable.color.a = 0f
        toastTable.isVisible = true
        toastTable.addAction(
            Actions.sequence(
                Actions.fadeIn(0.35f),
                Actions.delay(3.5f),
                Actions.fadeOut(0.5f),
                Actions.run {
                    toastTable.isVisible = false
                    viewModel.clearToastMessage()
                }
            )
        )
    }
}

@Scene2dDsl
fun <S> KWidget<S>.helpToastView(
    viewModel: HelpViewModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: HelpToastView.(S) -> Unit = {},
): HelpToastView = actor(HelpToastView(viewModel, skin), init)
