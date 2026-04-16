package com.github.jacks.planetaryIdle.ui.views

import ch.obermuhlner.math.big.BigDecimalMath
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.github.jacks.planetaryIdle.components.PlanetResources
import com.github.jacks.planetaryIdle.events.AchievementNotificationEvent
import com.github.jacks.planetaryIdle.events.BuyResourceEvent
import com.github.jacks.planetaryIdle.events.FloatingTextEvent
import com.github.jacks.planetaryIdle.events.GameCompletedEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.Colors
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.get
import com.github.jacks.planetaryIdle.ui.models.FarmModel
import com.github.jacks.planetaryIdle.ui.models.KitchenViewModel
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

/** Holds all mutable UI widgets for one farm-resource row. */
private data class ResourceWidgets(
    val rowTable: Table,
    val button: TextButton,
    val dropdownButton: TextButton,
    val progressFill: Image,
    val tooltipLabel: Label,
    val milestoneLabel: Label,
)

class FarmView(
    private val model: FarmModel,
    private val kitchenModel: KitchenViewModel,
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

    private lateinit var productionRateLabel: Label
    private lateinit var colonizationProgress: Image
    private lateinit var productionRateProgressLabel: Label

    // Currently active recipe pairs for visual linking
    private val linkedColors = mutableSetOf<String>()  // colors currently in a recipe

    private val recipeLinkedDrawable: TextureRegionDrawable = run {
        val px = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        px.setColor(Color(1f, 1f, 1f, 0.15f))
        px.fill()
        val t = Texture(px); px.dispose()
        TextureRegionDrawable(t)
    }

    private val dimDrawable: TextureRegionDrawable = run {
        val px = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        px.setColor(Color(0f, 0f, 0f, 0.4f))
        px.fill()
        val t = Texture(px); px.dispose()
        TextureRegionDrawable(t)
    }

    init {
        setFillParent(true)
        val view = this@FarmView

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

                        // Buy button (color-styled)
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

                        // Crop dropdown arrow button (shown when kitchen has multiple options)
                        val dropBtn = textButton("v", Buttons.GREY_BUTTON_SMALL.skinKey) { cell ->
                            cell.width(24f).height(55f).pad(3f, 0f, 3f, 4f)
                            isVisible = false   // hidden until kitchen unlocks
                            addListener(object : ChangeListener() {
                                override fun changed(event: ChangeEvent, actor: Actor) {
                                    view.showCropDropdown(resource)
                                }
                            })
                        }

                        // Progress bar
                        var fillImage: Image? = null
                        stack { stackCell ->
                            image(view.skin[Drawables.BAR_GREY_THICK])
                            fillImage = image(view.skin[Drawables.BAR_GREY_THICK]) {
                                color = view.colorFor(resource).color
                                scaleX = 0f
                            }
                            stackCell.expandX().fillX().height(30f).pad(3f, 0f, 3f, 5f).prefWidth(0f)
                        }

                        val milestone = label(
                            view.nextMilestoneText(resource, state.owned.toLong()),
                            Labels.SMALL.skinKey
                        ) { cell ->
                            cell.width(90f).height(55f).pad(0f, 4f, 0f, 4f)
                            setAlignment(Align.center)
                            color = com.badlogic.gdx.graphics.Color.GRAY
                            isVisible = view.nextMilestoneFor(resource, state.owned.toLong()) != null
                        }

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
                            rowTable       = this,
                            button         = btn,
                            dropdownButton = dropBtn,
                            progressFill   = fillImage!!,
                            tooltipLabel   = tooltip,
                            milestoneLabel = milestone,
                        )
                    }
                    // Right-click on row opens crop dropdown (desktop) — added after rowTable is assigned
                    rowTable.addListener(object : InputListener() {
                        override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                            if (button == 1) {
                                view.showCropDropdown(resource)
                                return true
                            }
                            return false
                        }
                    })
                    row()
                    rowTable.isVisible = resource == PlanetResources.RED
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
        model.onPropertyChange(FarmModel::goldCoins) { amount ->
            goldCoins = amount
            updateAllButtonDisabledState()
            checkGoldAchievements(amount)
        }
        model.onPropertyChange(FarmModel::productionRate) { rate ->
            productionRateLabel.txt = "Production: ${formatShort(rate)}/s"
            updateColonizationBar(rate)
            if (rate >= PLANETARY_SCORE && !model.gameCompleted) {
                stage.fire(GameCompletedEvent())
            }
        }
        model.onPropertyChange(FarmModel::soilUpgrades) { amount ->
            soilUpgrades = amount
            reapplyUnlockVisibility()
            checkSoilAchievements(amount)
        }
        model.onPropertyChange(FarmModel::gameCompleted) { completed ->
            if (completed) stage.fire(GameCompletedEvent())
        }

        model.onPropertyChange(FarmModel::redState)    { stateChanged(PlanetResources.RED,    it) }
        model.onPropertyChange(FarmModel::orangeState) { stateChanged(PlanetResources.ORANGE, it) }
        model.onPropertyChange(FarmModel::yellowState) { stateChanged(PlanetResources.YELLOW, it) }
        model.onPropertyChange(FarmModel::greenState)  { stateChanged(PlanetResources.GREEN,  it) }
        model.onPropertyChange(FarmModel::blueState)   { stateChanged(PlanetResources.BLUE,   it) }
        model.onPropertyChange(FarmModel::purpleState) { stateChanged(PlanetResources.PURPLE, it) }
        model.onPropertyChange(FarmModel::pinkState)   { stateChanged(PlanetResources.PINK,   it) }
        model.onPropertyChange(FarmModel::brownState)  { stateChanged(PlanetResources.BROWN,  it) }
        model.onPropertyChange(FarmModel::whiteState)  { stateChanged(PlanetResources.WHITE,  it) }
        model.onPropertyChange(FarmModel::blackState)  { stateChanged(PlanetResources.BLACK,  it) }

        model.onPropertyChange(FarmModel::lastProductionPayout) { (name, amount) ->
            if (name.isEmpty()) return@onPropertyChange
            val resource = PlanetResources.entries.find { it.resourceName == name } ?: return@onPropertyChange
            elapsedSeconds[resource] = 0f
            val fillImage = resourceWidgets[resource]?.progressFill ?: return@onPropertyChange
            val startPos  = fillImage.localToStageCoordinates(vec2(fillImage.width, fillImage.height / 2f))
            val targetPos = goldLabel.localToStageCoordinates(vec2(goldLabel.width / 2f, goldLabel.height / 2f))
            stage.fire(FloatingTextEvent(startPos, targetPos, amount, "+${formatShort(amount)}"))
        }

        // Kitchen bindings
        kitchenModel.onPropertyChange(KitchenViewModel::kitchenUnlocked) { _ ->
            updateDropdownVisibility()
            updateButtonTexts()
        }
        kitchenModel.onPropertyChange(KitchenViewModel::unlockedCrops) { _ ->
            updateDropdownVisibility()
        }
        kitchenModel.onPropertyChange(KitchenViewModel::activeCrops) { _ ->
            updateButtonTexts()
        }
        kitchenModel.onPropertyChange(KitchenViewModel::activeRecipes) { recipes ->
            linkedColors.clear()
            recipes.forEach { r ->
                r.crops.forEach { linkedColors.add(it.color) }
            }
            updateRecipeVisuals()
        }

        reapplyUnlockVisibility()
    }

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

    // ── Crop dropdown ─────────────────────────────────────────────────────────

    private fun showCropDropdown(resource: PlanetResources) {
        if (!kitchenModel.kitchenUnlocked) return
        val color = resource.resourceName
        val unlocked = kitchenModel.unlockedCrops[color] ?: return
        if (unlocked.size <= 1) return

        val popup = Table(skin)
        popup.setBackground(dimDrawable)
        popup.pad(6f)

        unlocked.forEach { cropName ->
            val btn = TextButton(cropName, skin, Buttons.GREY_BUTTON_SMALL.skinKey)
            btn.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    kitchenModel.setActiveCrop(color, cropName)
                    popup.remove()
                }
            })
            popup.add(btn).expandX().fillX().height(32f).pad(1f)
            popup.row()
        }

        popup.pack()
        val stg = stage ?: return
        popup.setPosition(
            (stg.width / 2f - popup.width / 2f).coerceIn(0f, stg.width - popup.width),
            (stg.height / 2f - popup.height / 2f).coerceIn(0f, stg.height - popup.height),
        )
        stg.addActor(popup)
    }

    // ── Kitchen visual updates ────────────────────────────────────────────────

    private fun updateDropdownVisibility() {
        PlanetResources.entries.forEach { resource ->
            val color = resource.resourceName
            val unlocked = kitchenModel.unlockedCrops[color] ?: emptyList()
            resourceWidgets[resource]?.dropdownButton?.isVisible =
                kitchenModel.kitchenUnlocked && unlocked.size > 1
        }
    }

    private fun updateButtonTexts() {
        PlanetResources.entries.forEach { resource ->
            val state = localStates[resource] ?: return@forEach
            val widgets = resourceWidgets[resource] ?: return@forEach
            widgets.button.txt = makeButtonText(resource, state)
        }
    }

    private fun updateRecipeVisuals() {
        PlanetResources.entries.forEach { resource ->
            val widgets = resourceWidgets[resource] ?: return@forEach
            if (resource.resourceName in linkedColors) {
                widgets.rowTable.background = recipeLinkedDrawable
            } else {
                widgets.rowTable.background = null
            }
        }
    }

    // ── Existing helpers ──────────────────────────────────────────────────────

    private fun stateChanged(resource: PlanetResources, state: ResourceModelState) {
        localStates[resource] = state
        val widgets = resourceWidgets[resource] ?: return
        widgets.button.txt = makeButtonText(resource, state)
        widgets.button.isDisabled = goldCoins < state.cost
        widgets.tooltipLabel.txt = makeTooltipText(resource, state)
        updateMilestoneLabel(resource, state.owned.toLong(), widgets)
        reapplyUnlockVisibility()
        checkOwnedAchievements(resource, state.owned)
    }

    private fun updateMilestoneLabel(resource: PlanetResources, owned: Long, widgets: ResourceWidgets) {
        val next = nextMilestoneFor(resource, owned)
        if (next == null) {
            widgets.milestoneLabel.isVisible = false
        } else {
            widgets.milestoneLabel.isVisible = true
            widgets.milestoneLabel.txt = nextMilestoneText(resource, owned)
        }
    }

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
    }

    private fun updateAllButtonDisabledState() {
        PlanetResources.entries.forEach { resource ->
            val state = localStates[resource] ?: return@forEach
            resourceWidgets[resource]?.button?.isDisabled = goldCoins < state.cost
        }
    }

    // ── Achievement notifications ──────────────────────────────────────────
    private fun checkOwnedAchievements(resource: PlanetResources, owned: BigDecimal) {
        val n = owned.toLong()
        fun fire(id: String) = stage.fire(AchievementNotificationEvent(id))
        when (resource) {
            PlanetResources.RED -> {
                if (n >= 10)   fire("red_10")
                if (n >= 50)   fire("red_50")
                if (n >= 250)  fire("red_250")
                if (n >= 500)  fire("red_500")
                if (n >= 1000) fire("red_1000")
                if (n >= 5000) fire("red_5000")
            }
            PlanetResources.ORANGE -> {
                if (n >= 10)   fire("orange_10")
                if (n >= 100)  fire("orange_100")
                if (n >= 1000) fire("orange_1000")
            }
            PlanetResources.YELLOW -> {
                if (n >= 10)   fire("yellow_10")
                if (n >= 100)  fire("yellow_100")
                if (n >= 1000) fire("yellow_1000")
            }
            PlanetResources.GREEN -> {
                if (n >= 10)   fire("green_10")
                if (n >= 100)  fire("green_100")
                if (n >= 1000) fire("green_1000")
            }
            PlanetResources.BLUE -> {
                if (n >= 10)   fire("blue_10")
                if (n >= 100)  fire("blue_100")
                if (n >= 1000) fire("blue_1000")
            }
            PlanetResources.PURPLE -> {
                if (n >= 10)   fire("purple_10")
                if (n >= 100)  fire("purple_100")
                if (n >= 1000) fire("purple_1000")
            }
            PlanetResources.PINK -> {
                if (n >= 10)   fire("pink_10")
                if (n >= 100)  fire("pink_100")
                if (n >= 1000) fire("pink_1000")
            }
            PlanetResources.BROWN -> {
                if (n >= 10)   fire("brown_10")
                if (n >= 100)  fire("brown_100")
                if (n >= 1000) fire("brown_1000")
            }
            PlanetResources.WHITE -> {
                if (n >= 10)   fire("white_10")
                if (n >= 100)  fire("white_100")
                if (n >= 1000) fire("white_1000")
            }
            PlanetResources.BLACK -> {
                if (n >= 1)   fire("black_1")
                if (n >= 10)  fire("black_10")
                if (n >= 100) fire("black_100")
            }
        }
        checkFullSpectrumAchievement()
    }

    private fun checkFullSpectrumAchievement() {
        if (PlanetResources.entries.all { (localStates[it]?.owned ?: BigDecimal.ZERO) >= TEN }) {
            stage.fire(AchievementNotificationEvent("combined_full_spectrum"))
        }
    }

    private fun checkGoldAchievements(amount: BigDecimal) {
        if (amount >= GOLD_1M)    stage.fire(AchievementNotificationEvent("gold_1m"))
        if (amount >= GOLD_1B)    stage.fire(AchievementNotificationEvent("gold_1b"))
        if (amount >= GOLD_1T)    stage.fire(AchievementNotificationEvent("gold_1t"))
        if (amount >= GOLD_1Q)    stage.fire(AchievementNotificationEvent("gold_1q"))
        if (amount >= GOLD_1E33)  stage.fire(AchievementNotificationEvent("gold_1e33"))
        if (amount >= GOLD_1E50)  stage.fire(AchievementNotificationEvent("gold_1e50"))
        if (amount >= GOLD_1E75)  stage.fire(AchievementNotificationEvent("gold_1e75"))
        if (amount >= GOLD_1E100) stage.fire(AchievementNotificationEvent("gold_1e100"))
        if (amount >= GOLD_1E150) stage.fire(AchievementNotificationEvent("gold_1e150"))
        if (amount >= GOLD_1E200) stage.fire(AchievementNotificationEvent("gold_1e200"))
        if (amount >= GOLD_1E308) stage.fire(AchievementNotificationEvent("gold_1e308"))
    }

    private fun checkSoilAchievements(amount: BigDecimal) {
        val n = amount.toInt()
        if (n >= 1)  stage.fire(AchievementNotificationEvent("soil_1"))
        if (n >= 5)  stage.fire(AchievementNotificationEvent("soil_5"))
        if (n >= 10) stage.fire(AchievementNotificationEvent("soil_10"))
        if (n >= 25) stage.fire(AchievementNotificationEvent("soil_25"))
    }

    // ── Colonization progress bar ─────────────────────────────────────────
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

    // ── Milestone helpers ─────────────────────────────────────────────────────

    private fun milestonesFor(resource: PlanetResources): List<Long> = when (resource) {
        PlanetResources.RED    -> listOf(10, 50, 250, 500, 1000, 5000)
        PlanetResources.ORANGE -> listOf(10, 100, 1000)
        PlanetResources.YELLOW -> listOf(10, 100, 1000)
        PlanetResources.GREEN  -> listOf(10, 100, 1000)
        PlanetResources.BLUE   -> listOf(10, 100, 1000)
        PlanetResources.PURPLE -> listOf(10, 100, 1000)
        PlanetResources.PINK   -> listOf(10, 100, 1000)
        PlanetResources.BROWN  -> listOf(10, 100, 1000)
        PlanetResources.WHITE  -> listOf(10, 100, 1000)
        PlanetResources.BLACK  -> listOf(1, 10, 100)
    }

    fun nextMilestoneFor(resource: PlanetResources, owned: Long): Long? =
        milestonesFor(resource).firstOrNull { it > owned }

    fun nextMilestoneText(resource: PlanetResources, owned: Long): String {
        val next = nextMilestoneFor(resource, owned) ?: return ""
        return "→ ${noDecFormat.format(next)}"
    }

    // ── Text helpers ──────────────────────────────────────────────────────
    private fun makeButtonText(resource: PlanetResources, state: ResourceModelState): String {
        val cropName = if (kitchenModel.kitchenUnlocked) {
            kitchenModel.activeCrops[resource.resourceName]
                ?: resource.resourceName.replaceFirstChar { it.uppercaseChar() }
        } else {
            resource.resourceName.replaceFirstChar { it.uppercaseChar() }
        }
        val owned = noDecFormat.format(state.owned)
        val cost  = formatShort(state.cost)
        return "$cropName ($owned)\n$cost gold"
    }

    private fun makeTooltipText(resource: PlanetResources, state: ResourceModelState): String {
        val payout   = formatShort(state.payout)
        val duration = "%.1f".format(state.cycleDuration.toFloat())
        return "Payout: $payout\nCycle: ${duration}s"
    }

    // ── Skin / style helpers ──────────────────────────────────────────────
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
        private val log = logger<FarmView>()
        private val PLANETARY_EXPONENT = BigDecimal(308)
        @Suppress("unused")
        private val PLANETARY_SCORE = BigDecimal(1e308)

        private val TEN       = BigDecimal(10)
        private val GOLD_1M   = BigDecimal(1_000_000L)
        private val GOLD_1B   = BigDecimal(1_000_000_000L)
        private val GOLD_1T   = BigDecimal(1_000_000_000_000L)
        private val GOLD_1Q   = BigDecimal(1_000_000_000_000_000L)
        private val GOLD_1E33  = BigDecimal("1e33")
        private val GOLD_1E50  = BigDecimal("1e50")
        private val GOLD_1E75  = BigDecimal("1e75")
        private val GOLD_1E100 = BigDecimal("1e100")
        private val GOLD_1E150 = BigDecimal("1e150")
        private val GOLD_1E200 = BigDecimal("1e200")
        private val GOLD_1E308 = BigDecimal("1e308")
    }
}

@Scene2dDsl
fun <S> KWidget<S>.farmView(
    model: FarmModel,
    kitchenModel: KitchenViewModel,
    stage: Stage,
    goldLabel: Label,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: FarmView.(S) -> Unit = {},
): FarmView = actor(FarmView(model, kitchenModel, stage, goldLabel, skin), init)
