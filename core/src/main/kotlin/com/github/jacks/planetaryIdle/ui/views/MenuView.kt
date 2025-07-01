package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
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
import ktx.actors.txt
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
    skin : Skin
) : Table(skin), KTable {

    private val preferences : Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }

    // buttons
    private val planetButton : TextButton
    private val galaxyButton : TextButton
    private val automationButton : TextButton
    private val challengesButton : TextButton
    private val achievementsButton : TextButton
    private val statisticsButton : TextButton
    private val settingsButton : TextButton
    private val resetButton : TextButton
    private val quitButton : TextButton

    init {
        setFillParent(true)
        stage = getStage()

        // UI
        table { menuTableCell ->
            this@MenuView.planetButton = textButton("Planet", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(40f)
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        // this@MenuView.currentView = "MenuView"
                        log.debug { "currentView -> ${this@MenuView.planetButton.txt}" }
                    }
                })
            }
            row()
            this@MenuView.galaxyButton = textButton("Galaxy", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(40f)
                isDisabled = false
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        // this@MenuView.currentView = "galaxyView"
                        log.debug { "currentView -> ${this@MenuView.galaxyButton.txt}" }
                    }
                })
            }
            row()
            this@MenuView.automationButton = textButton("Automation", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(40f)
                isDisabled = true
            }
            row()
            this@MenuView.challengesButton = textButton("Challenges", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(40f)
                isDisabled = true
            }
            row()
            this@MenuView.achievementsButton = textButton("Achievements", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(40f)
                isDisabled = true
            }
            row()
            this@MenuView.statisticsButton = textButton("Statistics", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(40f)
                isDisabled = true
            }
            row()
            this@MenuView.settingsButton = textButton("Settings", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(40f)
                isDisabled = true
            }
            row()
            this@MenuView.resetButton = textButton("Reset Game", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(40f)
                isDisabled = false
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        log.debug { "Reset Game" }
                        stage.fire(ResetGameEvent())
                    }
                })
            }
            row()
            this@MenuView.quitButton = textButton("Quit Game", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(40f)
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        log.debug { "Save Game" }
                        stage.fire(SaveGameEvent())
                        log.debug { "Quit Game" }
                        stage.fire(QuitGameEvent())
                    }
                })
            }
            menuTableCell.expand().top().left().width(180f)
        }


        // Data Binding
        // model.onPropertyChange(PlanetModel::totalPopulationAmount) { amount -> totalPopAmountChange(amount) }
    }

    companion object {
        private val log = logger<MenuView>()
    }
}

@Scene2dDsl
fun <S> KWidget<S>.menuView(
    model : MenuModel,
    skin : Skin = Scene2DSkin.defaultSkin,
    init : MenuView.(S) -> Unit = { }
) : MenuView = actor(MenuView(model, skin), init)
