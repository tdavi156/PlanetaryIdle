package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.github.jacks.planetaryIdle.events.BuyFoodEvent
import com.github.jacks.planetaryIdle.events.QuitGameEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.models.PlanetModel
import ktx.actors.txt
import ktx.log.logger
import ktx.scene2d.*

class PlanetView(
    model : PlanetModel,
    skin : Skin,
) : Table(skin), KTable {

    private lateinit var stage : Stage
    private var currentView : String = "planetView"

    // buttons
    private val planetButton : TextButton
    private val galaxyButton : TextButton
    private val automationButton : TextButton
    private val challengesButton : TextButton
    private val achievementsButton : TextButton
    private val statisticsButton : TextButton
    private val settingsButton : TextButton
    private val quitButton : TextButton

    private var buyWheatButton : TextButton
    private var buyCornButton : TextButton
    private var buyCabbageButton : TextButton
    private var buyPotatoesButton : TextButton

    // labels
    private var totalPopulationAmount : Label
    private var availablePopulationAmount : Label
    private var currentWheatAmount : Label
    private var currentCornAmount : Label
    private var currentCabbageAmount : Label
    private var currentPotatoesAmount : Label

    init {
        setFillParent(true)
        stage = getStage()

        // Left side menu buttons
        table { menuTableCell ->
            this@PlanetView.planetButton = textButton("Planet", Buttons.TEXT_BUTTON.skinKey) { cell ->
                cell.top().left().padTop(10f).width(180f).height(50f)
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@PlanetView.currentView = "planetView"
                        log.debug { "currentView -> ${this@PlanetView.currentView}" }
                    }
                })
            }
            row()
            this@PlanetView.galaxyButton = textButton("Galaxy", Buttons.TEXT_BUTTON.skinKey) { cell ->
                cell.top().left().padTop(10f).width(180f).height(50f)
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@PlanetView.currentView = "galaxyView"
                        log.debug { "currentView -> ${this@PlanetView.currentView}" }
                    }
                })
            }
            row()
            this@PlanetView.automationButton = textButton("Automation", Buttons.TEXT_BUTTON.skinKey) { cell ->
                cell.top().left().padTop(10f).width(180f).height(50f)
                isDisabled = true
            }
            row()
            this@PlanetView.challengesButton = textButton("Challenges", Buttons.TEXT_BUTTON.skinKey) { cell ->
                cell.top().left().padTop(10f).width(180f).height(50f)
                isDisabled = true
            }
            row()
            this@PlanetView.achievementsButton = textButton("Achievements", Buttons.TEXT_BUTTON.skinKey) { cell ->
                cell.top().left().padTop(10f).width(180f).height(50f)
                isDisabled = true
            }
            row()
            this@PlanetView.statisticsButton = textButton("Statistics", Buttons.TEXT_BUTTON.skinKey) { cell ->
                cell.top().left().padTop(10f).width(180f).height(50f)
                isDisabled = true
            }
            row()
            this@PlanetView.settingsButton = textButton("Settings", Buttons.TEXT_BUTTON.skinKey) { cell ->
                cell.top().left().padTop(10f).width(180f).height(50f)
                isDisabled = true
            }
            row()
            this@PlanetView.quitButton = textButton("Quit Game", Buttons.TEXT_BUTTON.skinKey) { cell ->
                cell.top().left().padTop(10f).width(180f).height(50f)
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        log.debug { "Quit Game" }
                        stage.fire(QuitGameEvent())
                    }
                })
            }
            menuTableCell.fillX().top().pad(0f, 5f, 0f, 10f).width(180f)
        }

        // Right side game play table
        table { gameTableCell ->
            // Top information area
            table { topLabelCell ->
                label("You have ", Labels.MEDIUM.skinKey) { cell ->
                    cell.expandX().right().padRight(2f)
                }
                this@PlanetView.availablePopulationAmount = label("10", Labels.MEDIUM.skinKey) { cell ->
                    cell.center()
                }
                label(" Population", Labels.MEDIUM.skinKey) { cell ->
                    cell.expandX().left().padLeft(2f)
                }
                topLabelCell.expandX().top().padTop(10f).height(120f)
            }
            // add a line here do clearly indicate the upper and mid sections with an image
            row()

            // Middle actionable area
            table { tableCell ->
                table { foodTableCell ->
                    label("Wheat", Labels.MEDIUM.skinKey) { cell ->
                        cell.left().padRight(30f)
                    }
                    this@PlanetView.currentWheatAmount = label("0", Labels.MEDIUM.skinKey) { cell ->
                        cell.expand().left()
                    }
                    this@PlanetView.buyWheatButton = textButton("Buy Wheat", Buttons.TEXT_BUTTON.skinKey) { cell ->
                        cell.right().width(200f).height(50f)
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyFoodEvent("wheat"))
                            }
                        })
                    }
                    foodTableCell.expandX().fill().pad(10f, 10f, 10f, 10f).top()
                }
                row()
                table { foodTableCell ->
                    label("Corn", Labels.MEDIUM.skinKey) { cell ->
                        cell.left().padRight(30f)
                    }
                    this@PlanetView.currentCornAmount = label("0", Labels.MEDIUM.skinKey) { cell ->
                        cell.expand().left()
                    }
                    this@PlanetView.buyCornButton = textButton("Buy Corn", Buttons.TEXT_BUTTON.skinKey) { cell ->
                        cell.right().width(200f).height(50f)
                        isDisabled = true
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyFoodEvent("corn"))
                            }
                        })
                    }
                    foodTableCell.expandX().fill().pad(10f, 10f, 10f, 10f).top()
                }
                row()
                table { foodTableCell ->
                    label("Cabbage", Labels.MEDIUM.skinKey) { cell ->
                        cell.left().padRight(30f)
                    }
                    this@PlanetView.currentCabbageAmount = label("0", Labels.MEDIUM.skinKey) { cell ->
                        cell.expand().left()
                    }
                    this@PlanetView.buyCabbageButton = textButton("Buy Cabbage", Buttons.TEXT_BUTTON.skinKey) { cell ->
                        cell.right().width(200f).height(50f)
                        isDisabled = true
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyFoodEvent("cabbage"))
                            }
                        })
                    }
                    foodTableCell.expandX().fill().pad(10f, 10f, 10f, 10f).top()
                }
                row()
                table { foodTableCell ->
                    label("Potatoes", Labels.MEDIUM.skinKey) { cell ->
                        cell.left().padRight(30f)
                    }
                    this@PlanetView.currentPotatoesAmount = label("0", Labels.MEDIUM.skinKey) { cell ->
                        cell.expand().left()
                    }
                    this@PlanetView.buyPotatoesButton = textButton("Buy Potatoes", Buttons.TEXT_BUTTON.skinKey) { cell ->
                        cell.right().width(200f).height(50f)
                        isDisabled = true
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyFoodEvent("potatoes"))
                            }
                        })
                    }
                    foodTableCell.expandX().fill().pad(10f, 10f, 10f, 10f).top()
                }
                tableCell.expand().fill()
            }

            // add a line here do clearly indicate the mid and lower sections
            row()

            // Bottom progress bar
            table { tableCell ->
                label("Progress towards colonization: ", Labels.MEDIUM.skinKey) { cell ->
                    cell.expandX().right().padRight(2f)
                }
                this@PlanetView.totalPopulationAmount = label("10", Labels.MEDIUM.skinKey) { cell ->
                    cell.center()
                }
                label(" / 1,000,000", Labels.MEDIUM.skinKey) { cell ->
                    cell.expandX().left().padLeft(2f)
                }
                tableCell.expandX().top().height(100f)
            }
            gameTableCell.expand().fill().pad(5f)
        }

        // Data Binding
        model.onPropertyChange(PlanetModel::totalPopulationAmount) { popAmount ->
            totalPopAmountChange(popAmount)
        }
        model.onPropertyChange(PlanetModel::availablePopulationAmount) { popAmount ->
            popAmountChange(popAmount)
            updateAvailable(popAmount)
        }
        model.onPropertyChange(PlanetModel::wheatAmount) { amount ->
            wheatAmountChange(amount)
        }
        model.onPropertyChange(PlanetModel::cornAmount) { amount ->
            cornAmountChange(amount)
        }
        model.onPropertyChange(PlanetModel::cabbageAmount) { amount ->
            cabbageAmountChange(amount)
        }
        model.onPropertyChange(PlanetModel::potatoesAmount) { amount ->
            potatoesAmountChange(amount)
        }
    }

    private fun totalPopAmountChange(amount : Int) {
        totalPopulationAmount.txt = "%,d".format(amount)
    }
    private fun popAmountChange(amount : Int) {
        availablePopulationAmount.txt = amount.toString()
    }
    private fun updateAvailable(amount : Int) {
        buyWheatButton.isDisabled = amount < 10
        buyCornButton.isDisabled = amount < 100
        buyCabbageButton.isDisabled = amount < 1000
        buyPotatoesButton.isDisabled = amount < 10000
    }
    private fun wheatAmountChange(amount : Int) {
        currentWheatAmount.txt = amount.toString()
    }
    private fun cornAmountChange(amount : Int) {
        currentCornAmount.txt = amount.toString()
    }
    private fun cabbageAmountChange(amount : Int) {
        currentCabbageAmount.txt = amount.toString()
    }
    private fun potatoesAmountChange(amount : Int) {
        currentPotatoesAmount.txt = amount.toString()
    }

    companion object {
        private val log = logger<PlanetView>()
    }
}

@Scene2dDsl
fun <S> KWidget<S>.planetView(
    model : PlanetModel,
    skin : Skin = Scene2DSkin.defaultSkin,
    init : PlanetView.(S) -> Unit = { }
) : PlanetView = actor(PlanetView(model, skin), init)
