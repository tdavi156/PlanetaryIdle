package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.github.jacks.planetaryIdle.components.CropRegistry
import com.github.jacks.planetaryIdle.components.RecipeRegistry
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.Colors
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.get
import com.github.jacks.planetaryIdle.ui.models.CodexModel
import com.github.jacks.planetaryIdle.ui.views.HeaderView.Companion.formatShort
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.image
import ktx.scene2d.scrollPane
import ktx.scene2d.table

private enum class CodexTab { CROPS, RECIPES }

class CodexView(
    private val model: CodexModel,
    private val skin: Skin,
) : Table(skin), KTable {

    private val tabButtons   = mutableMapOf<CodexTab, TextButton>()
    private var currentTab   = CodexTab.CROPS
    private val contentTable = Table(skin)

    init {
        val view = this@CodexView
        setFillParent(true)

        table { outerCell ->

            // ── Tab bar ───────────────────────────────────────────────────────
            table { tabCell ->
                CodexTab.entries.forEach { tab ->
                    val btn = TextButton(tab.label, skin, Buttons.GREY_BUTTON_SMALL.skinKey)
                    btn.isDisabled = (tab == view.currentTab)
                    btn.addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) {
                            view.setTab(tab)
                        }
                    })
                    view.tabButtons[tab] = btn
                    add(btn).height(30f).pad(4f).minWidth(120f)
                }
                tabCell.expandX().left().padLeft(8f).height(40f)
            }

            row()
            image(skin[Drawables.BAR_BLACK_THIN]) { cell ->
                cell.expandX().fillX().height(2f)
            }
            row()

            // ── Scroll pane ───────────────────────────────────────────────────
            scrollPane { scrollCell ->
                setScrollingDisabled(true, false)
                setOverscroll(false, false)
                setActor(view.contentTable)
                scrollCell.fill().expand()
            }
            outerCell.expand().fill().pad(5f, 0f, 5f, 0f)
        }

        // ── Data bindings ─────────────────────────────────────────────────────
        model.onPropertyChange(CodexModel::unlockedCrops)     { rebuildContent() }
        model.onPropertyChange(CodexModel::discoveredRecipes) { rebuildContent() }

        rebuildContent()
    }

    // ── Tab switching ─────────────────────────────────────────────────────────

    private fun setTab(tab: CodexTab) {
        currentTab = tab
        tabButtons.forEach { (t, btn) -> btn.isDisabled = (t == tab) }
        rebuildContent()
    }

    // ── Content rebuild ───────────────────────────────────────────────────────

    private fun rebuildContent() {
        contentTable.clear()
        contentTable.top().left()
        when (currentTab) {
            CodexTab.CROPS   -> buildCropsContent()
            CodexTab.RECIPES -> buildRecipesContent()
        }
        contentTable.invalidateHierarchy()
    }

    // ── Crops tab ─────────────────────────────────────────────────────────────

    private fun buildCropsContent() {
        RecipeRegistry.COLOR_SEQUENCE.forEach { color ->
            val unlocked = model.unlockedCrops[color] ?: emptyList()

            // Color header
            val header = Label(color.replaceFirstChar { it.uppercase() }, skin, Labels.DEFAULT.skinKey)
            header.color = colorEnumFor(color)?.color ?: Color.WHITE
            contentTable.add(header)
                .left().padLeft(12f).padTop(8f).padBottom(4f).colspan(CROP_COLUMNS)
            contentTable.row()

            // One tile per tier
            val crops = CropRegistry.forColor(color)
            crops.forEach { crop ->
                val isDiscovered = crop.cropName in unlocked
                val style = if (isDiscovered) colorLabelStyleFor(color) else Labels.SMALL_GREY_BGD.skinKey
                val text  = if (isDiscovered) crop.cropName else "???"
                val tile  = Label(text, skin, style)
                contentTable.add(tile)
                    .expandX().fillX()
                    .height(TILE_HEIGHT)
                    .pad(2f, 4f, 2f, 4f)
                    .center()
            }
            contentTable.row()

            // Separator
            contentTable.add(Image(skin[Drawables.BAR_BLACK_THIN]))
                .colspan(CROP_COLUMNS).expandX().fillX().height(1f).padTop(4f).padBottom(2f)
            contentTable.row()
        }
    }

    // ── Recipes tab ───────────────────────────────────────────────────────────

    private fun buildRecipesContent() {
        val discovered = model.discoveredRecipes
        val colorSeq   = RecipeRegistry.COLOR_SEQUENCE

        for (i in 0 until colorSeq.size - 1) {
            val c1 = colorSeq[i]
            val c2 = colorSeq[i + 1]

            val allInPair = RecipeRegistry.twoColorRecipes.filter { recipe ->
                val colors = recipe.crops.map { it.color }
                c1 in colors && c2 in colors
            }
            val discoveredInPair  = allInPair.filter { r -> discovered.any { it.id == r.id } }
            val undiscoveredCount = allInPair.size - discoveredInPair.size

            // Section header
            val c1Cap  = c1.replaceFirstChar { it.uppercase() }
            val c2Cap  = c2.replaceFirstChar { it.uppercase() }
            val header = Label("$c1Cap × $c2Cap", skin, Labels.DEFAULT.skinKey)
            header.color = colorEnumFor(c1)?.color ?: Color.WHITE
            contentTable.add(header)
                .left().padLeft(12f).padTop(8f).padBottom(4f).colspan(2)
            contentTable.row()

            // Discovered recipes
            discoveredInPair.forEach { recipe ->
                val payoutStr = "~${formatShort(model.estimatedPayout(recipe))}/cycle"

                val nameLabel = Label(recipe.displayName, skin, Labels.SMALL.skinKey)
                nameLabel.setWrap(true)
                contentTable.add(nameLabel).expandX().fillX().padLeft(16f).padTop(2f)

                val payLabel = Label(payoutStr, skin, Labels.TINY.skinKey)
                payLabel.color = Color.LIGHT_GRAY
                contentTable.add(payLabel).right().padRight(12f).padTop(2f).minWidth(110f)
                contentTable.row()
            }

            // Undiscovered count
            if (undiscoveredCount > 0) {
                val undiscLabel = Label("$undiscoveredCount undiscovered", skin, Labels.TINY.skinKey)
                undiscLabel.color = Color.GRAY
                contentTable.add(undiscLabel).left().padLeft(16f).padBottom(2f).colspan(2)
                contentTable.row()
            }

            // Separator
            contentTable.add(Image(skin[Drawables.BAR_BLACK_THIN]))
                .colspan(2).expandX().fillX().height(1f).padTop(4f).padBottom(2f)
            contentTable.row()
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun colorEnumFor(color: String): Colors? = when (color) {
        "red"    -> Colors.RED
        "orange" -> Colors.ORANGE
        "yellow" -> Colors.YELLOW
        "green"  -> Colors.GREEN
        "blue"   -> Colors.BLUE
        "purple" -> Colors.PURPLE
        "pink"   -> Colors.PINK
        "brown"  -> Colors.BROWN
        "white"  -> Colors.WHITE
        "black"  -> Colors.BLACK
        else     -> null
    }

    private fun colorLabelStyleFor(color: String): String = when (color) {
        "red"    -> Labels.SMALL_RED_BGD.skinKey
        "orange" -> Labels.SMALL_ORANGE_BGD.skinKey
        "yellow" -> Labels.SMALL_YELLOW_BGD.skinKey
        "green"  -> Labels.SMALL_GREEN_BGD.skinKey
        "blue"   -> Labels.SMALL_BLUE_BGD.skinKey
        "purple" -> Labels.SMALL_PURPLE_BGD.skinKey
        "pink"   -> Labels.SMALL_PINK_BGD.skinKey
        "brown"  -> Labels.SMALL_BROWN_BGD.skinKey
        "white"  -> Labels.SMALL_WHITE_BGD.skinKey
        "black"  -> Labels.SMALL_BLACK_BGD.skinKey
        else     -> Labels.SMALL.skinKey
    }

    // ── Companion ─────────────────────────────────────────────────────────────

    companion object {
        private const val TILE_HEIGHT  = 28f
        private const val CROP_COLUMNS = 5
    }
}

// ── Tab label extension ───────────────────────────────────────────────────────

private val CodexTab.label: String get() = when (this) {
    CodexTab.CROPS   -> "Crops"
    CodexTab.RECIPES -> "Recipes"
}

// ── DSL extension ─────────────────────────────────────────────────────────────

@Scene2dDsl
fun <S> KWidget<S>.codexView(
    model: CodexModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: CodexView.(S) -> Unit = {},
): CodexView = actor(CodexView(model, skin), init)
