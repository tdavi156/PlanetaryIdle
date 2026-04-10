package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.github.jacks.planetaryIdle.components.Discovery
import com.github.jacks.planetaryIdle.components.DiscoveryCategory
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.models.DiscoveryViewState
import com.github.jacks.planetaryIdle.ui.models.ObservatoryViewModel
import com.github.jacks.planetaryIdle.ui.views.HeaderView.Companion.formatShort
import ktx.actors.txt
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.label
import ktx.scene2d.table

/** Color tints for each Discovery category header label. */
private val CATEGORY_COLORS: Map<DiscoveryCategory, Color> = mapOf(
    DiscoveryCategory.COMMON    to Color(0.7f, 0.7f, 0.7f, 1f),
    DiscoveryCategory.TREASURED to Color(0.4f, 0.85f, 0.4f, 1f),
    DiscoveryCategory.LEGENDARY to Color(0.4f, 0.6f, 1f, 1f),
    DiscoveryCategory.FABLED    to Color(0.8f, 0.4f, 1f, 1f),
    DiscoveryCategory.MYTHICAL  to Color(1f, 0.7f, 0.2f, 1f),
)

class ObservatoryView(
    private val viewModel: ObservatoryViewModel,
    private val skin: Skin,
) : Table(skin), KTable {

    private lateinit var insightLabel: Label
    private lateinit var insightRateLabel: Label

    private val discoveryButtons     = mutableMapOf<String, TextButton>()
    private val discoveryNameLabels  = mutableMapOf<String, Label>()

    /** Built once; scroll pane wraps this. */
    private val listTable = Table(skin)

    init {
        setFillParent(true)
        val view = this@ObservatoryView

        table { outerCell ->

            // ── Insight header ─────────────────────────────────────────────
            table { headerCell ->
                view.insightLabel = label("Insight: 0", Labels.DEFAULT.skinKey) { cell ->
                    cell.pad(4f, 8f, 0f, 8f)
                }
                view.insightLabel.color = Color(0.9f, 0.85f, 0.4f, 1f)
                row()
                view.insightRateLabel = label("+0 / sec", Labels.SMALL.skinKey) { cell ->
                    cell.pad(0f, 8f, 6f, 8f)
                }
                view.insightRateLabel.color = Color(0.7f, 0.65f, 0.3f, 1f)
                headerCell.expandX().fillX()
            }
            row()

            // ── Scrollable discovery list ──────────────────────────────────
            val scrollPane = com.badlogic.gdx.scenes.scene2d.ui.ScrollPane(view.listTable, skin)
            scrollPane.setScrollingDisabled(true, false)
            scrollPane.setOverscroll(false, false)
            scrollPane.fadeScrollBars = true
            add(scrollPane).expand().fill()

            outerCell.expand().fill()
        }

        // Build all discovery rows into listTable
        buildDiscoveryRows()

        // ── Bind to ViewModel property changes ─────────────────────────────
        viewModel.onPropertyChange(ObservatoryViewModel::insight) { value ->
            insightLabel.txt = "Insight: ${formatShort(value)}"
            refreshAffordability(viewModel.discoveryStates)
        }
        viewModel.onPropertyChange(ObservatoryViewModel::insightPerSecond) { value ->
            insightRateLabel.txt = "+${formatShort(value)} / sec"
        }
        viewModel.onPropertyChange(ObservatoryViewModel::discoveryStates) { states ->
            refreshDiscoveryStates(states)
        }

        // Seed initial state
        refreshDiscoveryStates(viewModel.discoveryStates)
        insightLabel.txt = "Insight: ${formatShort(viewModel.insight)}"
        insightRateLabel.txt = "+${formatShort(viewModel.insightPerSecond)} / sec"
    }

    // ── Build discovery rows ──────────────────────────────────────────────────

    private fun buildDiscoveryRows() {
        listTable.top().padBottom(8f)

        DiscoveryCategory.entries.forEach { category ->
            val discoveries = Discovery.byCategory(category)
            val catColor = CATEGORY_COLORS[category] ?: Color.WHITE

            // Category separator label
            val catLabel = Label(category.displayName.uppercase(), skin, Labels.DEFAULT.skinKey)
            catLabel.color = catColor
            listTable.add(catLabel).left().padLeft(12f).padTop(10f).padBottom(4f).expandX().fillX()
            listTable.row()

            discoveries.forEach { discovery ->
                addDiscoveryRow(discovery)
            }
        }
    }

    private fun addDiscoveryRow(discovery: Discovery) {
        val rowTable = Table(skin)
        rowTable.pad(2f, 4f, 2f, 4f)

        // Left: name + description
        val descTable = Table(skin)
        val nameLabel = Label(discovery.displayName, skin, Labels.DEFAULT.skinKey)
        discoveryNameLabels[discovery.discoveryId] = nameLabel
        descTable.add(nameLabel).left().padLeft(8f).padTop(2f).expandX().fillX()
        descTable.row()
        val descLabel = Label(discovery.description, skin, Labels.SMALL.skinKey)
        descLabel.color = Color.LIGHT_GRAY
        descLabel.setWrap(true)
        descTable.add(descLabel).left().padLeft(8f).padTop(0f).padBottom(2f).width(560f)
        rowTable.add(descTable).expandX().fillX().left()

        // Right: cost + buy button
        val btnTable = Table(skin)
        val costLabel = Label(formatShort(discovery.insightCost) + " Insight", skin, Labels.SMALL.skinKey)
        costLabel.color = Color(0.9f, 0.85f, 0.4f, 1f)
        btnTable.add(costLabel).right().padRight(8f).padTop(4f)
        btnTable.row()
        val btn = TextButton("Research", skin, Buttons.GREY_BUTTON_SMALL.skinKey)
        btn.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                viewModel.purchase(discovery)
            }
        })
        discoveryButtons[discovery.discoveryId] = btn
        btnTable.add(btn).right().padRight(8f).padBottom(4f).width(100f).height(30f)
        rowTable.add(btnTable).right().width(140f)

        listTable.add(rowTable).expandX().fillX()
        listTable.row()
    }

    // ── Refresh helpers ───────────────────────────────────────────────────────

    private fun refreshDiscoveryStates(states: List<DiscoveryViewState>) {
        states.forEach { state ->
            val btn = discoveryButtons[state.discovery.discoveryId] ?: return@forEach
            val nameLabel = discoveryNameLabels[state.discovery.discoveryId]
            when {
                state.isPurchased -> {
                    btn.isDisabled = true
                    btn.setText("Researched")
                    nameLabel?.color = Color(0.5f, 0.9f, 0.5f, 1f)
                }
                !state.isCategoryUnlocked -> {
                    btn.isDisabled = true
                    btn.setText("Locked")
                    nameLabel?.color = Color(0.45f, 0.45f, 0.45f, 1f)
                }
                !state.canAfford -> {
                    btn.isDisabled = true
                    btn.setText("Research")
                    nameLabel?.color = Color.WHITE
                }
                else -> {
                    btn.isDisabled = false
                    btn.setText("Research")
                    nameLabel?.color = Color.WHITE
                }
            }
        }
    }

    private fun refreshAffordability(states: List<DiscoveryViewState>) {
        states.forEach { state ->
            if (state.isPurchased || !state.isCategoryUnlocked) return@forEach
            val btn = discoveryButtons[state.discovery.discoveryId] ?: return@forEach
            btn.isDisabled = !state.canAfford
        }
    }
}

@Scene2dDsl
fun <S> KWidget<S>.observatoryView(
    viewModel: ObservatoryViewModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: ObservatoryView.(S) -> Unit = {},
): ObservatoryView = actor(ObservatoryView(viewModel, skin), init)
