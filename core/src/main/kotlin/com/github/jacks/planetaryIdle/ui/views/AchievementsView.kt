package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.get
import com.github.jacks.planetaryIdle.ui.models.AchievementsModel
import ktx.actors.txt
import ktx.preferences.get
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

    private val preferences : Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }

    // labels
    private lateinit var achCompletedCountLabel : Label

    private lateinit var ach1LabelLocked : Label
    private lateinit var ach1LabelCompleted : Label
    private lateinit var ach2LabelLocked : Label
    private lateinit var ach2LabelCompleted : Label

    init {
        setFillParent(true)
        stage = getStage()

        table { achievementsTableCell ->
            table { tableCell ->
                this@AchievementsView.achCompletedCountLabel = label(
                    "Achievements Completed: ${this@AchievementsView.preferences["achCount", 0]} / 24",
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

            //scrollPane { scrollPaneCell ->
            //    setScrollingDisabled(true, false)
            //    setOverscroll(false, false)
                table { innerTableCell ->
                    stack { stackCell ->
                        this@AchievementsView.ach1LabelLocked = label("Achievement 1", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = true
                        }
                        this@AchievementsView.ach1LabelCompleted = label("Achievement 1", Labels.ACH_COMPLETED_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = false
                        }
                        stackCell.top().width(450f).height(40f).pad(3f)
                    }
                    row()
                    stack { stackCell ->
                        this@AchievementsView.ach2LabelLocked = label("Achievement 2", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = true
                        }
                        this@AchievementsView.ach2LabelCompleted = label("Achievement 2", Labels.ACH_COMPLETED_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = false
                        }
                        stackCell.top().width(450f).height(40f).pad(3f)
                    }
                    row()
                    innerTableCell.fill().expand()
                }
             //   scrollPaneCell.fill().expand()
           // }
            achievementsTableCell.expand().fill().pad(5f, 0f, 5f, 0f)
        }

        // Data Binding
        model.onPropertyChange(AchievementsModel::achCount) { count -> updateCount(count) }

        model.onPropertyChange(AchievementsModel::ach1) { ach -> updateAch("ach1") }
        model.onPropertyChange(AchievementsModel::ach2) { ach -> updateAch("ach2") }
    }

    private fun updateCount(count : Int) {
        stage.actors.filterIsInstance<NotificationView>().first().isVisible = false
        achCompletedCountLabel.txt = "Achievements Completed: $count / 24"
    }

    private fun updateAch(ach : String) {
        when(ach) {
            "ach1" -> {
                ach1LabelLocked.isVisible = false
                ach1LabelCompleted.isVisible = true
            }
            "ach2" -> {
                ach2LabelLocked.isVisible = false
                ach2LabelCompleted.isVisible = true
            }
        }
    }

}

@Scene2dDsl
fun <S> KWidget<S>.achievementsView(
    model : AchievementsModel,
    skin : Skin = Scene2DSkin.defaultSkin,
    init : AchievementsView.(S) -> Unit = { }
) : AchievementsView = actor(AchievementsView(model, skin), init)
