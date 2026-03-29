package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.github.jacks.planetaryIdle.events.QuitGameEvent
import com.github.jacks.planetaryIdle.events.ResetGameEvent
import com.github.jacks.planetaryIdle.events.SaveGameEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.models.MenuModel
import ktx.actors.txt
import ktx.log.logger
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton

class MenuView(
    model : MenuModel,
    skin : Skin,
    private val planetView : Table,
    private val shopView : Table,
    private val achievementsView : Table,
) : Table(skin), KTable {

    // buttons
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

    private lateinit var planetToolTipLabel : Label
    private lateinit var galaxyToolTipLabel : Label
    private lateinit var automationToolTipLabel : Label
    private lateinit var challengesToolTipLabel : Label
    private lateinit var shopToolTipLabel : Label
    private lateinit var achievementsToolTipLabel : Label
    private lateinit var statisticsToolTipLabel : Label
    private lateinit var settingsToolTipLabel : Label
    private lateinit var resetToolTipLabel : Label
    private lateinit var quitToolTipLabel : Label

    init {
        setFillParent(true)
        stage = getStage()

        // tooltips
        table { tooltipTableCell ->
            this@MenuView.planetToolTipLabel = label("planet", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                cell.expand().top().right().width(150f).height(45f).pad(4f, 2f, 2f, 2f)
                this.setAlignment(Align.center)
                this.isVisible = false
            }
            row()
            this@MenuView.galaxyToolTipLabel = label("galaxy", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                cell.expand().top().right().width(150f).height(45f).pad(2f, 2f, 2f, 2f)
                this.setAlignment(Align.center)
                this.isVisible = false
            }
            row()
            this@MenuView.automationToolTipLabel = label("automation", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                cell.expand().top().right().width(150f).height(45f).pad(2f, 2f, 2f, 2f)
                this.setAlignment(Align.center)
                this.isVisible = false
            }
            row()
            this@MenuView.challengesToolTipLabel = label("challenges", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                cell.expand().top().right().width(150f).height(45f).pad(2f, 2f, 2f, 2f)
                this.setAlignment(Align.center)
                this.isVisible = false
            }
            row()
            this@MenuView.shopToolTipLabel = label("shop", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                cell.expand().top().right().width(150f).height(45f).pad(2f, 2f, 2f, 2f)
                this.setAlignment(Align.center)
                this.isVisible = false
            }
            row()
            this@MenuView.achievementsToolTipLabel = label("achievements", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                cell.expand().top().right().width(150f).height(45f).pad(2f, 2f, 2f, 2f)
                this.setAlignment(Align.center)
                this.isVisible = false
            }
            row()
            this@MenuView.statisticsToolTipLabel = label("statistics", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                cell.expand().top().right().width(150f).height(45f).pad(2f, 2f, 2f, 2f)
                this.setAlignment(Align.center)
                this.isVisible = false
            }
            row()
            this@MenuView.settingsToolTipLabel = label("settings", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                cell.expand().top().right().width(150f).height(45f).pad(2f, 2f, 2f, 2f)
                this.setAlignment(Align.center)
                this.isVisible = false
            }
            row()
            this@MenuView.resetToolTipLabel = label("reset", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                cell.expand().top().right().width(150f).height(45f).pad(2f, 2f, 2f, 2f)
                this.setAlignment(Align.center)
                this.isVisible = false
            }
            row()
            this@MenuView.quitToolTipLabel = label("quit", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                cell.expand().top().right().width(150f).height(45f).pad(2f, 2f, 2f, 2f)
                this.setAlignment(Align.center)
                this.isVisible = false
            }
            row()
            tooltipTableCell.expand().top().right().width(230f).padTop(44f)
        }

        // menu buttons
        table { menuTableCell ->
            // planet
            this@MenuView.planetButton = textButton("Planet", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(4f, 2f, 2f, 2f)
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@MenuView.changeActiveActor(1)
                    }
                })
            }
            addHoverTooltip(this@MenuView.planetButton, this@MenuView.planetToolTipLabel)
            row()
            // galaxy
            this@MenuView.galaxyButton = textButton("Locked", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = true
            }
            addHoverTooltip(this@MenuView.galaxyButton, this@MenuView.galaxyToolTipLabel)
            row()
            // automation
            this@MenuView.automationButton = textButton("Locked", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = true
            }
            addHoverTooltip(this@MenuView.automationButton, this@MenuView.automationToolTipLabel)
            row()
            // challenges
            this@MenuView.challengesButton = textButton("Locked", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = true
            }
            addHoverTooltip(this@MenuView.challengesButton, this@MenuView.challengesToolTipLabel)
            row()
            // shop
            this@MenuView.shopButton = textButton("Shop", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = false
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@MenuView.changeActiveActor(2)
                    }
                })
            }
            addHoverTooltip(this@MenuView.shopButton, this@MenuView.shopToolTipLabel)
            row()
            // achievements
            this@MenuView.achievementsButton = textButton("Achievements", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = false
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@MenuView.changeActiveActor(3)
                    }
                })
            }
            addHoverTooltip(this@MenuView.achievementsButton, this@MenuView.achievementsToolTipLabel)
            row()
            // statistics
            this@MenuView.statisticsButton = textButton("Statistics", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = true
            }
            addHoverTooltip(this@MenuView.statisticsButton, this@MenuView.statisticsToolTipLabel)
            row()
            // settings
            this@MenuView.settingsButton = textButton("Settings", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = true
            }
            addHoverTooltip(this@MenuView.settingsButton, this@MenuView.settingsToolTipLabel)
            row()
            // reset
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
            addHoverTooltip(this@MenuView.resetButton, this@MenuView.resetToolTipLabel)
            row()
            // quit
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
            addHoverTooltip(this@MenuView.quitButton, this@MenuView.quitToolTipLabel)
            menuTableCell.top().right().width(204f).padTop(44f)
        }
    }

    private fun addHoverTooltip(button : TextButton, tooltip : Label) {
        button.addListener(object : InputListener() {
            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                tooltip.isVisible = isOver
                super.enter(event, x, y, pointer, fromActor)
            }
            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                tooltip.isVisible = isOver
                super.exit(event, x, y, pointer, toActor)
            }
        })
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
