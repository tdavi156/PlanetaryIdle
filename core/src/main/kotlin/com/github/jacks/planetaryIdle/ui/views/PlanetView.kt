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
    private var redValue = BigDecimal(preferences["red_value", "0.31"])
    private var redValueIncrease = BigDecimal(preferences["red_value_increase", "0.31"])
    private var redRate = BigDecimal(preferences["red_rate", "1.3"])
    private var redRateIncrease = BigDecimal(preferences["red_rate_increase", "0.17"])

    private var orangeOwned = BigDecimal(preferences["orange_owned", "0"])
    private var orangeCost = BigDecimal(preferences["orange_cost", "100"])
    private var orangeValue = BigDecimal(preferences["orange_value", "2.4"])
    private var orangeValueIncrease = BigDecimal(preferences["orange_value_increase", "2.4"])
    private var orangeRate = BigDecimal(preferences["orange_rate", "0.95"])
    private var orangeRateIncrease = BigDecimal(preferences["orange_rate_increase", "0.11"])

    // tables

    // buttons
    //private val setBuyAmountButton : TextButton

    private var redButton : TextButton
    private var orangeButton : TextButton

    //private var soilButton : TextButton

    // labels
    private var goldCoinsLabel : Label
    private var productionRateLabel : Label
    private var productionRateProgressLabel : Label

    private var redValueLabel : Label
    private var orangeValueLabel : Label
    private var yellowValueLabel : Label
