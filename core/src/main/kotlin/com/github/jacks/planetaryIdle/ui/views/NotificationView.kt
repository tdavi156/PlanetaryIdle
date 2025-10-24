package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.delay
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.github.jacks.planetaryIdle.components.Achievements
import com.github.jacks.planetaryIdle.events.AchievementCompletedEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.models.NotificationModel
import ktx.actors.alpha
import ktx.actors.plusAssign
import ktx.actors.txt
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.label
import ktx.scene2d.table

class NotificationView(
    model : NotificationModel,
    skin : Skin,
) : Table(skin), KTable {

    private lateinit var achLabel : Label

    init {
        setFillParent(true)

        if (!skin.has(PIXMAP_KEY, TextureRegionDrawable::class.java)) {
            skin.add(PIXMAP_KEY, TextureRegionDrawable(
                Texture(
                    Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
                        this.drawPixel(0, 0, Color.rgba8888(0.71f, 0.41f, 0.21f, 0.5f))
                    }
                )
            ))
        }

        //background = skin.get(PIXMAP_KEY, TextureRegionDrawable::class.java)

        table { tableCell ->
            this@NotificationView.achLabel = label("Achievement 1", Labels.ACH_COMPLETED_BGD.skinKey) { cell ->
                this.setAlignment(Align.center)
                cell.width(350f).height(50f).pad(5f)
            }

            this.alpha = 1f
            tableCell.expand().fill()
        }

        // Data Binding
        model.onPropertyChange(NotificationModel::achId) { achId -> triggerNotification(achId) }
    }

    private fun triggerNotification(achId : Int) {
        val ach = Achievements.entries[achId - 1]
        achLabel.txt = ach.achName

        achLabel.parent.clearActions()
        achLabel.parent += Actions.sequence(fadeIn(0.1f), delay(1.5f, fadeOut(0.3f)))
        if (achLabel.parent.actions.isEmpty) {
            stage.fire(AchievementCompletedEvent(achId))
        }
    }

    companion object {
        private const val PIXMAP_KEY = "pauseTexturePixmap"
    }
}

@Scene2dDsl
fun <S> KWidget<S>.notificationView(
    model : NotificationModel,
    skin : Skin = Scene2DSkin.defaultSkin,
    init : NotificationView.(S) -> Unit = { }
) : NotificationView = actor(NotificationView(model, skin), init)
