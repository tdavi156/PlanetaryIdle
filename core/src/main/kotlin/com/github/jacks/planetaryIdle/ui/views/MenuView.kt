package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.github.jacks.planetaryIdle.events.QuitGameEvent
import com.github.jacks.planetaryIdle.events.ResetGameEvent
import com.github.jacks.planetaryIdle.events.SaveGameEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.models.MenuModel
import ktx.log.logger
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.table
import ktx.scene2d.textButton

class MenuView(
    model : MenuModel,
    skin : Skin,
    private val planetView : Table,
    private val shopView : Table,
    private val achievementsView : Table,
) : Table(skin), KTable {

    private val planetButton : TextButton
    private val galaxyButton : TextButton
    private val automationButton : TextButton
    private val challengesButton : TextButton
    private val shopButton : TextButton
    private val achievementsButton : TextButton
    private val statisticsButton : TextButton
    private val settingsButton : TextButton
    private val resetButton : TextButton
    private val quitButton : TextButton

    init {
        table { tableCell ->
            top()
            this@MenuView.planetButton = textButton("Planet", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(4f, 2f, 2f, 2f)
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@MenuView.changeActiveActor(1)
                    }
                })
            }
            row()
            this@MenuView.galaxyButton = textButton("Locked", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = true
            }
            row()
            this@MenuView.automationButton = textButton("Locked", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = true
            }
            row()
            this@MenuView.challengesButton = textButton("Locked", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = true
            }
            row()
            this@MenuView.shopButton = textButton("Shop", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = false
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@MenuView.changeActiveActor(2)
                    }
                })
            }
            row()
            this@MenuView.achievementsButton = textButton("Achievements", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = false
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@MenuView.changeActiveActor(3)
                    }
                })
            }
            row()
            this@MenuView.statisticsButton = textButton("Statistics", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = true
            }
            row()
            this@MenuView.settingsButton = textButton("Settings", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = true
            }
            row()
            this@MenuView.resetButton = textButton("Reset Game", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = false
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        log.debug { "Reset Game" }
                        stage.fire(ResetGameEvent())
                    }
                })
            }
            row()
            this@MenuView.quitButton = textButton("Quit Game", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        log.debug { "Save Game" }
                        stage.fire(SaveGameEvent())
                        log.debug { "Quit Game" }
                        stage.fire(QuitGameEvent())
                    }
                })
            }
            tableCell.expand().fill()
        }
    }

    private fun changeActiveActor(actorId : Int) {
        planetView.isVisible = actorId == 1
        shopView.isVisible = actorId == 2
        achievementsView.isVisible = actorId == 3
    }

    companion object {
        private val log = logger<MenuView>()
    }
}

@Scene2dDsl
fun <S> KWidget<S>.menuView(
    model : MenuModel,
    planetView : Table,
    shopView : Table,
    achievementsView : Table,
    skin : Skin = Scene2DSkin.defaultSkin,
    init : MenuView.(S) -> Unit = { }
) : MenuView = actor(MenuView(model, skin, planetView, shopView, achievementsView), init)
