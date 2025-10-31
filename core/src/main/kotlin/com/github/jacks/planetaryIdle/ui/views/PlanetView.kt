package com.github.jacks.planetaryIdle.ui.views

import ch.obermuhlner.math.big.BigDecimalMath
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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.github.jacks.planetaryIdle.events.AchievementCompletedEvent
import com.github.jacks.planetaryIdle.events.AchievementNotificationEvent
import com.github.jacks.planetaryIdle.events.BuyResourceEvent
import com.github.jacks.planetaryIdle.events.GameCompletedEvent
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
import java.math.MathContext
import java.math.RoundingMode
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

    private val mathCx : MathContext = MathContext(3)
    private val twoDecimalWithCommasFormat : NumberFormat = DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance())

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
    private var redValueIncrease = BigDecimal(preferences["red_value_increase", "0.04"])
    private var redRate = BigDecimal(preferences["red_rate", "1.3"])
    private var redRateIncrease = BigDecimal(preferences["red_rate_increase", "0.17"])

    private var orangeOwned = BigDecimal(preferences["orange_owned", "0"])
    private var orangeCost = BigDecimal(preferences["orange_cost", "100"])
    private var orangeValue = BigDecimal(preferences["orange_value", "2.4"])
    private var orangeValueIncrease = BigDecimal(preferences["orange_value_increase", "0.09"])
    private var orangeRate = BigDecimal(preferences["orange_rate", "0.95"])
    private var orangeRateIncrease = BigDecimal(preferences["orange_rate_increase", "0.11"])

    private var yellowOwned = BigDecimal(preferences["yellow_owned", "0"])
    private var yellowCost = BigDecimal(preferences["yellow_cost", "1000"])
    private var yellowValue = BigDecimal(preferences["yellow_value", "18.9"])
    private var yellowValueIncrease = BigDecimal(preferences["yellow_value_increase", "0.23"])
    private var yellowRate = BigDecimal(preferences["yellow_rate", "0.67"])
    private var yellowRateIncrease = BigDecimal(preferences["yellow_rate_increase", "0.07"])

    private var greenOwned = BigDecimal(preferences["green_owned", "0"])
    private var greenCost = BigDecimal(preferences["green_cost", "50000"])
    private var greenValue = BigDecimal(preferences["green_value", "147.1"])
    private var greenValueIncrease = BigDecimal(preferences["green_value_increase", "3.21"])
    private var greenRate = BigDecimal(preferences["green_rate", "0.43"])
    private var greenRateIncrease = BigDecimal(preferences["green_rate_increase", "0.06"])

    private var blueOwned = BigDecimal(preferences["blue_owned", "0"])
    private var blueCost = BigDecimal(preferences["blue_cost", "1000000"])
    private var blueValue = BigDecimal(preferences["blue_value", "1147"])
    private var blueValueIncrease = BigDecimal(preferences["blue_value_increase", "12.5"])
    private var blueRate = BigDecimal(preferences["blue_rate", "0.21"])
    private var blueRateIncrease = BigDecimal(preferences["blue_rate_increase", "0.05"])

    private var purpleOwned = BigDecimal(preferences["purple_owned", "0"])
    private var purpleCost = BigDecimal(preferences["purple_cost", "500000000"])
    private var purpleValue = BigDecimal(preferences["purple_value", "8952"])
    private var purpleValueIncrease = BigDecimal(preferences["purple_value_increase", "46.3"])
    private var purpleRate = BigDecimal(preferences["purple_rate", "0.12"])
    private var purpleRateIncrease = BigDecimal(preferences["purple_rate_increase", "0.04"])

    private var pinkOwned = BigDecimal(preferences["pink_owned", "0"])
    private var pinkCost = BigDecimal(preferences["pink_cost", "10000000000"])
    private var pinkValue = BigDecimal(preferences["pink_value", "69811"])
    private var pinkValueIncrease = BigDecimal(preferences["pink_value_increase", "374.8"])
    private var pinkRate = BigDecimal(preferences["pink_rate", "0.08"])
    private var pinkRateIncrease = BigDecimal(preferences["pink_rate_increase", "0.03"])

    private var brownOwned = BigDecimal(preferences["brown_owned", "0"])
    private var brownCost = BigDecimal(preferences["brown_cost", "100000000000000"])
    private var brownValue = BigDecimal(preferences["brown_value", "544532"])
    private var brownValueIncrease = BigDecimal(preferences["brown_value_increase", "2567"])
    private var brownRate = BigDecimal(preferences["brown_rate", "0.05"])
    private var brownRateIncrease = BigDecimal(preferences["brown_rate_increase", "0.02"])

    private var whiteOwned = BigDecimal(preferences["white_owned", "0"])
    private var whiteCost = BigDecimal(preferences["white_cost", "1000000000000000000"])
    private var whiteValue = BigDecimal(preferences["white_value", "4247354"])
    private var whiteValueIncrease = BigDecimal(preferences["white_value_increase", "74502"])
    private var whiteRate = BigDecimal(preferences["white_rate", "0.03"])
    private var whiteRateIncrease = BigDecimal(preferences["white_rate_increase", "0.01"])

    private var blackOwned = BigDecimal(preferences["black_owned", "0"])
    private var blackCost = BigDecimal(preferences["black_cost", "1000000000000000000000000"])
    private var blackValue = BigDecimal(preferences["black_value", "33129365"])
    private var blackValueIncrease = BigDecimal(preferences["black_value_increase", "3312936"])
    private var blackRate = BigDecimal(preferences["black_rate", "0.01"])
    private var blackRateIncrease = BigDecimal(preferences["black_rate_increase", "0.005"])

    private var soilUpgrades = BigDecimal(preferences["soil_upgrades", "0"])
    private var soilCost = BigDecimal(preferences["soil_cost", "1000000"])

    // tables

    // buttons
    //private val setBuyAmountButton : TextButton

    private var redButton : TextButton
    private var orangeButton : TextButton
    private var yellowButton : TextButton
    private var greenButton : TextButton
    private var blueButton : TextButton
    private var purpleButton : TextButton
    private var pinkButton : TextButton
    private var brownButton : TextButton
    private var whiteButton : TextButton
    private var blackButton : TextButton

    private var soilButton : TextButton

    // labels
    private var goldCoinsLabel : Label
    private var productionRateLabel : Label
    private var productionRateProgressLabel : Label

    private var redValueLabel : Label
    private var orangeValueLabel : Label
    private var yellowValueLabel : Label
    private var greenValueLabel : Label
    private var blueValueLabel : Label
    private var purpleValueLabel : Label
    private var pinkValueLabel : Label
    private var brownValueLabel : Label
    private var whiteValueLabel : Label
    private var blackValueLabel : Label
    private var multiplierValueLabel : Label

    private lateinit var redToolTipLabel : Label
    private lateinit var orangeToolTipLabel : Label
    private lateinit var yellowToolTipLabel : Label
    private lateinit var greenToolTipLabel : Label
    private lateinit var blueToolTipLabel : Label
    private lateinit var purpleToolTipLabel : Label
    private lateinit var pinkToolTipLabel : Label
    private lateinit var brownToolTipLabel : Label
    private lateinit var whiteToolTipLabel : Label
    private lateinit var blackToolTipLabel : Label
    private lateinit var multiplierToolTipLabel : Label

    private var soilToolTipLabel : Label

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
                this@PlanetView.redValueLabel = label("0", Labels.RED.skinKey) { cell ->
                    cell.center().pad(3f)
                }
                label(" + ", Labels.WHITE.skinKey)
                this@PlanetView.orangeValueLabel = label("0", Labels.ORANGE.skinKey) { cell ->
                    cell.center().pad(3f)
                }
                label(" + ", Labels.WHITE.skinKey)
                this@PlanetView.yellowValueLabel = label("0", Labels.YELLOW.skinKey) { cell ->
                    cell.center().pad(3f)
                }
                label(" + ", Labels.WHITE.skinKey)
                this@PlanetView.greenValueLabel = label("0", Labels.GREEN.skinKey) { cell ->
                    cell.center().pad(3f)
                }
                label(" + ", Labels.WHITE.skinKey)
                this@PlanetView.blueValueLabel = label("0", Labels.BLUE.skinKey) { cell ->
                    cell.center().pad(3f)
                }
                label(" + ", Labels.WHITE.skinKey)
                this@PlanetView.purpleValueLabel = label("0", Labels.PURPLE.skinKey) { cell ->
                    cell.center().pad(3f)
                }
                label(" + ", Labels.WHITE.skinKey)
                this@PlanetView.pinkValueLabel = label("0", Labels.PINK.skinKey) { cell ->
                    cell.center().pad(3f)
                }
                label(" + ", Labels.WHITE.skinKey)
                this@PlanetView.brownValueLabel = label("0", Labels.BROWN.skinKey) { cell ->
                    cell.center().pad(3f)
                }
                label(" + ", Labels.WHITE.skinKey)
                this@PlanetView.whiteValueLabel = label("0", Labels.WHITE.skinKey) { cell ->
                    cell.center().pad(3f)
                }
                label(" + ", Labels.WHITE.skinKey)
                this@PlanetView.blackValueLabel = label("0", Labels.BLACK.skinKey) { cell ->
                    cell.center().pad(3f)
                }
                label("  x ", Labels.WHITE.skinKey)
                this@PlanetView.multiplierValueLabel = label("1.00", Labels.WHITE.skinKey) { cell ->
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
                                stage.fire(BuyResourceEvent("red"))
                            }
                        })
                        this.addListener(object : InputListener() {
                            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                                this@PlanetView.redToolTipLabel.isVisible = isOver
                                super.enter(event, x, y, pointer, fromActor)
                            }
                            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                                this@PlanetView.redToolTipLabel.isVisible = isOver
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
                                stage.fire(BuyResourceEvent("orange"))
                            }
                        })
                        this.addListener(object : InputListener() {
                            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                                this@PlanetView.orangeToolTipLabel.isVisible = isOver
                                super.enter(event, x, y, pointer, fromActor)
                            }
                            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                                this@PlanetView.orangeToolTipLabel.isVisible = isOver
                                super.exit(event, x, y, pointer, toActor)
                            }
                        })
                    }
                    row()
                    this@PlanetView.yellowButton = textButton(this@PlanetView.updateButtonText("yellow"), Buttons.YELLOW_BUTTON_SMALL.skinKey) { cell ->
                        cell.expand().top().left().width(210f).height(55f).pad(3f,5f,3f,0f)
                        isVisible = false
                        isDisabled = this@PlanetView.goldCoins < this@PlanetView.yellowCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("yellow"))
                            }
                        })
                        this.addListener(object : InputListener() {
                            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                                this@PlanetView.yellowToolTipLabel.isVisible = isOver
                                super.enter(event, x, y, pointer, fromActor)
                            }
                            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                                this@PlanetView.yellowToolTipLabel.isVisible = isOver
                                super.exit(event, x, y, pointer, toActor)
                            }
                        })
                    }
                    row()
                    this@PlanetView.greenButton = textButton(this@PlanetView.updateButtonText("green"), Buttons.GREEN_BUTTON_SMALL.skinKey) { cell ->
                        cell.expand().top().left().width(210f).height(55f).pad(3f,5f,3f,0f)
                        isVisible = false
                        isDisabled = this@PlanetView.goldCoins < this@PlanetView.greenCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("green"))
                            }
                        })
                        this.addListener(object : InputListener() {
                            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                                this@PlanetView.greenToolTipLabel.isVisible = isOver
                                super.enter(event, x, y, pointer, fromActor)
                            }
                            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                                this@PlanetView.greenToolTipLabel.isVisible = isOver
                                super.exit(event, x, y, pointer, toActor)
                            }
                        })
                    }
                    row()
                    this@PlanetView.blueButton = textButton(this@PlanetView.updateButtonText("blue"), Buttons.BLUE_BUTTON_SMALL.skinKey) { cell ->
                        cell.expand().top().left().width(210f).height(55f).pad(3f,5f,3f,0f)
                        isVisible = false
                        isDisabled = this@PlanetView.goldCoins < this@PlanetView.blueCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("blue"))
                            }
                        })
                        this.addListener(object : InputListener() {
                            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                                this@PlanetView.blueToolTipLabel.isVisible = isOver
                                super.enter(event, x, y, pointer, fromActor)
                            }
                            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                                this@PlanetView.blueToolTipLabel.isVisible = isOver
                                super.exit(event, x, y, pointer, toActor)
                            }
                        })
                    }
                    row()
                    this@PlanetView.purpleButton = textButton(this@PlanetView.updateButtonText("purple"), Buttons.PURPLE_BUTTON_SMALL.skinKey) { cell ->
                        cell.expand().top().left().width(210f).height(55f).pad(3f,5f,3f,0f)
                        isVisible = false
                        isDisabled = this@PlanetView.goldCoins < this@PlanetView.purpleCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("purple"))
                            }
                        })
                        this.addListener(object : InputListener() {
                            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                                this@PlanetView.purpleToolTipLabel.isVisible = isOver
                                super.enter(event, x, y, pointer, fromActor)
                            }
                            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                                this@PlanetView.purpleToolTipLabel.isVisible = isOver
                                super.exit(event, x, y, pointer, toActor)
                            }
                        })
                    }
                    row()
                    this@PlanetView.pinkButton = textButton(this@PlanetView.updateButtonText("pink"), Buttons.PINK_BUTTON_SMALL.skinKey) { cell ->
                        cell.expand().top().left().width(210f).height(55f).pad(3f,5f,3f,0f)
                        isVisible = false
                        isDisabled = this@PlanetView.goldCoins < this@PlanetView.pinkCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("pink"))
                            }
                        })
                        this.addListener(object : InputListener() {
                            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                                this@PlanetView.pinkToolTipLabel.isVisible = isOver
                                super.enter(event, x, y, pointer, fromActor)
                            }
                            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                                this@PlanetView.pinkToolTipLabel.isVisible = isOver
                                super.exit(event, x, y, pointer, toActor)
                            }
                        })
                    }
                    row()
                    this@PlanetView.brownButton = textButton(this@PlanetView.updateButtonText("brown"), Buttons.BROWN_BUTTON_SMALL.skinKey) { cell ->
                        cell.expand().top().left().width(210f).height(55f).pad(3f,5f,3f,0f)
                        isVisible = false
                        isDisabled = this@PlanetView.goldCoins < this@PlanetView.brownCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("brown"))
                            }
                        })
                        this.addListener(object : InputListener() {
                            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                                this@PlanetView.brownToolTipLabel.isVisible = isOver
                                super.enter(event, x, y, pointer, fromActor)
                            }
                            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                                this@PlanetView.brownToolTipLabel.isVisible = isOver
                                super.exit(event, x, y, pointer, toActor)
                            }
                        })
                    }
                    row()
                    this@PlanetView.whiteButton = textButton(this@PlanetView.updateButtonText("white"), Buttons.WHITE_BUTTON_SMALL.skinKey) { cell ->
                        cell.expand().top().left().width(210f).height(55f).pad(3f,5f,3f,0f)
                        isVisible = false
                        isDisabled = this@PlanetView.goldCoins < this@PlanetView.whiteCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("white"))
                            }
                        })
                        this.addListener(object : InputListener() {
                            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                                this@PlanetView.whiteToolTipLabel.isVisible = isOver
                                super.enter(event, x, y, pointer, fromActor)
                            }
                            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                                this@PlanetView.whiteToolTipLabel.isVisible = isOver
                                super.exit(event, x, y, pointer, toActor)
                            }
                        })
                    }
                    row()
                    this@PlanetView.blackButton = textButton(this@PlanetView.updateButtonText("black"), Buttons.BLACK_BUTTON_SMALL.skinKey) { cell ->
                        cell.expand().top().left().width(210f).height(55f).pad(3f,5f,3f,0f)
                        isVisible = false
                        isDisabled = this@PlanetView.goldCoins < this@PlanetView.blackCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(BuyResourceEvent("black"))
                            }
                        })
                        this.addListener(object : InputListener() {
                            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                                this@PlanetView.blackToolTipLabel.isVisible = isOver
                                super.enter(event, x, y, pointer, fromActor)
                            }
                            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                                this@PlanetView.blackToolTipLabel.isVisible = isOver
                                super.exit(event, x, y, pointer, toActor)
                            }
                        })
                    }
                    row()
                    this@PlanetView.soilButton = textButton("Upgrade Soil (${this@PlanetView.soilUpgrades})", Buttons.BLACK_BUTTON_SMALL.skinKey) { cell ->
                        cell.expand().bottom().left().width(210f).height(55f).pad(3f,5f,3f,0f)
                        isVisible = false
                        isDisabled = this@PlanetView.goldCoins < this@PlanetView.soilCost
                        this.addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                stage.fire(UpgradeSoilEvent())
                            }
                        })
                        this.addListener(object : InputListener() {
                            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                                this@PlanetView.soilToolTipLabel.isVisible = isOver
                                super.enter(event, x, y, pointer, fromActor)
                            }
                            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                                this@PlanetView.soilToolTipLabel.isVisible = isOver
                                super.exit(event, x, y, pointer, toActor)
                            }
                        })
                    }
                    buttonTableCell.expand().fill().top().left().pad(5f).width(230f)
                }
                table { tooltipTableCell ->
                    this@PlanetView.redToolTipLabel = label(this@PlanetView.updateTooltipText("red"), Labels.SMALL_RED_BGD.skinKey) { cell ->
                        cell.expand().top().left().width(150f).height(65f).pad(0f, 5f, 0f, 0f)
                        this.setAlignment(Align.center)
                        this.isVisible = false
                    }
                    row()
                    this@PlanetView.orangeToolTipLabel = label(this@PlanetView.updateTooltipText("orange"), Labels.SMALL_ORANGE_BGD.skinKey) { cell ->
                        cell.expand().top().left().width(150f).height(65f).pad(0f, 5f, 0f, 0f)
                        this.setAlignment(Align.center)
                        this.isVisible = false
                    }
                    row()
                    this@PlanetView.yellowToolTipLabel = label(this@PlanetView.updateTooltipText("yellow"), Labels.SMALL_YELLOW_BGD.skinKey) { cell ->
                        cell.expand().top().left().width(150f).height(65f).pad(0f, 5f, 0f, 0f)
                        this.setAlignment(Align.center)
                        this.isVisible = false
                    }
                    row()
                    this@PlanetView.greenToolTipLabel = label(this@PlanetView.updateTooltipText("green"), Labels.SMALL_GREEN_BGD.skinKey) { cell ->
                        cell.expand().top().left().width(150f).height(65f).pad(0f, 5f, 0f, 0f)
                        this.setAlignment(Align.center)
                        this.isVisible = false
                    }
                    row()
                    this@PlanetView.blueToolTipLabel = label(this@PlanetView.updateTooltipText("blue"), Labels.SMALL_BLUE_BGD.skinKey) { cell ->
                        cell.expand().top().left().width(150f).height(65f).pad(0f, 5f, 0f, 0f)
                        this.setAlignment(Align.center)
                        this.isVisible = false
                    }
                    row()
                    this@PlanetView.purpleToolTipLabel = label(this@PlanetView.updateTooltipText("purple"), Labels.SMALL_PURPLE_BGD.skinKey) { cell ->
                        cell.expand().top().left().width(150f).height(65f).pad(0f, 5f, 0f, 0f)
                        this.setAlignment(Align.center)
                        this.isVisible = false
                    }
                    row()
                    this@PlanetView.pinkToolTipLabel = label(this@PlanetView.updateTooltipText("pink"), Labels.SMALL_PINK_BGD.skinKey) { cell ->
                        cell.expand().top().left().width(150f).height(65f).pad(0f, 5f, 0f, 0f)
                        this.setAlignment(Align.center)
                        this.isVisible = false
                    }
                    row()
                    this@PlanetView.brownToolTipLabel = label(this@PlanetView.updateTooltipText("brown"), Labels.SMALL_BROWN_BGD.skinKey) { cell ->
                        cell.expand().top().left().width(150f).height(65f).pad(0f, 5f, 0f, 0f)
                        this.setAlignment(Align.center)
                        this.isVisible = false
                    }
                    row()
                    this@PlanetView.whiteToolTipLabel = label(this@PlanetView.updateTooltipText("white"), Labels.SMALL_WHITE_BGD.skinKey) { cell ->
                        cell.expand().top().left().width(150f).height(65f).pad(0f, 5f, 0f, 0f)
                        this.setAlignment(Align.center)
                        this.isVisible = false
                    }
                    row()
                    this@PlanetView.blackToolTipLabel = label(this@PlanetView.updateTooltipText("black"), Labels.SMALL_BLACK_BGD.skinKey) { cell ->
                        cell.expand().top().left().width(150f).height(65f).pad(0f, 5f, 0f, 0f)
                        this.setAlignment(Align.center)
                        this.isVisible = false
                    }
                    row()
                    this@PlanetView.soilToolTipLabel = label("Upgrading soil resets all crops and gold coins, but increases crop speed by x2", Labels.SOIL_TOOLTIP_BGD.skinKey) { cell ->
                        cell.expand().bottom().left().width(150f).height(85f).pad(0f, 5f, 0f, 0f)
                        this.wrap = true
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
                        "${"%.2f".format((this@PlanetView.productionRate / PLANETARY_SCORE).toFloat().coerceAtMost(1f) * 100f)} %"
                        , Labels.MEDIUM.skinKey
                    ) { cell ->
                        setAlignment(Align.center)
                    }
                    stackCell.center().width(600f).height(30f)
                }
                tableCell.expandX().top().height(40f).padTop(15f)
            }
            gameTableCell.expand().fill().pad(5f, 0f, 5f, 0f)
        }

        // Data Binding
        model.onPropertyChange(PlanetModel::goldCoins) { amount -> goldCoinAmountChanged(amount) }
        model.onPropertyChange(PlanetModel::productionRate) { rate -> productionRateChanged(rate) }
        model.onPropertyChange(PlanetModel::buyAmount) { amount -> buyAmountChange(amount) }

        model.onPropertyChange(PlanetModel::redOwned) { amount -> ownedChanged("red", amount) }
        model.onPropertyChange(PlanetModel::redCost) { cost -> costChanged("red", cost) }
        model.onPropertyChange(PlanetModel::redValue) { value -> valueChanged("red", value) }
        model.onPropertyChange(PlanetModel::redRate) { rate -> rateChanged("red", rate) }

        model.onPropertyChange(PlanetModel::orangeOwned) { amount -> ownedChanged("orange", amount) }
        model.onPropertyChange(PlanetModel::orangeCost) { cost -> costChanged("orange", cost) }
        model.onPropertyChange(PlanetModel::orangeValue) { value -> valueChanged("orange", value) }
        model.onPropertyChange(PlanetModel::orangeRate) { rate -> rateChanged("orange", rate) }

        model.onPropertyChange(PlanetModel::yellowOwned) { amount -> ownedChanged("yellow", amount) }
        model.onPropertyChange(PlanetModel::yellowCost) { cost -> costChanged("yellow", cost) }
        model.onPropertyChange(PlanetModel::yellowValue) { value -> valueChanged("yellow", value) }
        model.onPropertyChange(PlanetModel::yellowRate) { rate -> rateChanged("yellow", rate) }

        model.onPropertyChange(PlanetModel::greenOwned) { amount -> ownedChanged("green", amount) }
        model.onPropertyChange(PlanetModel::greenCost) { cost -> costChanged("green", cost) }
        model.onPropertyChange(PlanetModel::greenValue) { value -> valueChanged("green", value) }
        model.onPropertyChange(PlanetModel::greenRate) { rate -> rateChanged("green", rate) }

        model.onPropertyChange(PlanetModel::blueOwned) { amount -> ownedChanged("blue", amount) }
        model.onPropertyChange(PlanetModel::blueCost) { cost -> costChanged("blue", cost) }
        model.onPropertyChange(PlanetModel::blueValue) { value -> valueChanged("blue", value) }
        model.onPropertyChange(PlanetModel::blueRate) { rate -> rateChanged("blue", rate) }

        model.onPropertyChange(PlanetModel::purpleOwned) { amount -> ownedChanged("purple", amount) }
        model.onPropertyChange(PlanetModel::purpleCost) { cost -> costChanged("purple", cost) }
        model.onPropertyChange(PlanetModel::purpleValue) { value -> valueChanged("purple", value) }
        model.onPropertyChange(PlanetModel::purpleRate) { rate -> rateChanged("purple", rate) }

        model.onPropertyChange(PlanetModel::pinkOwned) { amount -> ownedChanged("pink", amount) }
        model.onPropertyChange(PlanetModel::pinkCost) { cost -> costChanged("pink", cost) }
        model.onPropertyChange(PlanetModel::pinkValue) { value -> valueChanged("pink", value) }
        model.onPropertyChange(PlanetModel::pinkRate) { rate -> rateChanged("pink", rate) }

        model.onPropertyChange(PlanetModel::brownOwned) { amount -> ownedChanged("brown", amount) }
        model.onPropertyChange(PlanetModel::brownCost) { cost -> costChanged("brown", cost) }
        model.onPropertyChange(PlanetModel::brownValue) { value -> valueChanged("brown", value) }
        model.onPropertyChange(PlanetModel::brownRate) { rate -> rateChanged("brown", rate) }

        model.onPropertyChange(PlanetModel::whiteOwned) { amount -> ownedChanged("white", amount) }
        model.onPropertyChange(PlanetModel::whiteCost) { cost -> costChanged("white", cost) }
        model.onPropertyChange(PlanetModel::whiteValue) { value -> valueChanged("white", value) }
        model.onPropertyChange(PlanetModel::whiteRate) { rate -> rateChanged("white", rate) }

        model.onPropertyChange(PlanetModel::blackOwned) { amount -> ownedChanged("black", amount) }
        model.onPropertyChange(PlanetModel::blackCost) { cost -> costChanged("black", cost) }
        model.onPropertyChange(PlanetModel::blackValue) { value -> valueChanged("black", value) }
        model.onPropertyChange(PlanetModel::blackRate) { rate -> rateChanged("black", rate) }

        model.onPropertyChange(PlanetModel::achievementMultiplier) { mult -> achMultChanged(mult) }

        model.onPropertyChange(PlanetModel::soilUpgrades) { amount -> soilUpgradesChanged(amount) }

        model.onPropertyChange(PlanetModel::gameCompleted) { completed -> popupGameCompleted(completed) }
    }

    private fun ownedChanged(name : String, amount : BigDecimal) {
        stage = getStage()
        when (name) {
            "red" -> {
                redOwned = amount
                redToolTipLabel.txt = updateTooltipText("red")
                if (amount.toInt() >= 5) {
                    orangeButton.isVisible = true
                }
                if (amount.toInt() >= 5) {
                    stage.fire(AchievementNotificationEvent(1))
                }
                if (amount.toInt() >= 100) {
                    stage.fire(AchievementNotificationEvent(5))
                }
                if (amount.toInt() >= 1000) {
                    stage.fire(AchievementNotificationEvent(17))
                }
            }
            "orange" -> {
                orangeOwned = amount
                orangeToolTipLabel.txt = updateTooltipText("orange")
                if (amount.toInt() >= 5) {
                    yellowButton.isVisible = true
                }
                if (amount.toInt() >= 1) {
                    stage.fire(AchievementNotificationEvent(2))
                }
            }
            "yellow" -> {
                yellowOwned = amount
                yellowToolTipLabel.txt = updateTooltipText("yellow")
                if (amount.toInt() >= 5) {
                    greenButton.isVisible = true
                }
                if (amount.toInt() >= 1) {
                    stage.fire(AchievementNotificationEvent(3))
                }
            }
            "green" -> {
                greenOwned = amount
                greenToolTipLabel.txt = updateTooltipText("green")
                if (amount.toInt() >= 5) {
                    soilButton.isVisible = true
                    if (soilUpgrades.toInt() >= 1) {
                        blueButton.isVisible = true
                    }
                }
                if (amount.toInt() >= 1) {
                    stage.fire(AchievementNotificationEvent(4))
                }
            }
            "blue" -> {
                blueOwned = amount
                blueToolTipLabel.txt = updateTooltipText("blue")
                if (amount.toInt() >= 5) {
                    purpleButton.isVisible = true
                }
                if (amount.toInt() >= 1) {
                    stage.fire(AchievementNotificationEvent(8))
                }
            }
            "purple" -> {
                purpleOwned = amount
                purpleToolTipLabel.txt = updateTooltipText("purple")
                if (amount.toInt() >= 5) {
                    pinkButton.isVisible = true
                }
                if (amount.toInt() >= 1) {
                    stage.fire(AchievementNotificationEvent(9))
                }
            }
            "pink" -> {
                pinkOwned = amount
                pinkToolTipLabel.txt = updateTooltipText("pink")
                if (amount.toInt() >= 5) {
                    brownButton.isVisible = true
                }
                if (amount.toInt() >= 1) {
                    stage.fire(AchievementNotificationEvent(10))
                }
            }
            "brown" -> {
                brownOwned = amount
                brownToolTipLabel.txt = updateTooltipText("brown")
                if (amount.toInt() >= 5) {
                    whiteButton.isVisible = true
                }
                if (amount.toInt() >= 1) {
                    stage.fire(AchievementNotificationEvent(14))
                }
            }
            "white" -> {
                whiteOwned = amount
                whiteToolTipLabel.txt = updateTooltipText("white")
                if (amount.toInt() >= 5) {
                    blackButton.isVisible = true
                }
                if (amount.toInt() >= 1) {
                    stage.fire(AchievementNotificationEvent(15))
                }
            }
            "black" -> {
                blackOwned = amount
                blackToolTipLabel.txt = updateTooltipText("black")
                if (amount.toInt() >= 1) {
                    stage.fire(AchievementNotificationEvent(18))
                }
            }
        }
    }
    private fun costChanged(name : String, cost : BigDecimal) {
        when (name) {
            "red" -> {
                redCost = cost
            }
            "orange" -> {
                orangeCost = cost
            }
            "yellow" -> {
                yellowCost = cost
            }
            "green" -> {
                greenCost = cost
            }
            "blue" -> {
                blueCost = cost
            }
            "purple" -> {
                purpleCost = cost
            }
            "pink" -> {
                pinkCost = cost
            }
            "brown" -> {
                brownCost = cost
            }
            "white" -> {
                whiteCost = cost
            }
            "black" -> {
                blackCost = cost
            }
        }
    }
    private fun valueChanged(name : String, value : BigDecimal) {
        when (name) {
            "red" -> {
                redValue = value
                redValueLabel.txt = formatNumberWithLetter(value)
            }
            "orange" -> {
                orangeValue = value
                orangeValueLabel.txt = formatNumberWithLetter(value)
            }
            "yellow" -> {
                yellowValue = value
                yellowValueLabel.txt = formatNumberWithLetter(value)
            }
            "green" -> {
                greenValue = value
                greenValueLabel.txt = formatNumberWithLetter(value)
            }
            "blue" -> {
                blueValue = value
                blueValueLabel.txt = formatNumberWithLetter(value)
            }
            "purple" -> {
                purpleValue = value
                purpleValueLabel.txt = formatNumberWithLetter(value)
            }
            "pink" -> {
                pinkValue = value
                pinkValueLabel.txt = formatNumberWithLetter(value)
            }
            "brown" -> {
                brownValue = value
                brownValueLabel.txt = formatNumberWithLetter(value)
            }
            "white" -> {
                whiteValue = value
                whiteValueLabel.txt = formatNumberWithLetter(value)
            }
            "black" -> {
                blackValue = value
                blackValueLabel.txt = formatNumberWithLetter(value)
            }
        }
    }
    private fun rateChanged(name : String, rate : BigDecimal) {
        when (name) {
            "red" -> {
                redRate = rate
                redButton.txt = updateButtonText("red")
            }
            "orange" -> {
                orangeRate = rate
                orangeButton.txt = updateButtonText("orange")
            }
            "yellow" -> {
                yellowRate = rate
                yellowButton.txt = updateButtonText("yellow")
            }
            "green" -> {
                greenRate = rate
                greenButton.txt = updateButtonText("green")
            }
            "blue" -> {
                blueRate = rate
                blueButton.txt = updateButtonText("blue")
            }
            "purple" -> {
                purpleRate = rate
                purpleButton.txt = updateButtonText("purple")
            }
            "pink" -> {
                pinkRate = rate
                pinkButton.txt = updateButtonText("pink")
            }
            "brown" -> {
                brownRate = rate
                brownButton.txt = updateButtonText("brown")
            }
            "white" -> {
                whiteRate = rate
                whiteButton.txt = updateButtonText("white")
            }
            "black" -> {
                blackRate = rate
                blackButton.txt = updateButtonText("black")
            }
        }
    }
    private fun updateButtonText(buttonName : String) : String {
        return when (buttonName) {
            "red" -> { "Crops/s: ${formatNumberWithDecimal(redRate)} (+$redRateIncrease)\n${formatNumberWithLetter(redCost)} gold" }
            "orange" -> { "Crops/s: ${formatNumberWithDecimal(orangeRate)} (+$orangeRateIncrease)\n${formatNumberWithLetter(orangeCost)} gold" }
            "yellow" -> { "Crops/s: ${formatNumberWithDecimal(yellowRate)} (+$yellowRateIncrease)\n${formatNumberWithLetter(yellowCost)} gold" }
            "green" -> { "Crops/s: ${formatNumberWithDecimal(greenRate)} (+$greenRateIncrease)\n${formatNumberWithLetter(greenCost)} gold" }
            "blue" -> { "Crops/s: ${formatNumberWithDecimal(blueRate)} (+$blueRateIncrease)\n${formatNumberWithLetter(blueCost)} gold" }
            "purple" -> { "Crops/s: ${formatNumberWithDecimal(purpleRate)} (+$purpleRateIncrease)\n${formatNumberWithLetter(purpleCost)} gold" }
            "pink" -> { "Crops/s: ${formatNumberWithDecimal(pinkRate)} (+$pinkRateIncrease)\n${formatNumberWithLetter(pinkCost)} gold" }
            "brown" -> { "Crops/s: ${formatNumberWithDecimal(brownRate)} (+$brownRateIncrease)\n${formatNumberWithLetter(brownCost)} gold" }
            "white" -> { "Crops/s: ${formatNumberWithDecimal(whiteRate)} (+$whiteRateIncrease)\n${formatNumberWithLetter(whiteCost)} gold" }
            "black" -> { "Crops/s: ${formatNumberWithDecimal(blackRate)} (+$blackRateIncrease)\n${formatNumberWithLetter(blackCost)} gold" }
            else -> {
                "Invalid Button Name"
            }
        }
    }
    private fun updateTooltipText(tooltipName : String) : String {
        return when (tooltipName) {
            "red" -> { "Name: Red\nAmount: ${formatNumberNoDecimal(redOwned)} \nValue: ${formatNumberWithDecimal(redValueIncrease)}" }
            "orange" -> { "Name: Orange\nAmount: ${formatNumberNoDecimal(orangeOwned)} \nValue: ${formatNumberWithDecimal(orangeValueIncrease)}" }
            "yellow" -> { "Name: Yellow\nAmount: ${formatNumberNoDecimal(yellowOwned)} \nValue: ${formatNumberWithDecimal(yellowValueIncrease)}" }
            "green" -> { "Name: Green\nAmount: ${formatNumberNoDecimal(greenOwned)} \nValue: ${formatNumberWithDecimal(greenValueIncrease)}" }
            "blue" -> { "Name: Blue\nAmount: ${formatNumberNoDecimal(blueOwned)} \nValue: ${formatNumberWithDecimal(blueValueIncrease)}" }
            "purple" -> { "Name: Purple\nAmount: ${formatNumberNoDecimal(purpleOwned)} \nValue: ${formatNumberWithDecimal(purpleValueIncrease)}" }
            "pink" -> { "Name: Pink\nAmount: ${formatNumberNoDecimal(pinkOwned)} \nValue: ${formatNumberWithDecimal(pinkValueIncrease)}" }
            "brown" -> { "Name: Brown\nAmount: ${formatNumberNoDecimal(brownOwned)} \nValue: ${formatNumberWithDecimal(brownValueIncrease)}" }
            "white" -> { "Name: White\nAmount: ${formatNumberNoDecimal(whiteOwned)} \nValue: ${formatNumberWithDecimal(whiteValueIncrease)}" }
            "black" -> { "Name: Black\nAmount: ${formatNumberNoDecimal(blackOwned)} \nValue: ${formatNumberWithDecimal(blackValueIncrease)}" }
            else -> {
                "Invalid Tooltip Name"
            }
        }
    }
    private fun goldCoinAmountChanged(amount : BigDecimal) {
        goldCoins = amount
        goldCoinsLabel.txt = "You have ${formatNumberWithLetter(amount)} gold coins."
        updateAvailable(amount)
        if (amount >= BigDecimal(1_000_000)) {
            stage.fire(AchievementNotificationEvent(6))
        }
        if (amount >= BigDecimal(1_000_000_000_000)) {
            stage.fire(AchievementNotificationEvent(13))
        }
        if (amount >= BigDecimal("1e33")) {
            stage.fire(AchievementNotificationEvent(20))
        }
        if (amount >= BigDecimal("1e50")) {
            stage.fire(AchievementNotificationEvent(22))
        }
    }
    private fun productionRateChanged(rate : BigDecimal) {
        productionRate = rate
        productionRateLabel.txt = "You are producing ${formatNumberWithLetter(rate)} food per second."
        val prodMan = BigDecimalMath.mantissa(productionRate)
        val prodExp = BigDecimalMath.exponent(productionRate).toBigDecimal()
        val expPercent = prodExp.divide(PLANETARY_EXPONENT, 6, RoundingMode.UP)
        val manPercent = expPercent * prodMan.divide(BigDecimal(10))
        var prodPercent = 0f

        if (expPercent != null && expPercent < BigDecimal(1 / 308)) {
            prodPercent = manPercent.toFloat()
        }

        if (productionRate > ONE && expPercent != null) {
            prodPercent = (expPercent + manPercent).toFloat()
        }
        productionRateProgressLabel.txt = "${"%.2f".format(prodPercent.coerceAtMost(1f) * 100f)} %"
        colonizationProgress.scaleX = prodPercent
    }
    private fun buyAmountChange(amount : Float) {
        //setBuyAmountButton.txt = "Buy ${amount.roundToInt()}"
    }
    private fun updateAvailable(amount : BigDecimal) {
        redButton.isDisabled = amount < redCost
        orangeButton.isDisabled = amount < orangeCost
        yellowButton.isDisabled = amount < yellowCost
        greenButton.isDisabled = amount < greenCost
        blueButton.isDisabled = amount < blueCost
        purpleButton.isDisabled = amount < purpleCost
        pinkButton.isDisabled = amount < pinkCost
        brownButton.isDisabled = amount < brownCost
        whiteButton.isDisabled = amount < whiteCost
        blackButton.isDisabled = amount < blackCost
    }
    private fun achMultChanged(mult : BigDecimal) {
        // not displaying properly, but i think the value is correct and the multiplier is in effect
        multiplierValueLabel.txt = formatNumberWithDecimal(mult)
    }
    private fun soilUpgradesChanged(amount : BigDecimal) {
        soilButton.txt = "Upgrade Soil (${amount.toInt()})"
        soilUpgrades = amount
    }

    private fun formatBuyButton(cost : BigDecimal) : String {
        return if (cost > BigDecimal(999)) {
            "Buy ${buyAmount.roundToInt()}\nAssign: ${formatExponentNoDec(cost)} AP"
        } else {
            "Buy ${buyAmount.roundToInt()}\nAssign: ${formatNumberNoDecimal(cost)} AP"
        }
    }

    private fun formatNumberWithLetter(number : BigDecimal) : String {
        return if (number < MILLION) {
            twoDecimalWithCommasFormat.format(number)
        } else if (number < BILLION) {
            twoDecimalWithCommasFormat.format(number.divide(MILLION, 6, RoundingMode.UP)) + " M"
        } else if (number < TRILLION) {
            twoDecimalWithCommasFormat.format(number.divide(BILLION, 6, RoundingMode.UP)) + " B"
        } else if (number < QUADRILLION) {
            twoDecimalWithCommasFormat.format(number.divide(TRILLION, 6, RoundingMode.UP)) + " T"
        } else if (number < QUINTILLION) {
            twoDecimalWithCommasFormat.format(number.divide(QUADRILLION, 6, RoundingMode.UP)) + " Qa"
        } else if (number < SEXTILLION) {
            twoDecimalWithCommasFormat.format(number.divide(QUINTILLION, 6, RoundingMode.UP)) + " Qi"
        } else if (number < SEPTILLION) {
            twoDecimalWithCommasFormat.format(number.divide(SEXTILLION, 6, RoundingMode.UP)) + " Sx"
        } else if (number < OCTILLION) {
            twoDecimalWithCommasFormat.format(number.divide(SEPTILLION, 6, RoundingMode.UP)) + " Sp"
        } else if (number < NONILLION) {
            twoDecimalWithCommasFormat.format(number.divide(OCTILLION, 6, RoundingMode.UP)) + " Oc"
        } else if (number < DECILLION) {
            twoDecimalWithCommasFormat.format(number.divide(NONILLION, 6, RoundingMode.UP)) + " No"
        } else if (number < UNDECILLION) {
            twoDecimalWithCommasFormat.format(number.divide(DECILLION, 6, RoundingMode.UP)) + " Dc"
        } else {
            formatExponent2Dec(number)
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
        if (amount >= PLANETARY_SCORE) {
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
        private val ZERO = BigDecimal(0)
        private val ONE = BigDecimal(1)
        private val PLANETARY_EXPONENT = BigDecimal(308)
        private val PLANETARY_SCORE = BigDecimal(1e308)
        private val MILLION = BigDecimal(1e6)
        private val BILLION = BigDecimal(1e9)
        private val TRILLION = BigDecimal(1e12)
        private val QUADRILLION = BigDecimal(1e15)
        private val QUINTILLION = BigDecimal(1e18)
        private val SEXTILLION = BigDecimal(1e21)
        private val SEPTILLION = BigDecimal(1e24)
        private val OCTILLION = BigDecimal(1e27)
        private val NONILLION = BigDecimal(1e30)
        private val DECILLION = BigDecimal(1e33)
        private val UNDECILLION = BigDecimal(1e36)
    }
}

@Scene2dDsl
fun <S> KWidget<S>.planetView(
    model : PlanetModel,
    skin : Skin = Scene2DSkin.defaultSkin,
    init : PlanetView.(S) -> Unit = { }
) : PlanetView = actor(PlanetView(model, skin), init)
