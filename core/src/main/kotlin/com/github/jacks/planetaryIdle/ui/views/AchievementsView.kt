package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
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
import ktx.scene2d.stack
import ktx.scene2d.table

class AchievementsView(
    model : AchievementsModel,
    skin : Skin,
) : Table(skin), KTable {

    private lateinit var achCompletedCountLabel : Label
    private val lockedLabels = arrayOfNulls<Label>(22)
    private val completedLabels = arrayOfNulls<Label>(22)

    init {
        setFillParent(true)
        stage = getStage()

        table { achievementsTableCell ->
            table { tableCell ->
                this@AchievementsView.achCompletedCountLabel = label(
                    "Achievements Completed: 0 / 22",
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
                table { innerTableCell ->
                    Achievements.entries.forEach { ach ->
                        val idx = ach.achId - 1
                        stack { stackCell ->
                            this@AchievementsView.lockedLabels[idx] = label(
                                "${ach.achId}: ${ach.achName}\n${ach.achDesc}",
                                Labels.SMALL_GREY_BGD.skinKey
                            ) {
                                this.setAlignment(Align.center)
                                isVisible = true
                            }
                            this@AchievementsView.completedLabels[idx] = label(
                                "${ach.achId}: ${ach.achName}\n${ach.achDesc}",
                                Labels.ACH_COMPLETED_BGD.skinKey
                            ) {
                                this.setAlignment(Align.center)
                                isVisible = false
                            }
                            stackCell.top().width(350f).height(50f).pad(4f)
                        }
                        row()
                    }
                    innerTableCell.fill().expand()
                }
                scrollPaneCell.fill().expand()
            }
            achievementsTableCell.expand().fill().pad(5f, 0f, 5f, 0f)
        }

        // Data Binding — fires on each subsequent change
        model.onPropertyChange(AchievementsModel::completedAchievements) { completed ->
            updateCount(completed.size)
            completed.forEach { updateAch(it) }
        }

        // Apply initial state for achievements already completed before this session
        model.completedAchievements.let { completed ->
            updateCount(completed.size)
            completed.forEach { updateAch(it) }
        }
    }

    private fun updateCount(count : Int) {
        achCompletedCountLabel.txt = "Achievements Completed: $count / 22"
    }

    private fun updateAch(achId : Int) {
        val idx = achId - 1
        lockedLabels[idx]?.isVisible = false
        completedLabels[idx]?.isVisible = true
    }

    companion object {
        val log = logger<AchievementsView>()
    }
}

@Scene2dDsl
fun <S> KWidget<S>.achievementsView(
    model : AchievementsModel,
    skin : Skin = Scene2DSkin.defaultSkin,
    init : AchievementsView.(S) -> Unit = { }
) : AchievementsView = actor(AchievementsView(model, skin), init)
