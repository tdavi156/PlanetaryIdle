package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.github.jacks.planetaryIdle.components.PlanetResources
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.get
import com.github.jacks.planetaryIdle.ui.models.AutomationModel
import com.github.jacks.planetaryIdle.ui.models.KitchenViewModel
import ktx.actors.txt
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.scrollPane
import ktx.scene2d.table
import ktx.scene2d.textButton

class AutomationView(
    private val model: AutomationModel,
    private val kitchenViewModel: KitchenViewModel,
    skin: Skin,
) : Table(skin), KTable {

    // Crop section toggles and threshold labels — rebuilt when smartBuyUnlocked changes
    private var cropSection: Table? = null

    // Soil section — shown/hidden based on soilAutoUnlocked
    private lateinit var soilSection: Table

    // Kitchen section — shown/hidden based on kitchenAutoUnlocked
    private lateinit var kitchenSection: Table

    // Container that holds all content; re-built when unlock states change
    private lateinit var contentTable: Table

    init {
        setFillParent(true)

        scrollPane { spCell ->
            this@AutomationView.contentTable = table { _ ->
                top().left().pad(10f)
            }
            setScrollingDisabled(true, false)
            spCell.expand().fill()
        }

        buildContent()
        bindModelProperties()
    }

    // ── Build / rebuild ───────────────────────────────────────────────────────

    private fun buildContent() {
        contentTable.clear()

        // ── Header ────────────────────────────────────────────────────────────
        contentTable.add(
            Label("Automation", skin, Labels.LARGE.skinKey)
        ).left().padBottom(8f)
        contentTable.row()

        // ── Crop section ──────────────────────────────────────────────────────
        contentTable.add(buildSectionHeader("Crop Auto-Buy")).expandX().fillX().padTop(4f)
        contentTable.row()

        val crops = Table(skin)
        buildCropRows(crops)
        cropSection = crops
        contentTable.add(crops).expandX().fillX().padLeft(8f)
        contentTable.row()

        // ── Soil section ──────────────────────────────────────────────────────
        soilSection = Table(skin)
        buildSoilSection(soilSection)
        soilSection.isVisible = model.soilAutoUnlocked
        contentTable.add(soilSection).expandX().fillX().padTop(8f)
        contentTable.row()

        // ── Kitchen section ───────────────────────────────────────────────────
        kitchenSection = Table(skin)
        buildKitchenSection(kitchenSection)
        kitchenSection.isVisible = model.kitchenAutoUnlocked
        contentTable.add(kitchenSection).expandX().fillX().padTop(8f)
        contentTable.row()

        contentTable.add().expandY()
    }

    // ── Crop rows ─────────────────────────────────────────────────────────────

    private fun buildCropRows(container: Table) {
        container.clear()
        PlanetResources.entries.forEach { res ->
            val color = res.resourceName
            buildCropRow(container, color)
        }
    }

    private fun buildCropRow(container: Table, color: String) {
        val row = Table(skin)

        // Color label
        val colorLabel = Label(color.replaceFirstChar { it.uppercase() }, skin, Labels.SMALL.skinKey)
        colorLabel.color = colorForResource(color)
        row.add(colorLabel).width(80f).left().padLeft(4f)

        // Toggle button
        val enabled = model.cropAutoEnabled[color] ?: false
        val toggleBtn = TextButton(if (enabled) "ON" else "OFF", skin, Buttons.GREY_BUTTON_SMALL.skinKey)
        toggleBtn.color = if (enabled) Color.GREEN else Color.LIGHT_GRAY
        toggleBtn.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                model.toggleCropAuto(color)
            }
        })
        row.add(toggleBtn).width(60f).height(30f).padLeft(8f)

        // Smart buy threshold controls (visible when smartBuyUnlocked)
        val thresholdGroup = Table(skin)
        buildThresholdControls(thresholdGroup, color)
        thresholdGroup.isVisible = model.smartBuyUnlocked
        row.add(thresholdGroup).padLeft(12f)

        container.add(row).expandX().fillX().padBottom(3f)
        container.row()
    }

    private fun buildThresholdControls(container: Table, color: String) {
        val threshold = model.cropGoldThreshold[color] ?: 1.0f
        val pctLabel = Label("${(threshold * 100).toInt()}%", skin, Labels.SMALL.skinKey)

        val minusBtn = TextButton("-10%", skin, Buttons.GREY_BUTTON_SMALL.skinKey)
        minusBtn.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                val cur = model.cropGoldThreshold[color] ?: 1.0f
                model.setGoldThreshold(color, cur - 0.10f)
            }
        })

        val plusBtn = TextButton("+10%", skin, Buttons.GREY_BUTTON_SMALL.skinKey)
        plusBtn.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                val cur = model.cropGoldThreshold[color] ?: 1.0f
                model.setGoldThreshold(color, cur + 0.10f)
            }
        })

        container.add(Label("Max Gold %:", skin, Labels.TINY.skinKey)).padRight(4f)
        container.add(minusBtn).width(60f).height(28f)
        container.add(pctLabel).width(44f).center()
        container.add(plusBtn).width(60f).height(28f)
    }

    // ── Soil section ──────────────────────────────────────────────────────────

    private fun buildSoilSection(container: Table) {
        container.clear()
        container.add(buildSectionHeader("Soil Auto-Buy")).expandX().fillX()
        container.row()

        val row = Table(skin)
        row.add(Label("Auto Soil Upgrade:", skin, Labels.SMALL.skinKey)).left().padLeft(4f)

        val enabled = model.soilAutoEnabled
        val toggleBtn = TextButton(if (enabled) "ON" else "OFF", skin, Buttons.GREY_BUTTON_SMALL.skinKey)
        toggleBtn.color = if (enabled) Color.GREEN else Color.LIGHT_GRAY
        toggleBtn.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                model.toggleSoilAuto()
            }
        })
        row.add(toggleBtn).width(60f).height(30f).padLeft(8f)
        row.add(Label("Automatically buys Soil upgrades when affordable.", skin, Labels.TINY.skinKey))
            .left().padLeft(12f).expandX()

        container.add(row).expandX().fillX().padLeft(8f)
        container.row()
    }

    // ── Kitchen section ───────────────────────────────────────────────────────

    private fun buildKitchenSection(container: Table) {
        container.clear()
        container.add(buildSectionHeader("Kitchen Automation")).expandX().fillX()
        container.row()

        // Per-researcher auto-research toggles
        val researchers = kitchenViewModel.researchers
        if (researchers.isEmpty()) {
            container.add(Label("No researchers hired yet.", skin, Labels.SMALL.skinKey))
                .left().padLeft(12f)
            container.row()
        } else {
            researchers.forEachIndexed { index, researcher ->
                val row = Table(skin)
                row.add(Label("Researcher ${index + 1}:", skin, Labels.SMALL.skinKey)).width(120f).left().padLeft(4f)

                val enabled = researcher.autoResearchEnabled
                val toggleBtn = TextButton(if (enabled) "Auto ON" else "Auto OFF", skin, Buttons.GREY_BUTTON_SMALL.skinKey)
                toggleBtn.color = if (enabled) Color.GREEN else Color.LIGHT_GRAY
                toggleBtn.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        kitchenViewModel.setAutoResearch(index, !(kitchenViewModel.researchers.getOrNull(index)?.autoResearchEnabled ?: false))
                    }
                })
                row.add(toggleBtn).width(90f).height(30f).padLeft(8f)
                container.add(row).expandX().fillX().padLeft(8f).padBottom(3f)
                container.row()
            }
        }

        // Auto-recipe toggle (shown only if autoRecipeUnlocked)
        if (model.autoRecipeUnlocked) {
            val sep = image(skin[Drawables.BAR_BLACK_THIN])
            container.add(sep).expandX().fillX().height(1f).padTop(6f).padBottom(6f)
            container.row()

            val row = Table(skin)
            row.add(Label("Auto Best Recipe:", skin, Labels.SMALL.skinKey)).left().padLeft(4f)

            val recipeEnabled = model.autoRecipeEnabled
            val recipeToggle = TextButton(if (recipeEnabled) "ON" else "OFF", skin, Buttons.GREY_BUTTON_SMALL.skinKey)
            recipeToggle.color = if (recipeEnabled) Color.GREEN else Color.LIGHT_GRAY
            recipeToggle.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    model.toggleRecipeAuto()
                }
            })
            row.add(recipeToggle).width(60f).height(30f).padLeft(8f)
            row.add(Label("Automatically assigns highest-payout non-conflicting recipes.", skin, Labels.TINY.skinKey))
                .left().padLeft(12f).expandX()
            container.add(row).expandX().fillX().padLeft(8f)
            container.row()
        }
    }

    // ── Model bindings ────────────────────────────────────────────────────────

    private fun bindModelProperties() {
        // Rebuild crop section when toggles or thresholds change (or smartBuy unlocks)
        model.onPropertyChange(AutomationModel::cropAutoEnabled) { _ ->
            cropSection?.let { buildCropRows(it) }
        }
        model.onPropertyChange(AutomationModel::cropGoldThreshold) { _ ->
            cropSection?.let { buildCropRows(it) }
        }
        model.onPropertyChange(AutomationModel::smartBuyUnlocked) { _ ->
            buildContent() // rebuild everything to show threshold controls
        }
        model.onPropertyChange(AutomationModel::soilAutoUnlocked) { unlocked ->
            if (::soilSection.isInitialized) soilSection.isVisible = unlocked
        }
        model.onPropertyChange(AutomationModel::soilAutoEnabled) { _ ->
            if (::soilSection.isInitialized) buildSoilSection(soilSection)
        }
        model.onPropertyChange(AutomationModel::kitchenAutoUnlocked) { unlocked ->
            if (::kitchenSection.isInitialized) {
                kitchenSection.isVisible = unlocked
                if (unlocked) buildKitchenSection(kitchenSection)
            }
        }
        model.onPropertyChange(AutomationModel::autoRecipeUnlocked) { _ ->
            if (::kitchenSection.isInitialized) buildKitchenSection(kitchenSection)
        }
        model.onPropertyChange(AutomationModel::autoRecipeEnabled) { _ ->
            if (::kitchenSection.isInitialized) buildKitchenSection(kitchenSection)
        }
        kitchenViewModel.onPropertyChange(KitchenViewModel::researchers) { _ ->
            if (::kitchenSection.isInitialized && model.kitchenAutoUnlocked) {
                buildKitchenSection(kitchenSection)
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildSectionHeader(title: String): Table {
        val t = Table(skin)
        t.background = skin[Drawables.BACKGROUND_GREY]
        t.add(Label(title, skin, Labels.MEDIUM.skinKey)).left().pad(4f, 8f, 4f, 8f)
        t.add().expandX()
        return t
    }

    private fun colorForResource(color: String): Color = when (color) {
        "red"    -> Color.RED
        "orange" -> Color.ORANGE
        "yellow" -> Color.YELLOW
        "green"  -> Color.GREEN
        "blue"   -> Color.BLUE
        "purple" -> Color(0.6f, 0.2f, 0.8f, 1f)
        "pink"   -> Color(1f, 0.6f, 0.8f, 1f)
        "brown"  -> Color(0.6f, 0.4f, 0.2f, 1f)
        "white"  -> Color.WHITE
        "black"  -> Color.LIGHT_GRAY
        else     -> Color.WHITE
    }
}

@Scene2dDsl
fun <S> KWidget<S>.automationView(
    model: AutomationModel,
    kitchenViewModel: KitchenViewModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: AutomationView.(S) -> Unit = {},
): AutomationView = actor(AutomationView(model, kitchenViewModel, skin), init)
