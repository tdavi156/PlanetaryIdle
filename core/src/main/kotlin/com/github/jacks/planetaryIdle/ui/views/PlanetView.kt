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
import com.github.jacks.planetaryIdle.events.SaveGameEvent
import com.github.jacks.planetaryIdle.events.UpdateBuyAmountEvent
import com.github.jacks.planetaryIdle.events.UpgradeSoilEvent
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
import java.math.BigDecimal
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import kotlin.math.roundToInt

class PlanetView(
    model : PlanetModel,
    skin : Skin,
) : Table(skin), KTable {

    private lateinit var stage : Stage
    private var currentView : String = "planetView"
    private val preferences : Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }

    private val decimalFormat : NumberFormat = NumberFormat.getInstance()
    private val noDecimalFormat : NumberFormat = NumberFormat.getInstance()
    private val expNoDecimalFormat : NumberFormat = DecimalFormat("0E0", DecimalFormatSymbols.getInstance())
    private val exp1DecimalFormat : NumberFormat = DecimalFormat("0.#E0", DecimalFormatSymbols.getInstance())
    private val exp2DecimalFormat : NumberFormat = DecimalFormat("0.##E0", DecimalFormatSymbols.getInstance())
    private val multNumFormat : NumberFormat = NumberFormat.getInstance()
    private val rateNumFormat : NumberFormat = NumberFormat.getInstance()
    private val amtNumFormat : NumberFormat = NumberFormat.getInstance()
    private val costNumFormat : NumberFormat = NumberFormat.getInstance()

    // initial values from preferences
    private var totalPopulation = BigDecimal(preferences["totalPopulation", "10"])
    private var availablePopulation = BigDecimal(preferences["availablePopulation", "10"])
    private var populationGainRate = BigDecimal(preferences["populationGainRate", "0"])
    private var buyAmount : Float = preferences["buyAmount", 1f]

    private var wheatAmount = BigInteger(preferences["wheat_amount", "0"])
    private var cornAmount = BigInteger(preferences["corn_amount", "0"])
    private var lettuceAmount = BigInteger(preferences["lettuce_amount", "0"])
    private var carrotsAmount = BigInteger(preferences["carrots_amount", "0"])
    private var tomatoesAmount = BigInteger(preferences["tomatoes_amount", "0"])
    private var broccoliAmount = BigInteger(preferences["broccoli_amount", "0"])
    private var onionsAmount = BigInteger(preferences["onions_amount", "0"])
    private var potatoesAmount = BigInteger(preferences["potatoes_amount", "0"])

    private var wheatMultiplier = BigDecimal(preferences["wheat_multiplier", "1"])
    private var cornMultiplier = BigDecimal(preferences["corn_multiplier", "1"])
    private var lettuceMultiplier = BigDecimal(preferences["lettuce_multiplier", "1"])
    private var carrotsMultiplier = BigDecimal(preferences["carrots_multiplier", "1"])
    private var tomatoesMultiplier = BigDecimal(preferences["tomatoes_multiplier", "1"])
    private var broccoliMultiplier = BigDecimal(preferences["broccoli_multiplier", "1"])
    private var onionsMultiplier = BigDecimal(preferences["onions_multiplier", "1"])
    private var potatoesMultiplier = BigDecimal(preferences["potatoes_multiplier", "1"])

    private var wheatCost = BigDecimal(preferences["wheat_cost", "10"])
    private var cornCost = BigDecimal(preferences["corn_cost", "100"])
    private var lettuceCost = BigDecimal(preferences["lettuce_cost", "1000"])
    private var carrotsCost = BigDecimal(preferences["carrots_cost", "10000"])
    private var tomatoesCost = BigDecimal(preferences["tomatoes_cost", "100000"])
    private var broccoliCost = BigDecimal(preferences["broccoli_cost", "1000000"])
    private var onionsCost = BigDecimal(preferences["onions_cost", "10000000"])
    private var potatoesCost = BigDecimal(preferences["potatoes_cost", "100000000"])

    // tables
    private val wheatTable : Table
    private val cornTable : Table
    private val lettuceTable : Table
    private val carrotsTable : Table
    private val tomatoesTable : Table
    private val broccoliTable : Table
    private val onionsTable : Table
    private val potatoesTable : Table

    // buttons
    private val setBuyAmountButton : TextButton

    private var wheatButton : TextButton
    private var cornButton : TextButton
    private var lettuceButton : TextButton
    private var carrotsButton : TextButton
    private var tomatoesButton : TextButton
    private var broccoliButton : TextButton
    private var onionsButton : TextButton
    private var potatoesButton : TextButton

    private var soilButton : TextButton

    // labels
    private var totalPopulationLabel : Label
    private var availablePopulationLabel : Label
    private var populationGainPerSecondLabel : Label

    private var wheatAmountLabel : Label
    private var cornAmountLabel : Label
    private var lettuceAmountLabel : Label
    private var carrotsAmountLabel : Label
    private var tomatoesAmountLabel : Label
    private var broccoliAmountLabel : Label
    private var onionsAmountLabel : Label
    private var potatoesAmountLabel : Label

    private var wheatMultiplierLabel : Label
    private var cornMultiplierLabel : Label
    private var lettuceMultiplierLabel : Label
    private var carrotsMultiplierLabel : Label
    private var tomatoesMultiplierLabel : Label
    private var broccoliMultiplierLabel : Label
    private var onionsMultiplierLabel : Label
    private var potatoesMultiplierLabel : Label

    private var soilLabel : Label

    // images
    private var colonizationProgress : Image

    init {
        setFillParent(true)
        stage = getStage()
        setupNumberFormating()

        // Game play table
        table { gameTableCell ->
            // 1st row information area
            table { tableCell ->
                this@PlanetView.availablePopulationLabel = label("You have ${this@PlanetView.availablePopulation} available population. (AP)", Labels.DEFAULT.skinKey) { cell ->
                    cell.center().padBottom(10f)
                }
                row()
                this@PlanetView.populationGainPerSecondLabel = label("You are gaining ${this@PlanetView.formatNumberWithDecimal(this@PlanetView.populationGainRate)} population per second.", Labels.DEFAULT.skinKey) { cell ->
                    cell.center()
                }
                tableCell.expandX().top().padTop(10f).height(100f)
            }

            row()
            image(skin[Drawables.BAR_GREEN_THIN]) { cell ->
                cell.expandX().fillX().height(2f)
            }
            row()

            // 2nd row buttons area
            table { tableCell ->
                table { innerTableCell ->
                    this@PlanetView.soilLabel = label("Improved Soil (0)", Labels.SMALL.skinKey) { cell ->

                    }
                    row()
                    this@PlanetView.soilButton = textButton(
                        "Reset all crops and\n" +
                            "start over with better soil.\n" +
                            "Unlock the next crop.",
                        Buttons.GREEN_TEXT_BUTTON_SMALL.skinKey
                    ) { cell ->
                        cell.left().width(160f).height(80f).pad(5f)
                        isDisabled = false
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(UpgradeSoilEvent())
                            }
                        })
                    }
                    innerTableCell.expandX().left().width(180f).height(90f)
                }
                this@PlanetView.setBuyAmountButton = textButton("Buy ${this@PlanetView.buyAmount.roundToInt()}", Buttons.BLUE_TEXT_BUTTON_SMALL.skinKey) { cell ->
                    this.addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) {
                            when (this@PlanetView.buyAmount) {
                                1f -> this@PlanetView.buyAmount = 10f
                                10f -> this@PlanetView.buyAmount = 100f
                                100f -> this@PlanetView.buyAmount = 1f
                                else -> this@PlanetView.buyAmount = 1f
                            }
                            log.debug { "BuyAmount: ${this@PlanetView.buyAmount}" }
                            stage.fire(UpdateBuyAmountEvent(this@PlanetView.buyAmount))
                        }
                    })
                    cell.expandX().top().right().width(90f).height(30f).pad(10f, 10f, 20f, 10f)
                }
                tableCell.expandX().fillX().top().padTop(10f).height(80f)
            }
            row()

            // Middle actionable area
            table { tableCell ->
                this@PlanetView.wheatTable = table { foodTableCell ->
                    table { foodTableNameCell ->
                        label("Wheat Fields", Labels.MEDIUM.skinKey) { cell ->
                            cell.left().pad(0f, 10f, 0f, 15f)
                        }
                        this@PlanetView.wheatMultiplierLabel = label("x${this@PlanetView.multNumFormat.format(this@PlanetView.wheatMultiplier)}", Labels.SMALL.skinKey) { cell ->
                            cell.expand().left()
                        }
                        foodTableNameCell.left().width(200f)
                    }
                    this@PlanetView.wheatAmountLabel = label(this@PlanetView.amtNumFormat.format(this@PlanetView.wheatAmount), Labels.MEDIUM.skinKey) { cell ->
                        cell.expandX().left()
                    }
                    this@PlanetView.wheatButton = textButton(this@PlanetView.formatBuyButton(this@PlanetView.wheatCost), Buttons.GREEN_TEXT_BUTTON_SMALL.skinKey) { cell ->
                        cell.right().width(200f).height(50f).pad(5f)
                        isDisabled = this@PlanetView.availablePopulation < this@PlanetView.wheatCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("wheat"))
                            }
                        })
                    }
                    foodTableCell.expandX().fill().pad(0f, 10f, 0f, 10f).top()
                }
                row()
                this@PlanetView.cornTable = table { foodTableCell ->
                    table { foodTableNameCell ->
                        label("Corn Fields", Labels.MEDIUM.skinKey) { cell ->
                            cell.left().pad(0f, 10f, 0f, 15f)
                        }
                        this@PlanetView.cornMultiplierLabel = label("x${this@PlanetView.multNumFormat.format(this@PlanetView.cornMultiplier)}", Labels.SMALL.skinKey) { cell ->
                            cell.expand().left()
                        }
                        foodTableNameCell.left().width(200f)
                    }
                    this@PlanetView.cornAmountLabel = label(this@PlanetView.amtNumFormat.format(this@PlanetView.cornAmount), Labels.MEDIUM.skinKey) { cell ->
                        cell.expandX().left()
                    }
                    this@PlanetView.cornButton = textButton(this@PlanetView.formatBuyButton(this@PlanetView.cornCost), Buttons.GREEN_TEXT_BUTTON_SMALL.skinKey) { cell ->
                        cell.right().width(200f).height(50f).pad(5f)
                        isDisabled = this@PlanetView.availablePopulation < this@PlanetView.cornCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("corn"))
                            }
                        })
                    }
                    isVisible = false
                    background(skin[Drawables.BAR_GREEN_THICK_A25])
                    foodTableCell.expandX().fill().pad(0f, 10f, 0f, 10f).top()
                }
                row()
                this@PlanetView.lettuceTable = table { foodTableCell ->
                    table { foodTableNameCell ->
                        label("Lettuce Fields", Labels.MEDIUM.skinKey) { cell ->
                            cell.left().pad(0f, 10f, 0f, 15f)
                        }
                        this@PlanetView.lettuceMultiplierLabel = label("x${this@PlanetView.multNumFormat.format(this@PlanetView.lettuceMultiplier)}", Labels.SMALL.skinKey) { cell ->
                            cell.expand().left()
                        }
                        foodTableNameCell.left().width(200f)
                    }
                    this@PlanetView.lettuceAmountLabel = label(this@PlanetView.amtNumFormat.format(this@PlanetView.lettuceAmount), Labels.MEDIUM.skinKey) { cell ->
                        cell.expandX().left()
                    }
                    this@PlanetView.lettuceButton = textButton(this@PlanetView.formatBuyButton(this@PlanetView.lettuceCost), Buttons.GREEN_TEXT_BUTTON_SMALL.skinKey) { cell ->
                        cell.right().width(200f).height(50f).pad(5f)
                        isDisabled = this@PlanetView.availablePopulation < this@PlanetView.lettuceCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("lettuce"))
                            }
                        })
                    }
                    isVisible = false
                    foodTableCell.expandX().fill().pad(0f, 10f, 0f, 10f).top()
                }
                row()
                this@PlanetView.carrotsTable = table { foodTableCell ->
                    table { foodTableNameCell ->
                        label("Carrot Fields", Labels.MEDIUM.skinKey) { cell ->
                            cell.left().pad(0f, 10f, 0f, 15f)
                        }
                        this@PlanetView.carrotsMultiplierLabel = label("x${this@PlanetView.multNumFormat.format(this@PlanetView.carrotsMultiplier)}", Labels.SMALL.skinKey) { cell ->
                            cell.expand().left()
                        }
                        foodTableNameCell.left().width(200f)
                    }
                    this@PlanetView.carrotsAmountLabel = label(this@PlanetView.amtNumFormat.format(this@PlanetView.carrotsAmount), Labels.MEDIUM.skinKey) { cell ->
                        cell.expandX().left()
                    }
                    this@PlanetView.carrotsButton = textButton(this@PlanetView.formatBuyButton(this@PlanetView.carrotsCost), Buttons.GREEN_TEXT_BUTTON_SMALL.skinKey) { cell ->
                        cell.right().width(200f).height(50f).pad(5f)
                        isDisabled = this@PlanetView.availablePopulation < this@PlanetView.carrotsCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("carrots"))
                            }
                        })
                    }
                    isVisible = false
                    background(skin[Drawables.BAR_GREEN_THICK_A25])
                    foodTableCell.expandX().fill().pad(0f, 10f, 0f, 10f).top()
                }
                row()
                this@PlanetView.tomatoesTable = table { foodTableCell ->
                    table { foodTableNameCell ->
                        label("Tomato Fields", Labels.MEDIUM.skinKey) { cell ->
                            cell.left().pad(0f, 10f, 0f, 15f)
                        }
                        this@PlanetView.tomatoesMultiplierLabel = label("x${this@PlanetView.multNumFormat.format(this@PlanetView.tomatoesMultiplier)}", Labels.SMALL.skinKey) { cell ->
                            cell.expand().left()
                        }
                        foodTableNameCell.left().width(200f)
                    }
                    this@PlanetView.tomatoesAmountLabel = label(this@PlanetView.amtNumFormat.format(this@PlanetView.tomatoesAmount), Labels.MEDIUM.skinKey) { cell ->
                        cell.expandX().left()
                    }
                    this@PlanetView.tomatoesButton = textButton(this@PlanetView.formatBuyButton(this@PlanetView.tomatoesCost), Buttons.GREEN_TEXT_BUTTON_SMALL.skinKey) { cell ->
                        cell.right().width(200f).height(50f).pad(5f)
                        isDisabled = this@PlanetView.availablePopulation < this@PlanetView.tomatoesCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("tomatoes"))
                            }
                        })
                    }
                    isVisible = false
                    foodTableCell.expandX().fill().pad(0f, 10f, 0f, 10f).top()
                }
                row()
                this@PlanetView.broccoliTable = table { foodTableCell ->
                    table { foodTableNameCell ->
                        label("Broccoli Fields", Labels.MEDIUM.skinKey) { cell ->
                            cell.left().pad(0f, 10f, 0f, 15f)
                        }
                        this@PlanetView.broccoliMultiplierLabel = label("x${this@PlanetView.multNumFormat.format(this@PlanetView.broccoliMultiplier)}", Labels.SMALL.skinKey) { cell ->
                            cell.expand().left()
                        }
                        foodTableNameCell.left().width(200f)
                    }
                    this@PlanetView.broccoliAmountLabel = label(this@PlanetView.amtNumFormat.format(this@PlanetView.broccoliAmount), Labels.MEDIUM.skinKey) { cell ->
                        cell.expandX().left()
                    }
                    this@PlanetView.broccoliButton = textButton(this@PlanetView.formatBuyButton(this@PlanetView.broccoliCost), Buttons.GREEN_TEXT_BUTTON_SMALL.skinKey) { cell ->
                        cell.right().width(200f).height(50f).pad(5f)
                        isDisabled = this@PlanetView.availablePopulation < this@PlanetView.broccoliCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("broccoli"))
                            }
                        })
                    }
                    isVisible = false
                    background(skin[Drawables.BAR_GREEN_THICK_A25])
                    foodTableCell.expandX().fill().pad(0f, 10f, 0f, 10f).top()
                }
                row()
                this@PlanetView.onionsTable = table { foodTableCell ->
                    table { foodTableNameCell ->
                        label("Onion Fields", Labels.MEDIUM.skinKey) { cell ->
                            cell.left().pad(0f, 10f, 0f, 15f)
                        }
                        this@PlanetView.onionsMultiplierLabel = label("x${this@PlanetView.multNumFormat.format(this@PlanetView.onionsMultiplier)}", Labels.SMALL.skinKey) { cell ->
                            cell.expand().left()
                        }
                        foodTableNameCell.left().width(200f)
                    }
                    this@PlanetView.onionsAmountLabel = label(this@PlanetView.amtNumFormat.format(this@PlanetView.onionsAmount), Labels.MEDIUM.skinKey) { cell ->
                        cell.expandX().left()
                    }
                    this@PlanetView.onionsButton = textButton(this@PlanetView.formatBuyButton(this@PlanetView.onionsCost), Buttons.GREEN_TEXT_BUTTON_SMALL.skinKey) { cell ->
                        cell.right().width(200f).height(50f).pad(5f)
                        isDisabled = this@PlanetView.availablePopulation < this@PlanetView.onionsCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("onions"))
                            }
                        })
                    }
                    isVisible = false
                    foodTableCell.expandX().fill().pad(0f, 10f, 0f, 10f).top()
                }
                row()
                this@PlanetView.potatoesTable = table { foodTableCell ->
                    table { foodTableNameCell ->
                        label("Potato Fields", Labels.MEDIUM.skinKey) { cell ->
                            cell.left().pad(0f, 10f, 0f, 15f)
                        }
                        this@PlanetView.potatoesMultiplierLabel = label("x${this@PlanetView.multNumFormat.format(this@PlanetView.potatoesMultiplier)}", Labels.SMALL.skinKey) { cell ->
                            cell.expand().left()
                        }
                        foodTableNameCell.left().width(200f)
                    }
                    this@PlanetView.potatoesAmountLabel = label(this@PlanetView.amtNumFormat.format(this@PlanetView.potatoesAmount), Labels.MEDIUM.skinKey) { cell ->
                        cell.expandX().left()
                    }
                    this@PlanetView.potatoesButton = textButton(this@PlanetView.formatBuyButton(this@PlanetView.potatoesCost), Buttons.GREEN_TEXT_BUTTON_SMALL.skinKey) { cell ->
                        cell.right().width(200f).height(50f).pad(5f)
                        isDisabled = this@PlanetView.availablePopulation < this@PlanetView.potatoesCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("potatoes"))
                            }
                        })
                    }
                    isVisible = false
                    background(skin[Drawables.BAR_GREEN_THICK_A25])
                    foodTableCell.expandX().fill().pad(0f, 10f, 0f, 10f).top()
                }
                tableCell.expand().fill().top()
            }
            row()

            // Bottom progress bar
            table { tableCell ->
                stack { stackCell ->
                    image(skin[Drawables.BAR_GREY_THICK])
                    this@PlanetView.colonizationProgress = image(skin[Drawables.BAR_GREEN_THICK]) { cell ->
                        scaleX = 0f
                    }
                    this@PlanetView.totalPopulationLabel = label(
                        "${"%.2f".format((this@PlanetView.totalPopulation / MAX_POP_AMOUNT.toBigDecimal()).toFloat().coerceAtMost(1f) * 100f)} %"
                        , Labels.MEDIUM.skinKey
                    ) { cell ->
                        setAlignment(Align.center)
                    }
                    stackCell.center().width(600f).height(30f)
                }
                tableCell.expandX().top().height(40f).padTop(15f)
            }
            gameTableCell.expand().fill().pad(5f, 180f, 0f, 0f)
        }

        // Data Binding
        model.onPropertyChange(PlanetModel::totalPopulationAmount) { amount -> totalPopAmountChange(amount) }
        model.onPropertyChange(PlanetModel::availablePopulationAmount) { amount -> availablePopAmountChange(amount) }
        model.onPropertyChange(PlanetModel::populationGainPerSecond) { amount -> popGainRateChange(amount) }
        model.onPropertyChange(PlanetModel::buyAmount) { amount -> buyAmountChange(amount) }

        model.onPropertyChange(PlanetModel::wheatAmount) { amount -> wheatAmountChange(amount) }
        model.onPropertyChange(PlanetModel::cornAmount) { amount -> cornAmountChange(amount) }
        model.onPropertyChange(PlanetModel::lettuceAmount) { amount -> lettuceAmountChange(amount) }
        model.onPropertyChange(PlanetModel::carrotsAmount) { amount -> carrotsAmountChange(amount) }
        model.onPropertyChange(PlanetModel::tomatoesAmount) { amount -> tomatoesAmountChange(amount) }
        model.onPropertyChange(PlanetModel::broccoliAmount) { amount -> broccoliAmountChange(amount) }
        model.onPropertyChange(PlanetModel::onionsAmount) { amount -> onionsAmountChange(amount) }
        model.onPropertyChange(PlanetModel::potatoesAmount) { amount -> potatoesAmountChange(amount) }

        model.onPropertyChange(PlanetModel::wheatMultiplier) { multiplier -> wheatMultiplierChange(multiplier) }
        model.onPropertyChange(PlanetModel::cornMultiplier) { multiplier -> cornMultiplierChange(multiplier) }
        model.onPropertyChange(PlanetModel::lettuceMultiplier) { multiplier -> lettuceMultiplierChange(multiplier) }
        model.onPropertyChange(PlanetModel::carrotsMultiplier) { multiplier -> carrotsMultiplierChange(multiplier) }
        model.onPropertyChange(PlanetModel::tomatoesMultiplier) { multiplier -> tomatoesMultiplierChange(multiplier) }
        model.onPropertyChange(PlanetModel::broccoliMultiplier) { multiplier -> broccoliMultiplierChange(multiplier) }
        model.onPropertyChange(PlanetModel::onionsMultiplier) { multiplier -> onionsMultiplierChange(multiplier) }
        model.onPropertyChange(PlanetModel::potatoesMultiplier) { multiplier -> potatoesMultiplierChange(multiplier) }

        model.onPropertyChange(PlanetModel::wheatCost) { cost -> wheatCostChange(cost) }
        model.onPropertyChange(PlanetModel::cornCost) { cost -> cornCostChange(cost) }
        model.onPropertyChange(PlanetModel::lettuceCost) { cost -> lettuceCostChange(cost) }
        model.onPropertyChange(PlanetModel::carrotsCost) { cost -> carrotsCostChange(cost) }
        model.onPropertyChange(PlanetModel::tomatoesCost) { cost -> tomatoesCostChange(cost) }
        model.onPropertyChange(PlanetModel::broccoliCost) { cost -> broccoliCostChange(cost) }
        model.onPropertyChange(PlanetModel::onionsCost) { cost -> onionsCostChange(cost) }
        model.onPropertyChange(PlanetModel::potatoesCost) { cost -> potatoesCostChange(cost) }

        model.onPropertyChange(PlanetModel::gameCompleted) { completed -> popupGameCompleted(completed) }
    }

    private fun totalPopAmountChange(amount : BigDecimal) {
        val popPercent = (amount / MAX_POP_AMOUNT.toBigDecimal()).toFloat().coerceAtMost(1f)
        totalPopulationLabel.txt = "${"%.2f".format(popPercent * 100f)} %"
        colonizationProgress.scaleX = popPercent
        checkForGameEnd(amount)
    }
    private fun availablePopAmountChange(amount : BigDecimal) {
        availablePopulation = amount
        if (amount < BigDecimal("1000")) {
            availablePopulationLabel.txt = "You have ${formatNumberWithDecimal(amount)} available population. (AP)"
        } else {
            availablePopulationLabel.txt = "You have ${formatExponent2Dec(amount)} available population. (AP)"
        }
        updateAvailable(amount)
    }
    private fun popGainRateChange(amount : BigDecimal) {
        if (amount < BigDecimal("1000")) {
            populationGainPerSecondLabel.txt = "You are gaining ${formatNumberWithDecimal(amount)} population per second."
        } else {
            populationGainPerSecondLabel.txt = "You are gaining ${formatExponent2Dec(amount)} population per second."
        }
    }
    private fun buyAmountChange(amount : Float) {
        setBuyAmountButton.txt = "Buy ${amount.roundToInt()}"
    }
    private fun updateAvailable(amount : BigDecimal) {
        wheatButton.isDisabled = amount < wheatCost
        cornButton.isDisabled = amount < cornCost
        lettuceButton.isDisabled = amount < lettuceCost
        carrotsButton.isDisabled = amount < carrotsCost
        tomatoesButton.isDisabled = amount < tomatoesCost
        broccoliButton.isDisabled = amount < broccoliCost
        onionsButton.isDisabled = amount < onionsCost
        potatoesButton.isDisabled = amount < potatoesCost
    }
    private fun wheatAmountChange(amount : BigInteger) {
        wheatAmountLabel.txt = amount.toString()
        if (amount > BigInteger("0")) {
            cornTable.isVisible = true
        }
    }
    private fun cornAmountChange(amount : BigInteger) {
        cornAmountLabel.txt = amount.toString()
        if (amount > BigInteger("0")) {
            lettuceTable.isVisible = true
        }
    }
    private fun lettuceAmountChange(amount : BigInteger) {
        lettuceAmountLabel.txt = amount.toString()
        if (amount > BigInteger("0")) {
            carrotsTable.isVisible = true
        }
    }
    private fun carrotsAmountChange(amount : BigInteger) {
        carrotsAmountLabel.txt = amount.toString()
        if (amount > BigInteger("0")) {
            tomatoesTable.isVisible = true
        }
    }
    private fun tomatoesAmountChange(amount : BigInteger) {
        tomatoesAmountLabel.txt = amount.toString()
        if (amount > BigInteger("0")) {
            broccoliTable.isVisible = true
        }
    }
    private fun broccoliAmountChange(amount : BigInteger) {
        broccoliAmountLabel.txt = amount.toString()
        if (amount > BigInteger("0")) {
            onionsTable.isVisible = true
        }
    }
    private fun onionsAmountChange(amount : BigInteger) {
        onionsAmountLabel.txt = amount.toString()
        if (amount > BigInteger("0")) {
            potatoesTable.isVisible = true
        }
    }
    private fun potatoesAmountChange(amount : BigInteger) {
        potatoesAmountLabel.txt = amount.toString()
        if (amount > BigInteger("0")) {
            // next unlock
        }
    }
    private fun wheatMultiplierChange(multiplier : BigDecimal) { wheatMultiplierLabel.txt = "x${multNumFormat.format(multiplier)}" }
    private fun cornMultiplierChange(multiplier : BigDecimal) { cornMultiplierLabel.txt = "x${multNumFormat.format(multiplier)}" }
    private fun lettuceMultiplierChange(multiplier : BigDecimal) { lettuceMultiplierLabel.txt = "x${multNumFormat.format(multiplier)}" }
    private fun carrotsMultiplierChange(multiplier : BigDecimal) { carrotsMultiplierLabel.txt = "x${multNumFormat.format(multiplier)}" }
    private fun tomatoesMultiplierChange(multiplier : BigDecimal) { tomatoesMultiplierLabel.txt = "x${multNumFormat.format(multiplier)}" }
    private fun broccoliMultiplierChange(multiplier : BigDecimal) { broccoliMultiplierLabel.txt = "x${multNumFormat.format(multiplier)}" }
    private fun onionsMultiplierChange(multiplier : BigDecimal) { onionsMultiplierLabel.txt = "x${multNumFormat.format(multiplier)}" }
    private fun potatoesMultiplierChange(multiplier : BigDecimal) { potatoesMultiplierLabel.txt = "x${multNumFormat.format(multiplier)}" }

    private fun wheatCostChange(cost : BigDecimal) {
        wheatCost = cost
        wheatButton.isDisabled = availablePopulation < wheatCost
        wheatButton.txt = formatBuyButton(cost)
    }
    private fun cornCostChange(cost : BigDecimal) {
        cornCost = cost
        cornButton.isDisabled = availablePopulation < cornCost
        cornButton.txt = formatBuyButton(cost)
    }
    private fun lettuceCostChange(cost : BigDecimal) {
        lettuceCost = cost
        lettuceButton.isDisabled = availablePopulation < lettuceCost
        lettuceButton.txt = formatBuyButton(cost)
    }
    private fun carrotsCostChange(cost : BigDecimal) {
        carrotsCost = cost
        carrotsButton.isDisabled = availablePopulation < carrotsCost
        carrotsButton.txt = formatBuyButton(cost)
    }
    private fun tomatoesCostChange(cost : BigDecimal) {
        tomatoesCost = cost
        tomatoesButton.isDisabled = availablePopulation < tomatoesCost
        tomatoesButton.txt = formatBuyButton(cost)
    }
    private fun broccoliCostChange(cost : BigDecimal) {
        broccoliCost = cost
        broccoliButton.isDisabled = availablePopulation < broccoliCost
        broccoliButton.txt = formatBuyButton(cost)
    }
    private fun onionsCostChange(cost : BigDecimal) {
        onionsCost = cost
        onionsButton.isDisabled = availablePopulation < onionsCost
        onionsButton.txt = formatBuyButton(cost)
    }
    private fun potatoesCostChange(cost : BigDecimal) {
        potatoesCost = cost
        potatoesButton.isDisabled = availablePopulation < potatoesCost
        potatoesButton.txt = formatBuyButton(cost)
    }
    private fun formatBuyButton(cost : BigDecimal) : String {
        return if (cost > BigDecimal(999)) {
            "Buy ${buyAmount.roundToInt()}\nAssign: ${formatExponentNoDec(cost)} AP"
        } else {
            "Buy ${buyAmount.roundToInt()}\nAssign: ${formatNumberNoDecimal(cost)} AP"
        }
    }
    private fun formatNumberWithDecimal(number : BigDecimal) : String {
        return decimalFormat.format(number)
    }
    private fun formatNumberNoDecimal(number : BigDecimal) : String {
        return noDecimalFormat.format(number)
    }
    private fun formatExponentNoDec(number : BigDecimal) : String { return expNoDecimalFormat.format(number).replace('E', 'e') }
    private fun formatExponent1Dec(number : BigDecimal) : String { return exp1DecimalFormat.format(number).replace('E', 'e') }
    private fun formatExponent2Dec(number : BigDecimal) : String { return exp2DecimalFormat.format(number).replace('E', 'e') }

    private fun checkForGameEnd(amount : BigDecimal) {
        // check that the game is not already ended so we dont call multiple times
        if (amount >= MAX_POP_AMOUNT.toBigDecimal()) {
            fire(GameCompletedEvent())
        }
    }
    private fun popupGameCompleted(completed : Boolean) {
        //log.debug { "popupGameCompleted" }
    }

    private fun setupNumberFormating() {
        noDecimalFormat.maximumFractionDigits = 0
        decimalFormat.minimumFractionDigits = 2
        decimalFormat.maximumFractionDigits = 2
        expNoDecimalFormat.maximumFractionDigits = 0
        exp1DecimalFormat.minimumFractionDigits = 1
        exp1DecimalFormat.maximumFractionDigits = 1
        exp2DecimalFormat.minimumFractionDigits = 2
        exp2DecimalFormat.maximumFractionDigits = 2
        multNumFormat.maximumFractionDigits = 2
        rateNumFormat.maximumFractionDigits = 2
        amtNumFormat.maximumFractionDigits = 2
        costNumFormat.maximumFractionDigits = 0
    }

    companion object {
        private val log = logger<PlanetView>()
        private val MAX_POP_AMOUNT = BigInteger("1000000000000")
    }
}

@Scene2dDsl
fun <S> KWidget<S>.planetView(
    model : PlanetModel,
    skin : Skin = Scene2DSkin.defaultSkin,
    init : PlanetView.(S) -> Unit = { }
) : PlanetView = actor(PlanetView(model, skin), init)
