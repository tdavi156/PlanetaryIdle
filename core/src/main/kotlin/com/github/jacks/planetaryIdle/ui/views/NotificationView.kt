package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.github.jacks.planetaryIdle.components.Achievements
import com.github.jacks.planetaryIdle.events.AchievementCompletedEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.models.NotificationModel
import ktx.log.logger
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor

class NotificationView(
    model : NotificationModel,
    skin : Skin,
) : Table(skin), KTable {

    private val notifTable = Table(skin)
    private val activeButtons = mutableListOf<TextButton>()

    init {
        setFillParent(true)
        add(notifTable).expand().top().right().pad(50f, 0f, 0f, 5f)

        // Data Binding
        model.onPropertyChange(NotificationModel::achId) { achId -> triggerNotification(achId) }
    }

    private fun triggerNotification(achId : Int) {
        log.debug { "NotificationView: triggerNotification .. achId: $achId" }
        val ach = Achievements.entries[achId - 1]
        val button = TextButton(ach.achName, skin, Buttons.GREEN_BUTTON_SMALL.skinKey)

        button.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                dismiss(button)
            }
        })
        button.addAction(Actions.sequence(
            Actions.delay(NOTIFICATION_DURATION),
            Actions.run { dismiss(button) }
        ))

        activeButtons.add(button)
        rebuildTable()
        stage.fire(AchievementCompletedEvent(achId))
    }

    private fun dismiss(button : TextButton) {
        if (activeButtons.remove(button)) {
            button.clearActions()
            rebuildTable()
        }
    }

    private fun rebuildTable() {
        notifTable.clear()
        activeButtons.forEach { btn ->
            notifTable.add(btn).width(150f).height(35f).pad(2f, 0f, 2f, 0f)
            notifTable.row()
        }
    }

    companion object {
        private val log = logger<NotificationView>()
        private const val NOTIFICATION_DURATION = 5f
    }
}

@Scene2dDsl
fun <S> KWidget<S>.notificationView(
    model : NotificationModel,
    skin : Skin = Scene2DSkin.defaultSkin,
    init : NotificationView.(S) -> Unit = { }
) : NotificationView = actor(NotificationView(model, skin), init)
