package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.jacks.planetaryIdle.events.QuitGameEvent
import com.github.jacks.planetaryIdle.events.ResetGameEvent
import com.github.jacks.planetaryIdle.events.SaveGameEvent
import com.github.jacks.planetaryIdle.events.SettingsClosedEvent
import com.github.jacks.planetaryIdle.events.SettingsOpenEvent
import com.github.jacks.planetaryIdle.events.ViewStateChangeEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.ViewState
import com.github.jacks.planetaryIdle.ui.models.HelpViewModel
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
    model: MenuModel,
    helpViewModel: HelpViewModel,
    skin: Skin,
    private val stage: Stage,
    private val farmView: Table,
    private val barnView: Table,
    private val kitchenView: Table,
    private val codexView: Table,
    private val achievementsView: Table,
    private val settingsView: Table,
    private val observatoryView: Table,
    private val helpView: Table,
    private val automationView: Table,
) : Table(skin), KTable, EventListener {

    private lateinit var barnButton:        TextButton
    private lateinit var kitchenButton:     TextButton
    private lateinit var codexButton:       TextButton
    private lateinit var observatoryButton: TextButton
    private lateinit var automationButton:  TextButton
    private lateinit var helpBadgeDot:      Image
    private lateinit var kitchenBadgeDot:   Image

    init {
        stage.addListener(this)

        // Shared dot factory for menu badge overlays
        fun makeDotDrawable(r: Float, g: Float, b: Float): TextureRegionDrawable {
            val px = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply { drawPixel(0, 0, Color.rgba8888(r, g, b, 1f)) }
            val d = TextureRegionDrawable(Texture(px))
            px.dispose()
            return d
        }
        val orangeDot = makeDotDrawable(0.95f, 0.65f, 0.1f)

        val view = this@MenuView
        table { tableCell ->
            top()

            textButton("Farm", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(4f, 2f, 2f, 2f)
                addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@MenuView.changeActiveView(ViewState.FARM)
                    }
                })
            }
            row()

            view.barnButton = textButton("Barn", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = !model.barnUnlocked
                addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@MenuView.changeActiveView(ViewState.BARN)
                    }
                })
            }
            row()

            // Kitchen button with exhausted-research badge dot
            val kitchenBtn = textButton("Kitchen", Buttons.GREY_BUTTON_MEDIUM.skinKey) {
                isDisabled = !model.kitchenUnlocked
                addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@MenuView.changeActiveView(ViewState.KITCHEN)
                    }
                })
            }
            view.kitchenBadgeDot = Image(orangeDot).apply { isVisible = model.kitchenHasExhausted }
            val kitchenStack = Stack()
            kitchenStack.add(kitchenBtn)
            val kitchenDotWrapper = Table(skin)
            kitchenDotWrapper.add(view.kitchenBadgeDot).size(12f, 12f).top().right().pad(4f, 0f, 0f, 4f)
            kitchenStack.add(kitchenDotWrapper)
            add(kitchenStack).top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
            row()

            view.kitchenButton = kitchenBtn

            view.codexButton = textButton("Codex", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = !model.kitchenUnlocked
                addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@MenuView.changeActiveView(ViewState.CODEX)
                    }
                })
            }
            row()

            view.observatoryButton = textButton("Observatory", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = !model.observatoryUnlocked
                addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@MenuView.changeActiveView(ViewState.OBSERVATORY)
                    }
                })
            }
            row()

            view.automationButton = textButton("Automation", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = !model.automationUnlocked
                addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@MenuView.changeActiveView(ViewState.AUTOMATION)
                    }
                })
            }
            row()

            textButton("Achievements", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@MenuView.changeActiveView(ViewState.ACHIEVEMENTS)
                    }
                })
            }
            row()

            textButton("Statistics", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                isDisabled = true
            }
            row()

            // Help button with unread-badge dot overlay
            val helpBtn = textButton("Help", Buttons.GREY_BUTTON_MEDIUM.skinKey) {
                addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@MenuView.changeActiveView(ViewState.HELP)
                    }
                })
            }
            view.helpBadgeDot = Image(orangeDot).apply { isVisible = helpViewModel.hasUnread }
            val helpStack = Stack()
            helpStack.add(helpBtn)
            val dotWrapper = Table(skin)
            dotWrapper.add(view.helpBadgeDot).size(12f, 12f).top().right().pad(4f, 0f, 0f, 4f)
            helpStack.add(dotWrapper)

            add(helpStack).top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
            row()

            textButton("Settings", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@MenuView.changeActiveView(ViewState.SETTINGS)
                    }
                })
            }
            row()

            textButton("Reset Game", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        log.debug { "Reset Game" }
                        stage.fire(ResetGameEvent())
                    }
                })
            }
            row()

            textButton("Quit Game", Buttons.GREY_BUTTON_MEDIUM.skinKey) { cell ->
                cell.top().left().width(200f).height(45f).pad(2f, 2f, 2f, 2f)
                addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        log.debug { "Save & Quit Game" }
                        stage.fire(SaveGameEvent())
                        stage.fire(QuitGameEvent())
                    }
                })
            }

            tableCell.expand().fill()
        }

        // Model property bindings
        model.onPropertyChange(MenuModel::barnUnlocked) { unlocked ->
            barnButton.isDisabled = !unlocked
        }
        model.onPropertyChange(MenuModel::kitchenUnlocked) { unlocked ->
            kitchenButton.isDisabled = !unlocked
            codexButton.isDisabled   = !unlocked
        }
        model.onPropertyChange(MenuModel::observatoryUnlocked) { unlocked ->
            observatoryButton.isDisabled = !unlocked
        }
        model.onPropertyChange(MenuModel::automationUnlocked) { unlocked ->
            automationButton.isDisabled = !unlocked
        }
        model.onPropertyChange(MenuModel::kitchenHasExhausted) { exhausted ->
            kitchenBadgeDot.isVisible = exhausted
        }
        helpViewModel.onPropertyChange(HelpViewModel::hasUnread) { unread ->
            helpBadgeDot.isVisible = unread
        }
    }

    override fun handle(event: Event): Boolean {
        if (event is SettingsClosedEvent) {
            changeActiveView(ViewState.FARM)
            return true
        }
        return false
    }

    private fun changeActiveView(state: ViewState) {
        farmView.isVisible         = state == ViewState.FARM
        barnView.isVisible         = state == ViewState.BARN
        kitchenView.isVisible      = state == ViewState.KITCHEN
        codexView.isVisible        = state == ViewState.CODEX
        achievementsView.isVisible = state == ViewState.ACHIEVEMENTS
        settingsView.isVisible     = state == ViewState.SETTINGS
        observatoryView.isVisible  = state == ViewState.OBSERVATORY
        helpView.isVisible         = state == ViewState.HELP
        automationView.isVisible   = state == ViewState.AUTOMATION

        if (state == ViewState.SETTINGS) {
            stage.fire(SettingsOpenEvent())
        }
        stage.fire(ViewStateChangeEvent(state))
    }

    companion object {
        private val log = logger<MenuView>()
    }
}

@Scene2dDsl
fun <S> KWidget<S>.menuView(
    model: MenuModel,
    helpViewModel: HelpViewModel,
    stage: Stage,
    farmView: Table,
    barnView: Table,
    kitchenView: Table,
    codexView: Table,
    achievementsView: Table,
    settingsView: Table,
    observatoryView: Table,
    helpView: Table,
    automationView: Table,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: MenuView.(S) -> Unit = {},
): MenuView = actor(
    MenuView(
        model, helpViewModel, skin, stage,
        farmView, barnView, kitchenView, codexView,
        achievementsView, settingsView, observatoryView, helpView,
        automationView,
    ),
    init
)
