package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.github.jacks.planetaryIdle.events.BuyResourceEvent
import com.github.jacks.planetaryIdle.events.GameCompletedEvent
import com.github.jacks.planetaryIdle.events.QuitGameEvent
import com.github.jacks.planetaryIdle.events.ResetGameEvent
import com.github.jacks.planetaryIdle.events.UpdateBuyAmountEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.get
import com.github.jacks.planetaryIdle.ui.models.PlanetModel
import ktx.actors.txt
import ktx.log.logger
import ktx.preferences.get
import ktx.scene2d.*
import kotlin.math.roundToInt

class PlanetView(
    model : PlanetModel,
    skin : Skin,
) : Table(skin), KTable {

    private lateinit var stage : Stage
    private var currentView : String = "planetView"
    private val preferences : Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }

    private var buyAmount : Float = preferences["buyAmount", 1f]

    private var availablePopAmount = 10

    private var wheatBaseCost : Float = 10f
    private var cornBaseCost : Float = 100f
    private var cabbageBaseCost : Float = 1000f
    private var potatoesBaseCost : Float = 10000f

    private var wheatCurrentCost : Float = 10f
    private var cornCurrentCost : Float = 100f
    private var cabbageCurrentCost : Float = 1000f
    private var potatoesCurrentCost : Float = 10000f

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

    private val setPurchaseAmountButton : TextButton

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

    // images
    private var colonizationProgress : Image

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
                this@PlanetView.setPurchaseAmountButton = textButton("Buy ${this@PlanetView.buyAmount.roundToInt()}", Buttons.BLUE_TEXT_BUTTON_SMALL.skinKey) { cell ->
                    cell.expand().top().right().width(90f).height(30f).pad(10f, 0f, 0f, 10f)
                    this.addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) {
                            when (this@PlanetView.buyAmount) {
                                1f -> this@PlanetView.buyAmount = 10f
                                10f -> this@PlanetView.buyAmount = 100f
                                100f -> this@PlanetView.buyAmount = 1f
                                else -> this@PlanetView.buyAmount = 1f
                            }
                            stage.fire(UpdateBuyAmountEvent(this@PlanetView.buyAmount))
                        }
                    })
                }
                row()
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
                    this@PlanetView.assignWheatButton = textButton(this@PlanetView.formatBuyButton("wheat"), Buttons.GREEN_TEXT_BUTTON_SMALL.skinKey) { cell ->
                        cell.right().width(200f).height(50f)
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("wheat"))
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
                    this@PlanetView.assignCornButton = textButton(this@PlanetView.formatBuyButton("corn"), Buttons.GREEN_TEXT_BUTTON_SMALL.skinKey) { cell ->
                        cell.right().width(200f).height(50f)
                        isDisabled = true
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("corn"))
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
                    this@PlanetView.assignCabbageButton = textButton(this@PlanetView.formatBuyButton("cabbage"), Buttons.GREEN_TEXT_BUTTON_SMALL.skinKey) { cell ->
                        cell.right().width(200f).height(50f)
                        isDisabled = true
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("cabbage"))
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
                    this@PlanetView.assignPotatoesButton = textButton(this@PlanetView.formatBuyButton("potatoes"), Buttons.GREEN_TEXT_BUTTON_SMALL.skinKey) { cell ->
                        cell.right().width(200f).height(50f)
                        isDisabled = true
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("potatoes"))
                            }
                        })
                    }
                    foodTableCell.expandX().fill().pad(10f, 10f, 10f, 10f).top()
                }
                tableCell.expand().fill()
            }

            row().expand().fill()
            image(skin[Drawables.BAR_GREEN_THIN]) { cell ->
                cell.expandX().fillX().height(2f)
            }
            row()

            // Bottom progress bar
            table { tableCell ->
                stack { stackCell ->
                    image(skin[Drawables.BAR_GREY_THICK])
                    this@PlanetView.colonizationProgress = image(skin[Drawables.BAR_GREEN_THICK]) { cell ->
                        scaleX = 0f
                    }
                    this@PlanetView.totalPopulation = label("10 / 1,000,000,000 (0.00 %)", Labels.MEDIUM.skinKey) { cell ->
                        setAlignment(Align.center)
                    }
                    stackCell.center().width(600f).height(30f)
                }
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
        model.onPropertyChange(PlanetModel::buyAmount) { amount ->
            setPurchaseAmountButton.txt = "Buy ${amount.roundToInt()}"
            wheatCurrentCost = wheatBaseCost * buyAmount
            cornCurrentCost = cornBaseCost * buyAmount
            cabbageCurrentCost = cabbageBaseCost * buyAmount
            potatoesCurrentCost = potatoesBaseCost * buyAmount
            assignWheatButton.txt = formatBuyButton("wheat")
            assignCornButton.txt = formatBuyButton("corn")
            assignCabbageButton.txt = formatBuyButton("cabbage")
            assignPotatoesButton.txt = formatBuyButton("potatoes")
            updateAvailable(availablePopAmount)
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
        totalPopulation.txt = "${"%,d".format(amount.coerceAtMost(MAX_POP_AMOUNT))} / ${"%,d".format(MAX_POP_AMOUNT)} (${"%.2f".format(amount.toFloat() / MAX_POP_AMOUNT.toFloat())} %)"
        colonizationProgress.scaleX = (amount.toFloat() / MAX_POP_AMOUNT.toFloat()).coerceAtMost(1f)
    }
    private fun popAmountChange(amount : Int) {
        availablePopAmount = amount
        availablePopulation.txt = "You have ${"%,d".format(amount)} available population. (AP)"
    }
    private fun popGainRateChange(amount : Float) {
        populationGainPerSecond.txt = "You are gaining ${"%.2f".format(amount)} population per second."
    }
    private fun updateAvailable(amount : Int) {
        assignWheatButton.isDisabled = amount < wheatCurrentCost
        assignCornButton.isDisabled = amount < cornCurrentCost
        assignCabbageButton.isDisabled = amount < cabbageCurrentCost
        assignPotatoesButton.isDisabled = amount < potatoesCurrentCost
    }
    private fun wheatAmountChange(amount : Int) {
        wheatOwned.txt = "%,d".format(amount)
    }
    private fun wheatMultiplierChange(multiplier : Float) {
        wheatMultiplier.txt = "x${"%.2f".format(multiplier)}"
    }
    private fun wheatCostChange(cost : Float) {
        wheatBaseCost = cost
        assignWheatButton.txt = formatBuyButton("wheat")
    }
    private fun cornAmountChange(amount : Int) {
        cornOwned.txt = "%,d".format(amount)
    }
    private fun cornMultiplierChange(multiplier : Float) {
        cornMultiplier.txt = "x${"%.2f".format(multiplier)}"
    }
    private fun cornCostChange(cost : Float) {
        cornBaseCost = cost
        assignCornButton.txt = formatBuyButton("corn")
    }
    private fun cabbageAmountChange(amount : Int) {
        cabbageOwned.txt = "%,d".format(amount)
    }
    private fun cabbageMultiplierChange(multiplier : Float) {
        cabbageMultiplier.txt = "x${"%.2f".format(multiplier)}"
    }
    private fun cabbageCostChange(cost : Float) {
        cabbageBaseCost = cost
        assignCabbageButton.txt = formatBuyButton("cabbage")
    }
    private fun potatoesAmountChange(amount : Int) {
        potatoesOwned.txt = "%,d".format(amount)
    }
    private fun potatoesMultiplierChange(multiplier : Float) {
        potatoesMultiplier.txt = "x${"%.2f".format(multiplier)}"
    }
    private fun potatoesCostChange(cost : Float) {
        potatoesBaseCost = cost
        assignPotatoesButton.txt = formatBuyButton("potatoes")
    }
    private fun getCost(name : String) : Float {
        return when (name) {
            "wheat" -> wheatCurrentCost
            "corn" -> cornCurrentCost
            "cabbage" -> cabbageCurrentCost
            "potatoes" -> potatoesCurrentCost
            else -> 1f
        }
    }
    private fun formatBuyButton(name : String) : String {
        return if (getCost(name) > 9_999f) {
            "Buy ${this@PlanetView.buyAmount.roundToInt()}\n${"%,d".format(getCost(name).roundToInt())} AP"
        } else {
            "Buy ${this@PlanetView.buyAmount.roundToInt()}\nAssign: ${"%,d".format(getCost(name).roundToInt())} AP"
        }
    }
    private fun checkForGameEnd(amount : Int) {
        // check that the game is not already ended so we dont call multiple times
        if (amount >= 1_000_000_000) {
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
        const val MAX_POP_AMOUNT = 1_000_000
    }
}

@Scene2dDsl
fun <S> KWidget<S>.planetView(
    model : PlanetModel,
    skin : Skin = Scene2DSkin.defaultSkin,
    init : PlanetView.(S) -> Unit = { }
) : PlanetView = actor(PlanetView(model, skin), init)
