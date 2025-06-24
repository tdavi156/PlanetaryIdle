package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.github.jacks.planetaryIdle.events.AssignPopEvent
import com.github.jacks.planetaryIdle.events.GameCompletedEvent
import com.github.jacks.planetaryIdle.events.QuitGameEvent
import com.github.jacks.planetaryIdle.events.ResetGameEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.get
import com.github.jacks.planetaryIdle.ui.models.PlanetModel
import ktx.actors.txt
import ktx.log.logger
import ktx.scene2d.*
import kotlin.math.roundToInt

class PlanetView(
    model : PlanetModel,
    skin : Skin,
) : Table(skin), KTable {

    private lateinit var stage : Stage
    private var currentView : String = "planetView"

    private var wheatCost : Int = 10
    private var cornCost : Int = 100
    private var cabbageCost : Int = 1000
    private var potatoesCost : Int = 10000

    // tables

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

    private var assignWheatButton : TextButton
    private var assignCornButton : TextButton
    private var assignCabbageButton : TextButton
    private var assignPotatoesButton : TextButton

    // labels
    private var totalPopulation : Label
    private var availablePopulation : Label
    private var populationGainPerSecond : Label
    private var gameCompleted : Label

    private var wheatOwned : Label
    private var wheatMultiplier : Label
    private var cornOwned : Label
    private var cornMultiplier : Label
    private var cabbageOwned : Label
    private var cabbageMultiplier : Label
    private var potatoesOwned : Label
    private var potatoesMultiplier : Label

    init {
        setFillParent(true)
        stage = getStage()

        // Left side menu buttons
        table { menuTableCell ->
            this@PlanetView.planetButton = textButton("Planet", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(50f)
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@PlanetView.currentView = "planetView"
                        log.debug { "currentView -> ${this@PlanetView.currentView}" }
                    }
                })
            }
            row()
            this@PlanetView.galaxyButton = textButton("Galaxy", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(50f)
                isDisabled = true
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        this@PlanetView.currentView = "galaxyView"
                        log.debug { "currentView -> ${this@PlanetView.currentView}" }
                    }
                })
            }
            row()
            this@PlanetView.automationButton = textButton("Automation", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(50f)
                isDisabled = true
            }
            row()
            this@PlanetView.challengesButton = textButton("Challenges", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(50f)
                isDisabled = true
            }
            row()
            this@PlanetView.achievementsButton = textButton("Achievements", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(50f)
                isDisabled = true
            }
            row()
            this@PlanetView.statisticsButton = textButton("Statistics", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(50f)
                isDisabled = true
            }
            row()
            this@PlanetView.settingsButton = textButton("Settings", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(50f)
                isDisabled = true
            }
            row()
            this@PlanetView.resetButton = textButton("Reset Game", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(50f)
                isDisabled = true
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        log.debug { "Reset Game" }
                        stage.fire(ResetGameEvent())
                    }
                })
            }
            row()
            this@PlanetView.quitButton = textButton("Quit Game", Buttons.BLUE_TEXT_BUTTON_DEFAULT.skinKey) { cell ->
                cell.top().left().width(180f).height(50f)
                this.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        log.debug { "Quit Game" }
                        stage.fire(QuitGameEvent())
                    }
                })
            }
            menuTableCell.fillX().top().width(180f)
        }

        // Right side game play table
        table { gameTableCell ->

            // Top information area
            table { topLabelCell ->
                this@PlanetView.availablePopulation = label("You have 10 available population. (AP)", Labels.DEFAULT.skinKey) { cell ->
                    cell.center().padBottom(10f)
                }
                row()
                this@PlanetView.populationGainPerSecond = label("You are gaining 0 population per second.", Labels.DEFAULT.skinKey) { cell ->
                    cell.center()
                }
                topLabelCell.expandX().top().padTop(10f).height(140f)
            }

            row()
            image(skin[Drawables.BAR_GREEN_THIN]) { cell ->
                cell.expandX().fillX().height(2f)
            }
            row()

            // Middle actionable area
            table { tableCell ->
                this@PlanetView.gameCompleted = label("Congratulations on colonizing the planet!", Labels.LARGE.skinKey) { cell ->
                    cell.center().pad(20f, 0f, 10f, 0f)
                    isVisible = false
                }
                row()
                table { foodTableCell ->
                    table { foodTableNameCell ->
                        label("Wheat Fields", Labels.MEDIUM.skinKey) { cell ->
                            cell.left().padRight(15f)
                        }
                        this@PlanetView.wheatMultiplier = label("x1.00", Labels.SMALL.skinKey) { cell ->
                            cell.expand().left()
                        }
                        foodTableNameCell.left().width(200f)
                    }
                    this@PlanetView.wheatOwned = label("0", Labels.MEDIUM.skinKey) { cell ->
                        cell.expandX().left()
                    }
                    this@PlanetView.assignWheatButton = textButton("Assign: 10 AP", Buttons.GREEN_TEXT_BUTTON_MEDIUM.skinKey) { cell ->
                        cell.right().width(200f).height(50f)
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(AssignPopEvent("wheat"))
                            }
                        })
                    }
                    foodTableCell.expandX().fill().pad(10f, 10f, 10f, 10f).top()
                }
                row()
                image(skin[Drawables.BAR_GREEN_THIN]) { cell ->
                    cell.expandX().fillX().height(2f)
                }
                row()
                table { foodTableCell ->
                    table { foodTableNameCell ->
                        label("Corn Fields", Labels.MEDIUM.skinKey) { cell ->
                            cell.left().padRight(15f)
                        }
                        this@PlanetView.cornMultiplier = label("x1.00", Labels.SMALL.skinKey) { cell ->
                            cell.expand().left()
                        }
                        foodTableNameCell.left().width(200f)
                    }
                    this@PlanetView.cornOwned = label("0", Labels.MEDIUM.skinKey) { cell ->
                        cell.expandX().left()
                    }
                    this@PlanetView.assignCornButton = textButton("Assign: 100 AP", Buttons.GREEN_TEXT_BUTTON_MEDIUM.skinKey) { cell ->
                        cell.right().width(200f).height(50f)
                        isDisabled = true
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(AssignPopEvent("corn"))
                            }
                        })
                    }
                    foodTableCell.expandX().fill().pad(10f, 10f, 10f, 10f).top()
                }
                row()
                image(skin[Drawables.BAR_GREEN_THIN]) { cell ->
                    cell.expandX().fillX().height(2f)
                }
                row()
                table { foodTableCell ->
                    table { foodTableNameCell ->
                        label("Cabbage Fields", Labels.MEDIUM.skinKey) { cell ->
                            cell.left().padRight(15f)
                        }
                        this@PlanetView.cabbageMultiplier = label("x1.00", Labels.SMALL.skinKey) { cell ->
                            cell.expand().left()
                        }
                        foodTableNameCell.left().width(200f)
                    }
                    this@PlanetView.cabbageOwned = label("0", Labels.MEDIUM.skinKey) { cell ->
                        cell.expandX().left()
                    }
                    this@PlanetView.assignCabbageButton = textButton("Assign: 1,000 AP", Buttons.GREEN_TEXT_BUTTON_MEDIUM.skinKey) { cell ->
                        cell.right().width(200f).height(50f)
                        isDisabled = true
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(AssignPopEvent("cabbage"))
                            }
                        })
                    }
                    foodTableCell.expandX().fill().pad(10f, 10f, 10f, 10f).top()
                }
                row()
                image(skin[Drawables.BAR_GREEN_THIN]) { cell ->
                    cell.expandX().fillX().height(2f)
                }
                row()
                table { foodTableCell ->
                    table { foodTableNameCell ->
                        label("Potato Fields", Labels.MEDIUM.skinKey) { cell ->
                            cell.left().padRight(15f)
                        }
                        this@PlanetView.potatoesMultiplier = label("x1.00", Labels.SMALL.skinKey) { cell ->
                            cell.expand().left()
                        }
                        foodTableNameCell.left().width(200f)
                    }
                    this@PlanetView.potatoesOwned = label("0", Labels.MEDIUM.skinKey) { cell ->
                        cell.expandX().left()
                    }
                    this@PlanetView.assignPotatoesButton = textButton("Assign: 10,000 AP", Buttons.GREEN_TEXT_BUTTON_MEDIUM.skinKey) { cell ->
                        cell.right().width(200f).height(50f)
                        isDisabled = true
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(AssignPopEvent("potatoes"))
                            }
                        })
                    }
                    foodTableCell.expandX().fill().pad(10f, 10f, 10f, 10f).top()
                }
                tableCell.expand().fill()
            }

            row()
            image(skin[Drawables.BAR_GREEN_THIN]) { cell ->
                cell.expandX().fillX().height(2f)
            }
            row()

            // Bottom progress bar
            table { tableCell ->
                this@PlanetView.totalPopulation = label("Progress towards planet colonization: 10 / 1,000,000,000", Labels.DEFAULT.skinKey) { cell ->
                    cell.expandX().center()
                }
                // add progress bar
                tableCell.expandX().top().height(100f)
            }
            gameTableCell.expand().fill().pad(5f)
        }

        // Data Binding
        model.onPropertyChange(PlanetModel::totalPopulationAmount) { popAmount ->
            totalPopAmountChange(popAmount)
            checkForGameEnd(popAmount)
        }
        model.onPropertyChange(PlanetModel::availablePopulationAmount) { popAmount ->
            popAmountChange(popAmount)
            updateAvailable(popAmount)
        }
        model.onPropertyChange(PlanetModel::populationGainPerSecond) { popAmount ->
            popGainRateChange(popAmount)
        }
        model.onPropertyChange(PlanetModel::wheatAmount) { amount ->
            wheatAmountChange(amount)
        }
        model.onPropertyChange(PlanetModel::wheatMultiplier) { multiplier ->
            wheatMultiplierChange(multiplier)
        }
        model.onPropertyChange(PlanetModel::wheatCost) { cost ->
            wheatCostChange(cost)
        }
        model.onPropertyChange(PlanetModel::cornAmount) { amount ->
            cornAmountChange(amount)
        }
        model.onPropertyChange(PlanetModel::cornMultiplier) { multiplier ->
            cornMultiplierChange(multiplier)
        }
        model.onPropertyChange(PlanetModel::cornCost) { cost ->
            cornCostChange(cost)
        }
        model.onPropertyChange(PlanetModel::cabbageAmount) { amount ->
            cabbageAmountChange(amount)
        }
        model.onPropertyChange(PlanetModel::cabbageMultiplier) { multiplier ->
            cabbageMultiplierChange(multiplier)
        }
        model.onPropertyChange(PlanetModel::cabbageCost) { cost ->
            cabbageCostChange(cost)
        }
        model.onPropertyChange(PlanetModel::potatoesAmount) { amount ->
            potatoesAmountChange(amount)
        }
        model.onPropertyChange(PlanetModel::potatoesMultiplier) { multiplier ->
            potatoesMultiplierChange(multiplier)
        }
        model.onPropertyChange(PlanetModel::potatoesCost) { cost ->
            potatoesCostChange(cost)
        }
        model.onPropertyChange(PlanetModel::gameCompleted) { completed ->
            popupGameCompleted(completed)
        }
    }

    private fun totalPopAmountChange(amount : Int) {
        totalPopulation.txt = "Progress towards planet colonization: ${"%,d".format(amount)} / 1,000,000,000"
    }
    private fun popAmountChange(amount : Int) {
        availablePopulation.txt = "You have ${"%,d".format(amount)} available population. (AP)"
    }
    private fun popGainRateChange(amount : Float) {
        populationGainPerSecond.txt = "You are gaining ${"%,d".format(amount.roundToInt())} population per second."
    }
    private fun updateAvailable(amount : Int) {
        assignWheatButton.isDisabled = amount < wheatCost
        assignCornButton.isDisabled = amount < cornCost
        assignCabbageButton.isDisabled = amount < cabbageCost
        assignPotatoesButton.isDisabled = amount < potatoesCost
    }
    private fun wheatAmountChange(amount : Int) {
        wheatOwned.txt = "%,d".format(amount)
    }
    private fun wheatMultiplierChange(multiplier : Float) {
        wheatMultiplier.txt = "x${"%.2f".format(multiplier)}"
    }
    private fun wheatCostChange(cost : Float) {
        wheatCost = cost.roundToInt()
        assignWheatButton.txt = formatCost(cost)
    }
    private fun cornAmountChange(amount : Int) {
        cornOwned.txt = "%,d".format(amount)
    }
    private fun cornMultiplierChange(multiplier : Float) {
        cornMultiplier.txt = "x${"%.2f".format(multiplier)}"
    }
    private fun cornCostChange(cost : Float) {
        cornCost = cost.roundToInt()
        assignCornButton.txt = formatCost(cost)
    }
    private fun cabbageAmountChange(amount : Int) {
        cabbageOwned.txt = "%,d".format(amount)
    }
    private fun cabbageMultiplierChange(multiplier : Float) {
        cabbageMultiplier.txt = "x${"%.2f".format(multiplier)}"
    }
    private fun cabbageCostChange(cost : Float) {
        cabbageCost = cost.roundToInt()
        assignCabbageButton.txt = formatCost(cost)
    }
    private fun potatoesAmountChange(amount : Int) {
        potatoesOwned.txt = "%,d".format(amount)
    }
    private fun potatoesMultiplierChange(multiplier : Float) {
        potatoesMultiplier.txt = "x${"%.2f".format(multiplier)}"
    }
    private fun potatoesCostChange(cost : Float) {
        potatoesCost = cost.roundToInt()
        assignPotatoesButton.txt = formatCost(cost)
    }
    private fun formatCost(cost : Float) : String {
        return if (cost > 9999f) {
            "${"%.0f".format(cost)} AP"
        } else {
            "Assign: ${"%.0f".format(cost)} AP"
        }
    }
    private fun checkForGameEnd(amount : Int) {
        // check that the game is not already ended so we dont call multiple times
        if (amount >= 1000000000) {
            fire(GameCompletedEvent())
        }
    }
    private fun popupGameCompleted(completed : Boolean) {
        log.debug { "popupGameCompleted" }
        this@PlanetView.gameCompleted.isVisible = completed
        //menuTable.invalidateHierarchy()
        this@PlanetView.resetButton.isDisabled = !completed
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
