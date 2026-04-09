package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.github.jacks.planetaryIdle.components.AchievementBonus
import com.github.jacks.planetaryIdle.components.Achievements
import com.github.jacks.planetaryIdle.ui.Colors
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.get
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

class AchievementsView(
    model: AchievementsModel,
    private val skin: Skin,
) : Table(skin), KTable {

    private lateinit var achBonusLabel: Label
    private lateinit var achCompletedCountLabel: Label
    private val cards = mutableMapOf<String, Table>()
    private val gridTable = Table(skin)
    private var lastColumnCount = -1

    private val totalAchievements = Achievements.entries.size

    init {
        setFillParent(true)

        table { achievementsTableCell ->
            // ── Header ────────────────────────────────────────────────────────
            table { tableCell ->
                this@AchievementsView.achBonusLabel = label(
                    this@AchievementsView.bonusText(model.achievementMultiplier),
                    Labels.DEFAULT.skinKey
                ) { cell ->
                    cell.center().pad(2f)
                }
                this@AchievementsView.achBonusLabel.color = Colors.YELLOW.color

                row()

                this@AchievementsView.achCompletedCountLabel = label(
                    this@AchievementsView.countText(model.completedAchievements.size),
                    Labels.SMALL.skinKey
                ) { cell ->
                    cell.center().pad(2f)
                }

                tableCell.expandX().top().height(52f)
            }

            row()
            image(skin[Drawables.BAR_BLACK_THIN]) { cell ->
                cell.expandX().fillX().height(2f)
            }
            row()

            scrollPane { scrollPaneCell ->
                setScrollingDisabled(true, false)
                setOverscroll(false, false)
                setActor(this@AchievementsView.gridTable)
                scrollPaneCell.fill().expand()
            }
            achievementsTableCell.expand().fill().pad(5f, 0f, 5f, 0f)
        }

        Achievements.entries.forEach { ach ->
            cards[ach.achId] = buildCard(ach)
        }

        // Data Binding
        model.onPropertyChange(AchievementsModel::achievementMultiplier) { mult ->
            achBonusLabel.txt = bonusText(mult)
        }
        model.onPropertyChange(AchievementsModel::completedAchievements) { completed ->
            achCompletedCountLabel.txt = countText(completed.size)
            completed.forEach { updateCard(it) }
        }

        // Apply initial state for achievements already completed before this session
        model.completedAchievements.let { completed ->
            achCompletedCountLabel.txt = countText(completed.size)
            completed.forEach { updateCard(it) }
        }
    }

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

    private fun buildCard(ach: Achievements): Table {
        val card = Table(skin)
        card.setBackground(skin[Drawables.BUTTON_GREY_UP])

        // Left: dark placeholder square
        card.add(Image(skin[Drawables.BUTTON_BLACK_UP])).size(IMAGE_SIZE).pad(INNER_PAD)

        // Right: name, requirement, and optional bonus description
        val textTable = Table(skin)
        val nameLabel = Label(ach.achName, skin, Labels.SMALL.skinKey)
        nameLabel.setWrap(true)
        val descLabel = Label(ach.achDesc, skin, Labels.TINY.skinKey)
        descLabel.setWrap(true)

        textTable.add(nameLabel).expandX().fillX().left()
        textTable.row()
        textTable.add(descLabel).expandX().fillX().left()

        ach.bonus?.let { bonus ->
            textTable.row()
            val bonusLabel = Label(bonus.bonusDesc, skin, Labels.TINY.skinKey)
            bonusLabel.setWrap(true)
            bonusLabel.color = Colors.YELLOW.color
            textTable.add(bonusLabel).expandX().fillX().left()
        }

        card.add(textTable).expandX().fillX().pad(INNER_PAD).left()
        return card
    }

    private fun rebuildGrid(columns: Int) {
        gridTable.clear()
        gridTable.top().left()

        Achievements.entries.forEachIndexed { index, ach ->
            val card = cards[ach.achId] ?: return@forEachIndexed
            gridTable.add(card).expandX().fillX().height(CARD_HEIGHT).pad(CARD_PAD)
            if ((index + 1) % columns == 0) gridTable.row()
        }

        // Fill out the last row so column widths stay uniform
        val remainder = totalAchievements % columns
        if (remainder != 0) {
            repeat(columns - remainder) {
                gridTable.add().expandX().fillX().height(CARD_HEIGHT).pad(CARD_PAD)
            }
        }

        gridTable.invalidateHierarchy()
    }

    private fun updateCard(achId: String) {
        cards[achId]?.setBackground(skin[Drawables.BUTTON_GREEN_DISABLED])
    }

    private fun bonusText(mult: BigDecimal): String =
        "Achievement Bonus: x${multFormat.format(mult)}"

    private fun countText(count: Int): String =
        "$count / $totalAchievements Completed"

    companion object {
        private val log = logger<AchievementsView>()
        private val multFormat = DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance())
        private const val WIDE_THRESHOLD = 900f
        private const val IMAGE_SIZE = 60f
        private const val CARD_HEIGHT = 90f
        private const val CARD_PAD = 4f
        private const val INNER_PAD = 6f
    }
}

@Scene2dDsl
fun <S> KWidget<S>.achievementsView(
    model: AchievementsModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: AchievementsView.(S) -> Unit = {},
): AchievementsView = actor(AchievementsView(model, skin), init)
