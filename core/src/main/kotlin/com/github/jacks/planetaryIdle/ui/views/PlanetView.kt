package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.github.jacks.planetaryIdle.events.BuyResourceEvent
import com.github.jacks.planetaryIdle.events.GameCompletedEvent
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
    private var goldCoins = BigDecimal(preferences["gold_coins", "5"])
    private var productionRate = BigDecimal(preferences["production_rate", "0"])
    private var buyAmount : Float = preferences["buy_amount", 1f]

    private var redOwned = BigDecimal(preferences["red_owned", "0"])

    private var redCost = BigDecimal(preferences["red_cost", "1"])

    private var redValue = BigDecimal(preferences["red_value", "0"])

    private var redValueIncrease = BigDecimal(preferences["red_value_increase", "0.31"])

    private var redRate = BigDecimal(preferences["red_rate", "1.3"])

    private var redRateIncrease = BigDecimal(preferences["red_rate_increase", "0.12"])

    // tables

    // buttons
    private val setBuyAmountButton : TextButton

    private var redButton : TextButton

    //private var soilButton : TextButton

    // labels
    private var goldCoinsLabel : Label
    private var productionRateLabel : Label

    private var redToolTip : Label

    //private var soilLabel : Label

    // images
    private var colonizationProgress : Image

    private val tooltipManager = TooltipManager.getInstance()

    init {
        tooltipManager.initialTime = 0f
        setFillParent(true)
        stage = getStage()
        setupNumberFormating()

        // Game play table
        table { gameTableCell ->
            // 1st row information area
            table { tableCell ->
                this@PlanetView.goldCoinsLabel = label("You have ${this@PlanetView.goldCoins} gold coins.", Labels.DEFAULT.skinKey) { cell ->
                    cell.center().padBottom(10f)
                }
                row()
                this@PlanetView.productionRateLabel = label("You are growing ${this@PlanetView.formatNumberWithDecimal(this@PlanetView.productionRate)} food per second.", Labels.DEFAULT.skinKey) { cell ->
                    cell.center()
                }
                tableCell.expandX().top().padTop(10f).height(100f)
            }

            row()
            image(skin[Drawables.BAR_BLACK_THIN]) { cell ->
                cell.expandX().fillX().height(2f)
            }
            row()

            // 2nd row buttons area
            table { tableCell ->
//                table { innerTableCell ->
//                    this@PlanetView.soilLabel = label("Improved Soil (0)", Labels.SMALL.skinKey) { cell ->
//
//                    }
//                    row()
//                    this@PlanetView.soilButton = textButton(
//                        "Reset all crops and\n" +
//                            "start over with better soil.\n" +
//                            "Unlock the next crop.",
//                        Buttons.GREEN_TEXT_BUTTON_SMALL.skinKey
//                    ) { cell ->
//                        cell.left().width(160f).height(80f).pad(5f)
//                        isDisabled = false
//                        this.addListener(object : ChangeListener() {
//                            override fun changed(event: ChangeEvent, actor: Actor) {
//                                stage.fire(UpgradeSoilEvent())
//                            }
//                        })
//                    }
//                    innerTableCell.expandX().left().width(180f).height(90f)
//                }
                this@PlanetView.setBuyAmountButton = textButton("Buy ${this@PlanetView.buyAmount.roundToInt()}", Buttons.GREY_BUTTON_SMALL.skinKey) { cell ->
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
                this@PlanetView.redButton = textButton(this@PlanetView.updateButtonText("red"), Buttons.RED_BUTTON_SMALL.skinKey) { cell ->
                    cell.expand().top().left().width(210f).height(55f).pad(3f,5f,0f,3f)
                    isDisabled = this@PlanetView.goldCoins < this@PlanetView.redCost
                    this.addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) {
                            this@PlanetView.redToolTip.isVisible = isOver
                            stage.fire(BuyResourceEvent("red"))
                        }
                    })
                    this.addListener(object : InputListener() {
                        override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                            this@PlanetView.redToolTip.isVisible = isOver
                            super.enter(event, x, y, pointer, fromActor)
                        }
                        override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                            this@PlanetView.redToolTip.isVisible = isOver
                            super.exit(event, x, y, pointer, toActor)
                        }
                    })
                }
                this@PlanetView.redToolTip = label(this@PlanetView.updateTooltipText("red"), Labels.SMALL_RED_BGD.skinKey) { cell ->
                    cell.expand().top().left().width(150f).height(65f).pad(3f, 10f, 0f, 0f)
                    this.setAlignment(Align.center)
                    this.isVisible = false
                }


                /*
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
                */

                tableCell.expand().fill().top().left()
            }
            row()

            // Bottom progress bar
            table { tableCell ->
                stack { stackCell ->
                    image(skin[Drawables.BAR_GREY_THICK])
                    this@PlanetView.colonizationProgress = image(skin[Drawables.BAR_GREEN_THICK]) { cell ->
                        scaleX = 0f
                    }
                    this@PlanetView.goldCoinsLabel = label(
                        "${"%.2f".format((this@PlanetView.productionRate / MAX_POP_AMOUNT.toBigDecimal()).toFloat().coerceAtMost(1f) * 100f)} %"
                        , Labels.MEDIUM.skinKey
                    ) { cell ->
                        setAlignment(Align.center)
                    }
                    stackCell.center().width(600f).height(30f)
                }
                tableCell.expandX().top().height(40f).padTop(15f)
            }
            gameTableCell.expand().fill().pad(5f, 0f, 5f, 200f)
        }

        // Data Binding
        model.onPropertyChange(PlanetModel::goldCoins) { amount -> goldCoinAmountChanged(amount) }
        model.onPropertyChange(PlanetModel::productionRate) { rate -> productionRateChanged(rate) }
        model.onPropertyChange(PlanetModel::buyAmount) { amount -> buyAmountChange(amount) }

        model.onPropertyChange(PlanetModel::redOwned) { amount -> redOwnedChanged(amount) }
        model.onPropertyChange(PlanetModel::redCost) { cost -> redCostChanged(cost) }
        model.onPropertyChange(PlanetModel::redValue) { value -> redValueChanged(value) }
        model.onPropertyChange(PlanetModel::redRate) { rate -> redRateChanged(rate) }

        model.onPropertyChange(PlanetModel::gameCompleted) { completed -> popupGameCompleted(completed) }

        // commit tooltip manager properties
        tooltipManager.hideAll()
    }

    private fun updateButtonText(buttonName : String) : String {
        return when (buttonName) {
            "red" -> {
                "Crops/s: $redRate (+$redRateIncrease)\n$redCost gold"
            }
            "yellow" -> {
                "Crops/s: $redRate (+$redRateIncrease)\n$redCost gold"
            }
            else -> {
                "Invalid Button Name"
            }
        }
    }

    private fun updateTooltipText(buttonName : String) : String {
        return "Name: \n" +
            "Amount: $redOwned \n" +
            "Value: $redValueIncrease"
    }

    /*
    private fun goldCoinAmountChanged(amount : BigDecimal) {
        val popPercent = (amount / MAX_POP_AMOUNT.toBigDecimal()).toFloat().coerceAtMost(1f)
        goldCoinsLabel.txt = "${"%.2f".format(popPercent * 100f)} %"
        colonizationProgress.scaleX = popPercent
        checkForGameEnd(amount)
    }
     */

    private fun goldCoinAmountChanged(amount : BigDecimal) {
        goldCoins = amount
        if (amount < BigDecimal("1000")) {
            goldCoinsLabel.txt = "You have ${formatNumberWithDecimal(amount)} gold coins."
        } else {
            goldCoinsLabel.txt = "You have ${formatExponent2Dec(amount)} gold coins."
        }
        updateAvailable(amount)
    }
    private fun productionRateChanged(amount : BigDecimal) {
        if (amount < BigDecimal("1000")) {
            productionRateLabel.txt = "You are producing ${formatNumberWithDecimal(amount)} food per second."
        } else {
            productionRateLabel.txt = "You are producing ${formatExponent2Dec(amount)} food per second."
        }
    }
    private fun buyAmountChange(amount : Float) {
        setBuyAmountButton.txt = "Buy ${amount.roundToInt()}"
    }
    private fun updateAvailable(amount : BigDecimal) {
        redButton.isDisabled = amount < redCost
    }
    private fun redOwnedChanged(amount : BigDecimal) {
        updateButtonText("red")
        updateTooltipText("red")
        if (amount > BigDecimal("5")) {
            //cornTable.isVisible = true
        }
    }
    private fun redCostChanged(cost : BigDecimal) {
        redCost = cost
    }
    private fun redValueChanged(value : BigDecimal) {
        redValue = value
    }
    private fun redRateChanged(rate : BigDecimal) {
        redRate = rate
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
        val log = logger<PlanetView>()
        private val MAX_POP_AMOUNT = BigInteger("1000000000000")
    }
}

@Scene2dDsl
fun <S> KWidget<S>.planetView(
    model : PlanetModel,
    skin : Skin = Scene2DSkin.defaultSkin,
    init : PlanetView.(S) -> Unit = { }
) : PlanetView = actor(PlanetView(model, skin), init)
