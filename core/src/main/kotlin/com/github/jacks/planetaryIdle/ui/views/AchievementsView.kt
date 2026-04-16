package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.github.jacks.planetaryIdle.components.Achievements
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.Colors
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.get
import com.github.jacks.planetaryIdle.ui.models.AchievementProgress
import com.github.jacks.planetaryIdle.ui.models.AchievementsModel
import ktx.actors.txt
import ktx.log.logger
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.scrollPane
import ktx.scene2d.table
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

private enum class FilterTab { ALL, COMPLETED, IN_PROGRESS, LOCKED }

class AchievementsView(
    private val model: AchievementsModel,
    private val skin: Skin,
) : Table(skin), KTable {

    private lateinit var achBonusLabel: Label
    private lateinit var achCompletedCountLabel: Label

    // One card Table per achievement ID
    private val cards         = mutableMapOf<String, Table>()
    // Progress bar fill image stored per achievement ID (scaleX = fraction)
    private val progressFills = mutableMapOf<String, Image>()
    // Progress text label stored per achievement ID ("12 / 50")
    private val progressTexts = mutableMapOf<String, Label>()

    private val gridTable = Table(skin)
    private var lastColumnCount = -1

    private val totalAchievements = Achievements.entries.size

    // Filter state
    private var currentFilter  = FilterTab.ALL
    private val filterButtons  = mutableMapOf<FilterTab, TextButton>()

    init {
        val view = this@AchievementsView
        setFillParent(true)

        table { achievementsTableCell ->

            // ── Header ────────────────────────────────────────────────────────
            table { tableCell ->
                view.achBonusLabel = label(
                    view.bonusText(view.model.achievementMultiplier),
                    Labels.DEFAULT.skinKey
                ) { cell -> cell.center().pad(2f) }
                view.achBonusLabel.color = Colors.YELLOW.color
                row()
                view.achCompletedCountLabel = label(
                    view.countText(view.model.completedAchievements.size),
                    Labels.SMALL.skinKey
                ) { cell -> cell.center().pad(2f) }
                tableCell.expandX().top().height(52f)
            }

            row()
            image(skin[Drawables.BAR_BLACK_THIN]) { cell ->
                cell.expandX().fillX().height(2f)
            }
            row()

            // ── Filter tabs ───────────────────────────────────────────────────
            table { tabCell ->
                FilterTab.entries.forEach { tab ->
                    val btn = TextButton(tab.label, skin, Buttons.GREY_BUTTON_SMALL.skinKey)
                    btn.isDisabled = (tab == view.currentFilter)
                    btn.addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) {
                            view.setFilter(tab)
                        }
                    })
                    view.filterButtons[tab] = btn
                    add(btn).height(30f).pad(4f, 4f, 4f, 4f).width(100f)
                }
                tabCell.expandX().left().padLeft(8f).height(40f)
            }

            row()
            image(skin[Drawables.BAR_BLACK_THIN]) { cell ->
                cell.expandX().fillX().height(2f)
            }
            row()

            // ── Scroll pane ───────────────────────────────────────────────────
            scrollPane { scrollPaneCell ->
                setScrollingDisabled(true, false)
                setOverscroll(false, false)
                setActor(this@AchievementsView.gridTable)
                scrollPaneCell.fill().expand()
            }
            achievementsTableCell.expand().fill().pad(5f, 0f, 5f, 0f)
        }

        // Build all 57 cards up front
        Achievements.entries.forEach { ach ->
            cards[ach.achId] = buildCard(ach)
        }

        // Apply initial completion state
        model.completedAchievements.let { completed ->
            achCompletedCountLabel.txt = countText(completed.size)
            completed.forEach { markCardComplete(it) }
        }

        // Apply initial progress
        model.progressMap.forEach { (achId, progress) ->
            applyProgress(achId, progress)
        }

        // ── Data bindings ─────────────────────────────────────────────────────
        model.onPropertyChange(AchievementsModel::achievementMultiplier) { mult ->
            achBonusLabel.txt = bonusText(mult)
        }
        model.onPropertyChange(AchievementsModel::completedAchievements) { completed ->
            achCompletedCountLabel.txt = countText(completed.size)
            completed.forEach { markCardComplete(it) }
            // Rebuild in case filter changes which cards are visible
            rebuildGrid(lastColumnCount.coerceAtLeast(1))
        }
        model.onPropertyChange(AchievementsModel::progressMap) { map ->
            map.forEach { (achId, progress) -> applyProgress(achId, progress) }
            // Rebuild grid to re-classify IN_PROGRESS vs LOCKED when filter is active
            if (currentFilter == FilterTab.IN_PROGRESS || currentFilter == FilterTab.LOCKED) {
                rebuildGrid(lastColumnCount.coerceAtLeast(1))
            }
        }
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    override fun layout() {
        super.layout()
        if (width <= 0f) return
        val cols = columnsForWidth(width)
        if (cols != lastColumnCount) {
            lastColumnCount = cols
            rebuildGrid(cols)
        }
    }

    private fun columnsForWidth(w: Float): Int = if (w < WIDE_THRESHOLD) 2 else 4

    // ── Filter ────────────────────────────────────────────────────────────────

    private fun setFilter(tab: FilterTab) {
        currentFilter = tab
        filterButtons.forEach { (t, btn) -> btn.isDisabled = (t == tab) }
        rebuildGrid(lastColumnCount.coerceAtLeast(1))
    }

    private fun visibleAchievements(): List<Achievements> = when (currentFilter) {
        FilterTab.ALL -> Achievements.entries
        FilterTab.COMPLETED -> Achievements.entries.filter {
            it.achId in model.completedAchievements
        }
        FilterTab.IN_PROGRESS -> Achievements.entries.filter {
            it.achId !in model.completedAchievements &&
                (model.progressMap[it.achId]?.fraction ?: 0f) > 0f
        }
        FilterTab.LOCKED -> Achievements.entries.filter {
            it.achId !in model.completedAchievements &&
                (model.progressMap[it.achId]?.fraction ?: 0f) == 0f
        }
    }

    // ── Grid ──────────────────────────────────────────────────────────────────

    private fun rebuildGrid(columns: Int) {
        gridTable.clear()
        gridTable.top().left()

        val visible = visibleAchievements()
        visible.forEachIndexed { index, ach ->
            val card = cards[ach.achId] ?: return@forEachIndexed
            gridTable.add(card).expandX().fillX().height(CARD_HEIGHT).pad(CARD_PAD)
            if ((index + 1) % columns == 0) gridTable.row()
        }

        val remainder = visible.size % columns
        if (remainder != 0) {
            repeat(columns - remainder) {
                gridTable.add().expandX().fillX().height(CARD_HEIGHT).pad(CARD_PAD)
            }
        }

        gridTable.invalidateHierarchy()
    }

    // ── Card construction ─────────────────────────────────────────────────────

    private fun buildCard(ach: Achievements): Table {
        val card = Table(skin)
        card.setBackground(skin[Drawables.BUTTON_GREY_UP])

        // Left: dark icon placeholder
        card.add(Image(skin[Drawables.BUTTON_BLACK_UP])).size(IMAGE_SIZE).pad(INNER_PAD)

        // Right: text content
        val textTable = Table(skin)

        val nameLabel = Label(ach.achName, skin, Labels.SMALL.skinKey)
        nameLabel.setWrap(true)
        textTable.add(nameLabel).expandX().fillX().left()
        textTable.row()

        val descLabel = Label(ach.achDesc, skin, Labels.TINY.skinKey)
        descLabel.setWrap(true)
        textTable.add(descLabel).expandX().fillX().left()

        ach.bonus?.let { bonus ->
            textTable.row()
            val bonusLabel = Label(bonus.bonusDesc, skin, Labels.TINY.skinKey)
            bonusLabel.setWrap(true)
            bonusLabel.color = Colors.YELLOW.color
            textTable.add(bonusLabel).expandX().fillX().left()
        }

        // Progress text (e.g. "12 / 50")
        textTable.row()
        val progressText = Label("", skin, Labels.TINY.skinKey)
        progressText.color = Color.LIGHT_GRAY
        textTable.add(progressText).expandX().fillX().left().padTop(2f)
        progressTexts[ach.achId] = progressText

        card.add(textTable).expandX().fillX().pad(INNER_PAD).left()

        // Full-width progress bar spanning both columns
        card.row()
        val barStack = Stack()
        val barBg   = Image(skin[Drawables.BAR_GREY_THICK])
        val barFill = Image(skin[Drawables.BAR_GREEN_THICK]).also { it.scaleX = 0f }
        barStack.add(barBg)
        barStack.add(barFill)
        card.add(barStack).colspan(2).expandX().fillX().height(BAR_HEIGHT).pad(0f, INNER_PAD, INNER_PAD, INNER_PAD).prefWidth(0f)
        progressFills[ach.achId] = barFill

        return card
    }

    // ── Card updates ──────────────────────────────────────────────────────────

    private fun markCardComplete(achId: String) {
        cards[achId]?.setBackground(skin[Drawables.BUTTON_GREEN_DISABLED])
    }

    private fun applyProgress(achId: String, progress: AchievementProgress) {
        progressFills[achId]?.scaleX = progress.fraction
        progressTexts[achId]?.txt    = progress.text
    }

    // ── Text helpers ──────────────────────────────────────────────────────────

    private fun bonusText(mult: BigDecimal): String =
        "Achievement Bonus: x${multFormat.format(mult)}"

    private fun countText(count: Int): String =
        "$count / $totalAchievements Completed"

    // ── Companion ─────────────────────────────────────────────────────────────

    companion object {
        private val log = logger<AchievementsView>()
        private val multFormat = DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance())
        private const val WIDE_THRESHOLD = 900f
        private const val IMAGE_SIZE     = 60f
        private const val CARD_HEIGHT    = 115f
        private const val CARD_PAD       = 4f
        private const val INNER_PAD      = 6f
        private const val BAR_HEIGHT     = 8f
    }
}

// ── Filter label extension ────────────────────────────────────────────────────

private val FilterTab.label: String get() = when (this) {
    FilterTab.ALL         -> "All"
    FilterTab.COMPLETED   -> "Completed"
    FilterTab.IN_PROGRESS -> "In Progress"
    FilterTab.LOCKED      -> "Locked"
}

// ── DSL extension ─────────────────────────────────────────────────────────────

@Scene2dDsl
fun <S> KWidget<S>.achievementsView(
    model: AchievementsModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: AchievementsView.(S) -> Unit = {},
): AchievementsView = actor(AchievementsView(model, skin), init)