//    private var greenValueLabel : Label
//    private var blueValueLabel : Label
//    private var purpleValueLabel : Label
//    private var pinkValueLabel : Label
//    private var brownValueLabel : Label
//    private var whiteValueLabel : Label
//    private var blackValueLabel : Label

    private lateinit var redToolTip : Label
    private lateinit var orangeToolTip : Label

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
                this@PlanetView.redValueLabel = label("0.00", Labels.RED.skinKey) { cell ->
                    cell.center().pad(3f)
                }
                label(" + ", Labels.WHITE.skinKey)
                this@PlanetView.orangeValueLabel = label("0.00", Labels.ORANGE.skinKey) { cell ->
                    cell.center().pad(3f)
                }
                label(" + ", Labels.WHITE.skinKey)
                this@PlanetView.yellowValueLabel = label("0.00", Labels.YELLOW.skinKey) { cell ->
                    cell.center().pad(3f)
                }
                tableCell.expandX().top().height(40f)
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
//                this@PlanetView.setBuyAmountButton = textButton("Buy ${this@PlanetView.buyAmount.roundToInt()}", Buttons.GREY_BUTTON_SMALL.skinKey) { cell ->
//                    this.addListener(object : ChangeListener() {
//                        override fun changed(event: ChangeEvent, actor: Actor) {
//                            when (this@PlanetView.buyAmount) {
//                                1f -> this@PlanetView.buyAmount = 10f
//                                10f -> this@PlanetView.buyAmount = 100f
//                                100f -> this@PlanetView.buyAmount = 1f
//                                else -> this@PlanetView.buyAmount = 1f
//                            }
//                            log.debug { "BuyAmount: ${this@PlanetView.buyAmount}" }
//                            stage.fire(UpdateBuyAmountEvent(this@PlanetView.buyAmount))
//                        }
//                    })
//                    cell.expandX().top().right().width(90f).height(30f).pad(10f, 10f, 20f, 10f)
//                }
//                tableCell.expandX().fillX().top().padTop(10f).height(80f)
            }
            row()

            // Middle actionable area
            table { tableCell ->
                table { buttonTableCell ->
                    this@PlanetView.redButton = textButton(this@PlanetView.updateButtonText("red"), Buttons.RED_BUTTON_SMALL.skinKey) { cell ->
                        cell.expand().top().left().width(210f).height(55f).pad(3f,5f,3f,0f)
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
                    row()
                    this@PlanetView.orangeButton = textButton(this@PlanetView.updateButtonText("orange"), Buttons.ORANGE_BUTTON_SMALL.skinKey) { cell ->
                        cell.expand().top().left().width(210f).height(55f).pad(3f,5f,3f,0f)
                        isVisible = false
                        isDisabled = this@PlanetView.goldCoins < this@PlanetView.orangeCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                this@PlanetView.orangeToolTip.isVisible = isOver
                                stage.fire(BuyResourceEvent("orange"))
                            }
                        })
                        this.addListener(object : InputListener() {
                            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                                this@PlanetView.orangeToolTip.isVisible = isOver
                                super.enter(event, x, y, pointer, fromActor)
                            }
                            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                                this@PlanetView.orangeToolTip.isVisible = isOver
                                super.exit(event, x, y, pointer, toActor)
                            }
                        })
                    }
                    buttonTableCell.expand().fill().top().left().pad(5f).width(230f)
                }
                table { tooltipTableCell ->
                    this@PlanetView.redToolTip = label(this@PlanetView.updateTooltipText("red"), Labels.SMALL_RED_BGD.skinKey) { cell ->
                        cell.expand().top().left().width(150f).height(65f).pad(0f, 5f, 0f, 0f)
                        this.setAlignment(Align.center)
                        this.isVisible = false
                    }
                    row()
                    this@PlanetView.orangeToolTip = label(this@PlanetView.updateTooltipText("orange"), Labels.SMALL_ORANGE_BGD.skinKey) { cell ->
                        cell.expand().top().left().width(150f).height(65f).pad(0f, 5f, 0f, 0f)
                        this.setAlignment(Align.center)
                        this.isVisible = false
                    }
                    tooltipTableCell.expand().fill().top().left().width(230f)
                }
                table { infoTableCell ->
                    this@PlanetView.goldCoinsLabel = label("You have ${this@PlanetView.goldCoins} gold coins.", Labels.DEFAULT.skinKey) { cell ->
                        cell.center().padBottom(10f)
                    }
                    row()
                    this@PlanetView.productionRateLabel = label("You are growing ${this@PlanetView.formatNumberWithDecimal(this@PlanetView.productionRate)} food per second.", Labels.DEFAULT.skinKey) { cell ->
                        cell.center()
                    }
                    infoTableCell.expand().fill().top().left()
                }
                tableCell.expandY().fillY().top().left()
            }
            row()

            // Bottom progress bar
            table { tableCell ->
                stack { stackCell ->
                    image(skin[Drawables.BAR_GREY_THICK])
                    this@PlanetView.colonizationProgress = image(skin[Drawables.BAR_GREEN_THICK]) { cell ->
                        scaleX = 0f
                    }
                    this@PlanetView.productionRateProgressLabel = label(
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

        model.onPropertyChange(PlanetModel::orangeOwned) { amount -> orangeOwnedChanged(amount) }
        model.onPropertyChange(PlanetModel::orangeCost) { cost -> orangeCostChanged(cost) }
        model.onPropertyChange(PlanetModel::orangeValue) { value -> orangeValueChanged(value) }
        model.onPropertyChange(PlanetModel::orangeRate) { rate -> orangeRateChanged(rate) }

        model.onPropertyChange(PlanetModel::gameCompleted) { completed -> popupGameCompleted(completed) }

        // commit tooltip manager properties
        tooltipManager.hideAll()
    }

    private fun updateButtonText(buttonName : String) : String {
        return when (buttonName) {
            "red" -> { "Crops/s: ${formatNumberWithDecimal(redRate)} (+$redRateIncrease)\n${formatNumberWithDecimal(redCost)} gold" }
            "orange" -> { "Crops/s: ${formatNumberWithDecimal(orangeRate)} (+$orangeRateIncrease)\n${formatNumberWithDecimal(orangeCost)} gold" }
            "yellow" -> { "Crops/s: ${formatNumberWithDecimal(redRate)} (+$redRateIncrease)\n${formatNumberWithDecimal(redCost)} gold" }
            else -> {
                "Invalid Button Name"
            }
        }
    }
    private fun updateTooltipText(tooltipName : String) : String {
        return when (tooltipName) {
            "red" -> { "Name: Red\nAmount: ${formatNumberNoDecimal(redOwned)} \nValue: ${formatNumberWithDecimal(redValueIncrease)}" }
            "orange" -> { "Name: Orange\nAmount: ${formatNumberNoDecimal(orangeOwned)} \nValue: ${formatNumberWithDecimal(orangeValueIncrease)}" }
            "yellow" -> { "Name: Yellow\nAmount: ${formatNumberNoDecimal(redOwned)} \nValue: ${formatNumberWithDecimal(redValueIncrease)}" }
            else -> {
                "Invalid Tooltip Name"
            }
        }
    }
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
        //setBuyAmountButton.txt = "Buy ${amount.roundToInt()}"
    }
    private fun updateAvailable(amount : BigDecimal) {
        redButton.isDisabled = amount < redCost
        orangeButton.isDisabled = amount < orangeCost
    }
    private fun redOwnedChanged(amount : BigDecimal) {
        redOwned = amount
        redToolTip.txt = updateTooltipText("red")
        if (amount.toInt() >= 5) {
            orangeButton.isVisible = true
        }
    }
    private fun redCostChanged(cost : BigDecimal) {
        redCost = cost
    }
    private fun redValueChanged(value : BigDecimal) {
        redValue = value
        redValueLabel.txt = formatNumberWithDecimal(value)
    }
    private fun redRateChanged(rate : BigDecimal) {
        redRate = rate
        redButton.txt = updateButtonText("red")
    }

    private fun orangeOwnedChanged(amount : BigDecimal) {
        orangeOwned = amount
        orangeToolTip.txt = updateTooltipText("orange")
        if (amount.toInt() >= 5) {
            //yellowButton.isVisible = true
        }
    }
    private fun orangeCostChanged(cost : BigDecimal) {
        orangeCost = cost
    }
    private fun orangeValueChanged(value : BigDecimal) {
        orangeValue = value
        orangeValueLabel.txt = formatNumberWithDecimal(value)
    }
    private fun orangeRateChanged(rate : BigDecimal) {
        orangeRate = rate
        orangeButton.txt = updateButtonText("orange")
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
