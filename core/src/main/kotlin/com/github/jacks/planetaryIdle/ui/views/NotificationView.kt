package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.github.jacks.planetaryIdle.components.Achievements
import com.github.jacks.planetaryIdle.events.AchievementCompletedEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.models.NotificationModel
import ktx.actors.txt
import ktx.log.logger
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.table
import ktx.scene2d.textButton

class NotificationView(
    model : NotificationModel,
    skin : Skin,
) : Table(skin), KTable {

    private lateinit var achButton : TextButton

    init {
        setFillParent(true)

        table { tableCell ->
            this@NotificationView.achButton = textButton("Achievement 1", Buttons.GREEN_BUTTON_SMALL.skinKey) { cell ->
                cell.expand().fill().top().right().width(150f).height(35f).pad(50f, 0f, 0f, 210f)
                isVisible = false
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@NotificationView.achButton.isVisible = false
                    }
                })
            }

            tableCell.expand().fill()
        }

        // Data Binding
        model.onPropertyChange(NotificationModel::achId) { achId -> triggerNotification(achId) }
    }

    private fun triggerNotification(achId : Int) {
        log.debug { "NotificationView: triggerNotification .. achId: $achId" }
        val ach = Achievements.entries[achId - 1]
        achButton.txt = ach.achName
        achButton.isVisible = true
        stage.fire(AchievementCompletedEvent(achId))
    }

    companion object {
        val log = logger<NotificationView>()
    }
}

@Scene2dDsl
fun <S> KWidget<S>.notificationView(
    model : NotificationModel,
    skin : Skin = Scene2DSkin.defaultSkin,
    init : NotificationView.(S) -> Unit = { }
) : NotificationView = actor(NotificationView(model, skin), init)
