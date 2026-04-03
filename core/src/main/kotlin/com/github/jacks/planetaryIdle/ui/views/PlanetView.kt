package com.github.jacks.planetaryIdle.ui.views

import ch.obermuhlner.math.big.BigDecimalMath
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
import com.github.jacks.planetaryIdle.components.PlanetResources
import com.github.jacks.planetaryIdle.events.AchievementNotificationEvent
import com.github.jacks.planetaryIdle.events.BuyResourceEvent
import com.github.jacks.planetaryIdle.events.FloatingTextEvent
import com.github.jacks.planetaryIdle.events.GameCompletedEvent
import com.github.jacks.planetaryIdle.events.UpgradeSoilEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.Colors
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.get
import com.github.jacks.planetaryIdle.ui.models.PlanetModel
import com.github.jacks.planetaryIdle.ui.models.ResourceModelState
import com.github.jacks.planetaryIdle.ui.views.HeaderView.Companion.formatShort
import ktx.actors.txt
import ktx.log.logger
import ktx.math.vec2
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.stack
import ktx.scene2d.table
import ktx.scene2d.textButton
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/** Holds all mutable UI widgets for one planet-resource row. */
private data class ResourceWidgets(
    val rowTable: Table,
    val button: TextButton,
    val progressFill: Image,
    val tooltipLabel: Label,
)

