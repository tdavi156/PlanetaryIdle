package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.github.jacks.planetaryIdle.components.Achievements
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

class AchievementsView(
    model : AchievementsModel,
    private val skin : Skin,
) : Table(skin), KTable {

    private lateinit var achCompletedCountLabel : Label
    private val cards = arrayOfNulls<Table>(ACH_COUNT)
    private val gridTable = Table(skin)
    private var lastColumnCount = -1

    init {
        setFillParent(true)

        table { achievementsTableCell ->
            table { tableCell ->
                this@AchievementsView.achCompletedCountLabel = label(
                    "Achievements Completed: 0 / $ACH_COUNT",
                    Labels.WHITE.skinKey
                ) { cell ->
                    cell.center().pad(3f)
                }
                tableCell.expandX().top().height(40f)
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
            cards[ach.achId - 1] = buildCard(ach)
        }

        // Data Binding — fires on each subsequent change
        model.onPropertyChange(AchievementsModel::completedAchievements) { completed ->
            updateCount(completed.size)
            completed.forEach { updateCard(it) }
        }

        // Apply initial state for achievements already completed before this session
        model.completedAchievements.let { completed ->
            updateCount(completed.size)
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

    private fun columnsForWidth(w : Float) : Int = if (w < WIDE_THRESHOLD) 2 else 4

    private fun buildCard(ach : Achievements) : Table {
        val card = Table(skin)
        card.setBackground(skin[Drawables.BUTTON_GREY_UP])

        // Left: dark placeholder square (replace drawable here when adding real art)
        card.add(Image(skin[Drawables.BUTTON_BLACK_UP])).size(IMAGE_SIZE).pad(INNER_PAD)

        // Right: name on top, requirement below
        val textTable = Table(skin)
        val nameLabel = Label(ach.achName, skin, Labels.SMALL.skinKey)
        nameLabel.setWrap(true)
        val descLabel = Label(ach.achDesc, skin, Labels.TINY.skinKey)
        descLabel.setWrap(true)
        textTable.add(nameLabel).expandX().fillX().left()
        textTable.row()
        textTable.add(descLabel).expandX().fillX().left()

        card.add(textTable).expandX().fillX().pad(INNER_PAD).left()

        return card
    }

    private fun rebuildGrid(columns : Int) {
        gridTable.clear()
        gridTable.top().left()

        Achievements.entries.forEachIndexed { index, ach ->
            val card = cards[ach.achId - 1] ?: return@forEachIndexed
            gridTable.add(card).expandX().fillX().height(CARD_HEIGHT).pad(CARD_PAD)
            if ((index + 1) % columns == 0) gridTable.row()
        }

        // Fill out the last row with empty cells so column widths stay uniform
        val remainder = ACH_COUNT % columns
        if (remainder != 0) {
            repeat(columns - remainder) {
                gridTable.add().expandX().fillX().height(CARD_HEIGHT).pad(CARD_PAD)
            }
        }

        gridTable.invalidateHierarchy()
    }

    private fun updateCount(count : Int) {
        achCompletedCountLabel.txt = "Achievements Completed: $count / $ACH_COUNT"
    }

    private fun updateCard(achId : Int) {
        cards[achId - 1]?.setBackground(skin[Drawables.BUTTON_GREEN_DISABLED])
    }

    companion object {
        private val log = logger<AchievementsView>()
        private const val ACH_COUNT = 22
        private const val WIDE_THRESHOLD = 900f
        private const val IMAGE_SIZE = 60f
        private const val CARD_HEIGHT = 80f
        private const val CARD_PAD = 4f
        private const val INNER_PAD = 6f
    }
}

@Scene2dDsl
fun <S> KWidget<S>.achievementsView(
    model : AchievementsModel,
    skin : Skin = Scene2DSkin.defaultSkin,
    init : AchievementsView.(S) -> Unit = { }
) : AchievementsView = actor(AchievementsView(model, skin), init)