class PlanetView(
    private val model: PlanetModel,
    private val stage: Stage,
    private val goldLabel: Label,
    skin: Skin,
) : Table(skin), KTable {

    private val noDecFormat = DecimalFormat("#,##0", DecimalFormatSymbols.getInstance())

    private val resourceWidgets = mutableMapOf<PlanetResources, ResourceWidgets>()
    private val localStates     = mutableMapOf<PlanetResources, ResourceModelState>()
    private val elapsedSeconds  = mutableMapOf<PlanetResources, Float>()

    private var goldCoins    = model.goldCoins
    private var soilUpgrades = model.soilUpgrades
    private var soilCost     = model.soilCost

    private lateinit var soilButton: TextButton
    private lateinit var productionRateLabel: Label
    private lateinit var colonizationProgress: Image
    private lateinit var productionRateProgressLabel: Label

    init {
        setFillParent(true)
        val view = this@PlanetView   // explicit capture for use inside DSL lambdas

        PlanetResources.entries.forEach { resource ->
            localStates[resource] = stateFor(resource)
            elapsedSeconds[resource] = 0f
        }

        table { gameCell ->
            // ── Resource rows ──────────────────────────────────────────────
            table { rowsCell ->
                PlanetResources.entries.forEach { resource ->
                    val state = view.localStates[resource]!!
                    val rowTable = table { rowCell ->
                        // Buy button
                        val btn = textButton(
                            view.makeButtonText(resource, state),
                            view.buttonStyleFor(resource)
                        ) { cell ->
                            cell.left().width(210f).height(55f).pad(3f, 5f, 3f, 0f)
                            isDisabled = view.goldCoins < state.cost
                            addListener(object : ChangeListener() {
                                override fun changed(event: ChangeEvent, actor: Actor) {
                                    view.stage.fire(BuyResourceEvent(resource.resourceName))
                                }
                            })
                        }

                        // Progress bar: grey background + colored fill in a Stack
                        var fillImage: Image? = null
                        stack { stackCell ->
                            image(view.skin[Drawables.BAR_GREY_THICK])
                            fillImage = image(view.skin[Drawables.BAR_GREY_THICK]) {
                                color = view.colorFor(resource).color
                                scaleX = 0f
                            }
                            stackCell.expandX().fillX().height(30f).pad(3f, 5f, 3f, 5f)
                        }

                        // Hover tooltip
                        val tooltip = label(
                            view.makeTooltipText(resource, state),
                            view.tooltipStyleFor(resource)
                        ) { cell ->
                            cell.width(160f).height(65f).pad(0f, 0f, 0f, 5f)
                            setAlignment(Align.center)
                            isVisible = false
                        }

                        btn.addListener(object : InputListener() {
                            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                                if (pointer == -1) tooltip.isVisible = true
                            }
                            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                                if (pointer == -1) tooltip.isVisible = false
                            }
                        })

                        rowCell.expandX().fillX()

                        view.resourceWidgets[resource] = ResourceWidgets(
                            rowTable    = this,
                            button      = btn,
                            progressFill = fillImage!!,
                            tooltipLabel = tooltip,
                        )
                    }
                    row()
                    // Visibility resolved at end of init via reapplyUnlockVisibility()
                    rowTable.isVisible = resource == PlanetResources.RED
                }

                // Soil upgrade button
                row()
                view.soilButton = textButton(
                    view.makeSoilButtonText(),
                    Buttons.BLACK_BUTTON_SMALL.skinKey
                ) { cell ->
                    cell.left().width(210f).height(55f).pad(3f, 5f, 3f, 0f)
                    isVisible = false
                    isDisabled = view.goldCoins < view.soilCost
                    addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) {
                            view.stage.fire(UpgradeSoilEvent())
                        }
                    })
                }

                rowsCell.expandY().fillY().top().left()
            }

            row()
            image(skin[Drawables.BAR_BLACK_THIN]) { cell ->
                cell.expandX().fillX().height(2f)
            }
            row()

            // ── Bottom info row ────────────────────────────────────────────
            table { bottomCell ->
                view.productionRateLabel = label(
                    "Production: ${formatShort(view.model.productionRate)}/s",
                    Labels.SMALL.skinKey
                ) { cell ->
                    cell.expandX().left().padLeft(10f)
                }
                bottomCell.expandX().fillX().height(30f)
            }
            row()

            // ── Colonization progress bar ──────────────────────────────────
            table { barCell ->
                stack { stackCell ->
                    image(view.skin[Drawables.BAR_GREY_THICK])
                    view.colonizationProgress = image(view.skin[Drawables.BAR_GREEN_THICK]) {
                        scaleX = 0f
                    }
                    view.productionRateProgressLabel = label("0.00 %", Labels.MEDIUM.skinKey) {
                        setAlignment(Align.center)
                    }
                    stackCell.center().width(600f).height(30f)
                }
                barCell.expandX().top().height(44f).padTop(5f)
            }

            gameCell.expand().fill().pad(5f, 0f, 5f, 0f)
        }

        // ── Model bindings ─────────────────────────────────────────────────
        model.onPropertyChange(PlanetModel::goldCoins) { amount ->
            goldCoins = amount
            updateAllButtonDisabledState()
            checkGoldAchievements(amount)
        }
        model.onPropertyChange(PlanetModel::productionRate) { rate ->
            productionRateLabel.txt = "Production: ${formatShort(rate)}/s"
            updateColonizationBar(rate)
        }
        model.onPropertyChange(PlanetModel::soilCost) { cost ->
            soilCost = cost
            soilButton.txt = makeSoilButtonText()
        }
        model.onPropertyChange(PlanetModel::soilUpgrades) { amount ->
            soilUpgrades = amount
            soilButton.txt = makeSoilButtonText()
            resetButtonVisibility()
            reapplyUnlockVisibility()
            checkSoilAchievements(amount)
        }
        model.onPropertyChange(PlanetModel::gameCompleted) { completed ->
            if (completed) stage.fire(GameCompletedEvent())
        }

        model.onPropertyChange(PlanetModel::redState)    { stateChanged(PlanetResources.RED,    it) }
        model.onPropertyChange(PlanetModel::orangeState) { stateChanged(PlanetResources.ORANGE, it) }
        model.onPropertyChange(PlanetModel::yellowState) { stateChanged(PlanetResources.YELLOW, it) }
        model.onPropertyChange(PlanetModel::greenState)  { stateChanged(PlanetResources.GREEN,  it) }
        model.onPropertyChange(PlanetModel::blueState)   { stateChanged(PlanetResources.BLUE,   it) }
        model.onPropertyChange(PlanetModel::purpleState) { stateChanged(PlanetResources.PURPLE, it) }
        model.onPropertyChange(PlanetModel::pinkState)   { stateChanged(PlanetResources.PINK,   it) }
        model.onPropertyChange(PlanetModel::brownState)  { stateChanged(PlanetResources.BROWN,  it) }
        model.onPropertyChange(PlanetModel::whiteState)  { stateChanged(PlanetResources.WHITE,  it) }
        model.onPropertyChange(PlanetModel::blackState)  { stateChanged(PlanetResources.BLACK,  it) }

        // Floating text: fires when a production cycle completes, then gold is credited by animation end
        model.onPropertyChange(PlanetModel::lastProductionPayout) { (name, amount) ->
            if (name.isEmpty()) return@onPropertyChange
            val resource = PlanetResources.entries.find { it.resourceName == name } ?: return@onPropertyChange
            elapsedSeconds[resource] = 0f

            val fillImage = resourceWidgets[resource]?.progressFill ?: return@onPropertyChange
            val startPos  = fillImage.localToStageCoordinates(vec2(fillImage.width, fillImage.height / 2f))
            val targetPos = goldLabel.localToStageCoordinates(vec2(goldLabel.width / 2f, goldLabel.height / 2f))
            stage.fire(FloatingTextEvent(startPos, targetPos, amount, "+${formatShort(amount)}"))
        }

        reapplyUnlockVisibility()
    }

    // ── act() — progress bar scaleX driven by local elapsed-time timer ──────
    override fun act(delta: Float) {
        super.act(delta)
        PlanetResources.entries.forEach { resource ->
            val state = localStates[resource] ?: return@forEach
            if (state.owned <= BigDecimal.ZERO || state.cycleDuration <= BigDecimal.ZERO) return@forEach
            val elapsed = (elapsedSeconds[resource] ?: 0f) + delta
            elapsedSeconds[resource] = elapsed
            val progress = (elapsed / state.cycleDuration.toFloat()).coerceIn(0f, 1f)
            resourceWidgets[resource]?.progressFill?.scaleX = progress
        }
    }

    // ── State change handler ─────────────────────────────────────────────────
    private fun stateChanged(resource: PlanetResources, state: ResourceModelState) {
        localStates[resource] = state
        val widgets = resourceWidgets[resource] ?: return
        widgets.button.txt = makeButtonText(resource, state)
        widgets.button.isDisabled = goldCoins < state.cost
        widgets.tooltipLabel.txt = makeTooltipText(resource, state)
        reapplyUnlockVisibility()
        checkOwnedAchievements(resource, state.owned)
    }

    // ── Unlock / visibility ───────────────────────────────────────────────────
    private fun reapplyUnlockVisibility() {
        fun owned(r: PlanetResources) = (localStates[r]?.owned ?: BigDecimal.ZERO).toInt()
        val soil = soilUpgrades.toInt()

        resourceWidgets[PlanetResources.ORANGE]?.rowTable?.isVisible = owned(PlanetResources.RED)    >= 5
        resourceWidgets[PlanetResources.YELLOW]?.rowTable?.isVisible = owned(PlanetResources.ORANGE) >= 5
        resourceWidgets[PlanetResources.GREEN]?.rowTable?.isVisible  = owned(PlanetResources.YELLOW) >= 5
        resourceWidgets[PlanetResources.BLUE]?.rowTable?.isVisible   = owned(PlanetResources.GREEN)  >= 5 && soil >= 1
        resourceWidgets[PlanetResources.PURPLE]?.rowTable?.isVisible = owned(PlanetResources.BLUE)   >= 5 && soil >= 3
        resourceWidgets[PlanetResources.PINK]?.rowTable?.isVisible   = owned(PlanetResources.PURPLE) >= 5 && soil >= 5
        resourceWidgets[PlanetResources.BROWN]?.rowTable?.isVisible  = owned(PlanetResources.PINK)   >= 5 && soil >= 7
        resourceWidgets[PlanetResources.WHITE]?.rowTable?.isVisible  = owned(PlanetResources.BROWN)  >= 5 && soil >= 9
        resourceWidgets[PlanetResources.BLACK]?.rowTable?.isVisible  = owned(PlanetResources.WHITE)  >= 5 && soil >= 11
        soilButton.isVisible = owned(PlanetResources.GREEN) >= 5
    }

    private fun resetButtonVisibility() {
        listOf(
            PlanetResources.ORANGE, PlanetResources.YELLOW, PlanetResources.GREEN,
            PlanetResources.BLUE, PlanetResources.PURPLE, PlanetResources.PINK,
            PlanetResources.BROWN, PlanetResources.WHITE, PlanetResources.BLACK,
        ).forEach { resourceWidgets[it]?.rowTable?.isVisible = false }
    }

    private fun updateAllButtonDisabledState() {
        PlanetResources.entries.forEach { resource ->
            val state = localStates[resource] ?: return@forEach
            resourceWidgets[resource]?.button?.isDisabled = goldCoins < state.cost
        }
        soilButton.isDisabled = goldCoins < soilCost
    }

    // ── Achievement notifications ─────────────────────────────────────────────
    private fun checkOwnedAchievements(resource: PlanetResources, owned: BigDecimal) {
        val n = owned.toInt()
        when (resource) {
            PlanetResources.RED    -> {
                if (n >= 1)   stage.fire(AchievementNotificationEvent(1))
                if (n >= 50)  stage.fire(AchievementNotificationEvent(5))
                if (n >= 250) stage.fire(AchievementNotificationEvent(17))
            }
            PlanetResources.ORANGE -> if (n >= 1) stage.fire(AchievementNotificationEvent(2))
            PlanetResources.YELLOW -> if (n >= 1) stage.fire(AchievementNotificationEvent(3))
            PlanetResources.GREEN  -> if (n >= 1) stage.fire(AchievementNotificationEvent(4))
            PlanetResources.BLUE   -> if (n >= 1) stage.fire(AchievementNotificationEvent(8))
            PlanetResources.PURPLE -> if (n >= 1) stage.fire(AchievementNotificationEvent(9))
            PlanetResources.PINK   -> if (n >= 1) stage.fire(AchievementNotificationEvent(10))
            PlanetResources.BROWN  -> if (n >= 1) stage.fire(AchievementNotificationEvent(14))
            PlanetResources.WHITE  -> if (n >= 1) stage.fire(AchievementNotificationEvent(15))
            PlanetResources.BLACK  -> if (n >= 1) stage.fire(AchievementNotificationEvent(18))
        }
    }

    private fun checkGoldAchievements(amount: BigDecimal) {
        if (amount >= BigDecimal(1_000_000L))         stage.fire(AchievementNotificationEvent(6))
        if (amount >= BigDecimal(1_000_000_000_000L)) stage.fire(AchievementNotificationEvent(13))
        if (amount >= BigDecimal("1e33"))             stage.fire(AchievementNotificationEvent(20))
        if (amount >= BigDecimal("1e50"))             stage.fire(AchievementNotificationEvent(22))
    }

    private fun checkSoilAchievements(amount: BigDecimal) {
        val n = amount.toInt()
        if (n >= 1)  stage.fire(AchievementNotificationEvent(7))
        if (n >= 5)  stage.fire(AchievementNotificationEvent(12))
        if (n >= 10) stage.fire(AchievementNotificationEvent(16))
        if (n >= 25) stage.fire(AchievementNotificationEvent(21))
    }

    // ── Colonization progress bar ─────────────────────────────────────────────
    private fun updateColonizationBar(rate: BigDecimal) {
        val prodMantissa = BigDecimalMath.mantissa(rate)
        val prodExponent = BigDecimalMath.exponent(rate).toBigDecimal()
        val expPercent   = prodExponent.divide(PLANETARY_EXPONENT, 6, RoundingMode.UP)
        val manPercent   = expPercent.multiply(prodMantissa.divide(BigDecimal(10), 10, RoundingMode.HALF_UP))
        val prodPercent  = if (rate > BigDecimal.ONE) (expPercent + manPercent).toFloat() else manPercent.toFloat()
        val clamped      = prodPercent.coerceIn(0f, 1f)
        productionRateProgressLabel.txt = "${"%.2f".format(clamped * 100f)} %"
        colonizationProgress.scaleX = clamped
    }

    // ── Text helpers ──────────────────────────────────────────────────────────
    private fun makeButtonText(resource: PlanetResources, state: ResourceModelState): String {
        val name  = resource.resourceName.replaceFirstChar { it.uppercaseChar() }
        val owned = noDecFormat.format(state.owned)
        val cost  = formatShort(state.cost)
        return "$name ($owned)\n$cost gold"
    }

    private fun makeTooltipText(resource: PlanetResources, state: ResourceModelState): String {
        val payout   = formatShort(state.payout)
        val duration = "%.1f".format(state.cycleDuration.toFloat())
        return "Payout: $payout\nCycle: ${duration}s"
    }

    private fun makeSoilButtonText(): String =
        "Upgrade Soil (${soilUpgrades.toInt()})\n${formatShort(soilCost)} gold"

    // ── Skin / style helpers ──────────────────────────────────────────────────
    private fun buttonStyleFor(resource: PlanetResources): String = when (resource) {
        PlanetResources.RED    -> Buttons.RED_BUTTON_SMALL.skinKey
        PlanetResources.ORANGE -> Buttons.ORANGE_BUTTON_SMALL.skinKey
        PlanetResources.YELLOW -> Buttons.YELLOW_BUTTON_SMALL.skinKey
        PlanetResources.GREEN  -> Buttons.GREEN_BUTTON_SMALL.skinKey
        PlanetResources.BLUE   -> Buttons.BLUE_BUTTON_SMALL.skinKey
        PlanetResources.PURPLE -> Buttons.PURPLE_BUTTON_SMALL.skinKey
        PlanetResources.PINK   -> Buttons.PINK_BUTTON_SMALL.skinKey
        PlanetResources.BROWN  -> Buttons.BROWN_BUTTON_SMALL.skinKey
        PlanetResources.WHITE  -> Buttons.WHITE_BUTTON_SMALL.skinKey
        PlanetResources.BLACK  -> Buttons.BLACK_BUTTON_SMALL.skinKey
    }

    private fun colorFor(resource: PlanetResources): Colors = when (resource) {
        PlanetResources.RED    -> Colors.RED
        PlanetResources.ORANGE -> Colors.ORANGE
        PlanetResources.YELLOW -> Colors.YELLOW
        PlanetResources.GREEN  -> Colors.GREEN
        PlanetResources.BLUE   -> Colors.BLUE
        PlanetResources.PURPLE -> Colors.PURPLE
        PlanetResources.PINK   -> Colors.PINK
        PlanetResources.BROWN  -> Colors.BROWN
        PlanetResources.WHITE  -> Colors.WHITE
        PlanetResources.BLACK  -> Colors.BLACK
    }

    private fun tooltipStyleFor(resource: PlanetResources): String = when (resource) {
        PlanetResources.RED    -> Labels.SMALL_RED_BGD.skinKey
        PlanetResources.ORANGE -> Labels.SMALL_ORANGE_BGD.skinKey
        PlanetResources.YELLOW -> Labels.SMALL_YELLOW_BGD.skinKey
        PlanetResources.GREEN  -> Labels.SMALL_GREEN_BGD.skinKey
        PlanetResources.BLUE   -> Labels.SMALL_BLUE_BGD.skinKey
        PlanetResources.PURPLE -> Labels.SMALL_PURPLE_BGD.skinKey
        PlanetResources.PINK   -> Labels.SMALL_PINK_BGD.skinKey
        PlanetResources.BROWN  -> Labels.SMALL_BROWN_BGD.skinKey
        PlanetResources.WHITE  -> Labels.SMALL_WHITE_BGD.skinKey
        PlanetResources.BLACK  -> Labels.SMALL_BLACK_BGD.skinKey
    }

    // ── Initial state from model ──────────────────────────────────────────────
    private fun stateFor(resource: PlanetResources): ResourceModelState = when (resource) {
        PlanetResources.RED    -> model.redState
        PlanetResources.ORANGE -> model.orangeState
        PlanetResources.YELLOW -> model.yellowState
        PlanetResources.GREEN  -> model.greenState
        PlanetResources.BLUE   -> model.blueState
        PlanetResources.PURPLE -> model.purpleState
        PlanetResources.PINK   -> model.pinkState
        PlanetResources.BROWN  -> model.brownState
        PlanetResources.WHITE  -> model.whiteState
        PlanetResources.BLACK  -> model.blackState
    }

    companion object {
        private val log             = logger<PlanetView>()
        private val PLANETARY_EXPONENT = BigDecimal(308)
        @Suppress("unused")
        private val PLANETARY_SCORE    = BigDecimal(1e308)
    }
}

@Scene2dDsl
fun <S> KWidget<S>.planetView(
    model: PlanetModel,
    stage: Stage,
    goldLabel: Label,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: PlanetView.(S) -> Unit = {},
): PlanetView = actor(PlanetView(model, stage, goldLabel, skin), init)
