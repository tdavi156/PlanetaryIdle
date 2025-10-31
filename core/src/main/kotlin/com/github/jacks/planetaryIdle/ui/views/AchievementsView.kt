package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
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
import java.math.BigDecimal

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
    private lateinit var ach3LabelLocked : Label
    private lateinit var ach3LabelCompleted : Label
    private lateinit var ach4LabelLocked : Label
    private lateinit var ach4LabelCompleted : Label
    private lateinit var ach5LabelLocked : Label
    private lateinit var ach5LabelCompleted : Label
    private lateinit var ach6LabelLocked : Label
    private lateinit var ach6LabelCompleted : Label
    private lateinit var ach7LabelLocked : Label
    private lateinit var ach7LabelCompleted : Label
    private lateinit var ach8LabelLocked : Label
    private lateinit var ach8LabelCompleted : Label
    private lateinit var ach9LabelLocked : Label
    private lateinit var ach9LabelCompleted : Label
    private lateinit var ach10LabelLocked : Label
    private lateinit var ach10LabelCompleted : Label
    private lateinit var ach11LabelLocked : Label
    private lateinit var ach11LabelCompleted : Label
    private lateinit var ach12LabelLocked : Label
    private lateinit var ach12LabelCompleted : Label
    private lateinit var ach13LabelLocked : Label
    private lateinit var ach13LabelCompleted : Label
    private lateinit var ach14LabelLocked : Label
    private lateinit var ach14LabelCompleted : Label
    private lateinit var ach15LabelLocked : Label
    private lateinit var ach15LabelCompleted : Label
    private lateinit var ach16LabelLocked : Label
    private lateinit var ach16LabelCompleted : Label
    private lateinit var ach17LabelLocked : Label
    private lateinit var ach17LabelCompleted : Label
    private lateinit var ach18LabelLocked : Label
    private lateinit var ach18LabelCompleted : Label
    private lateinit var ach19LabelLocked : Label
    private lateinit var ach19LabelCompleted : Label
    private lateinit var ach20LabelLocked : Label
    private lateinit var ach20LabelCompleted : Label
    private lateinit var ach21LabelLocked : Label
    private lateinit var ach21LabelCompleted : Label
    private lateinit var ach22LabelLocked : Label
    private lateinit var ach22LabelCompleted : Label

    init {
        setFillParent(true)
        stage = getStage()

        table { achievementsTableCell ->
            table { tableCell ->
                this@AchievementsView.achCompletedCountLabel = label(
                    "Achievements Completed: ${this@AchievementsView.preferences["achCount", 0]} / 22",
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
                        this@AchievementsView.ach1LabelLocked = label("1: ${Achievements.ACH_1.achName}\n${Achievements.ACH_1.achDesc}", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = true
                        }
                        this@AchievementsView.ach1LabelCompleted = label("1: ${Achievements.ACH_1.achName}\n${Achievements.ACH_1.achDesc}", Labels.ACH_COMPLETED_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = false
                        }
                        stackCell.top().width(350f).height(50f).pad(4f)
                    }
                    row()
                    stack { stackCell ->
                        this@AchievementsView.ach2LabelLocked = label("2: ${Achievements.ACH_2.achName}\n${Achievements.ACH_2.achDesc}", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = true
                        }
                        this@AchievementsView.ach2LabelCompleted = label("2: ${Achievements.ACH_2.achName}\n${Achievements.ACH_2.achDesc}", Labels.ACH_COMPLETED_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = false
                        }
                        stackCell.top().width(350f).height(50f).pad(4f)
                    }
                    row()
                    stack { stackCell ->
                        this@AchievementsView.ach3LabelLocked = label("3: ${Achievements.ACH_3.achName}\n${Achievements.ACH_3.achDesc}", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = true
                        }
                        this@AchievementsView.ach3LabelCompleted = label("3: ${Achievements.ACH_3.achName}\n${Achievements.ACH_3.achDesc}", Labels.ACH_COMPLETED_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = false
                        }
                        stackCell.top().width(350f).height(50f).pad(4f)
                    }
                    row()
                    stack { stackCell ->
                        this@AchievementsView.ach4LabelLocked = label("4: ${Achievements.ACH_4.achName}\n${Achievements.ACH_4.achDesc}", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = true
                        }
                        this@AchievementsView.ach4LabelCompleted = label("4: ${Achievements.ACH_4.achName}\n${Achievements.ACH_4.achDesc}", Labels.ACH_COMPLETED_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = false
                        }
                        stackCell.top().width(350f).height(50f).pad(4f)
                    }
                    row()
                    stack { stackCell ->
                        this@AchievementsView.ach5LabelLocked = label("5: ${Achievements.ACH_5.achName}\n${Achievements.ACH_5.achDesc}", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = true
                        }
                        this@AchievementsView.ach5LabelCompleted = label("5: ${Achievements.ACH_5.achName}\n${Achievements.ACH_5.achDesc}", Labels.ACH_COMPLETED_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = false
                        }
                        stackCell.top().width(350f).height(50f).pad(4f)
                    }
                    row()
                    stack { stackCell ->
                        this@AchievementsView.ach6LabelLocked = label("6: ${Achievements.ACH_6.achName}\n${Achievements.ACH_6.achDesc}", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = true
                        }
                        this@AchievementsView.ach6LabelCompleted = label("6: ${Achievements.ACH_6.achName}\n${Achievements.ACH_6.achDesc}", Labels.ACH_COMPLETED_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = false
                        }
                        stackCell.top().width(350f).height(50f).pad(4f)
                    }
                    row()
                    stack { stackCell ->
                        this@AchievementsView.ach7LabelLocked = label("7: ${Achievements.ACH_7.achName}\n${Achievements.ACH_7.achDesc}", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = true
                        }
                        this@AchievementsView.ach7LabelCompleted = label("7: ${Achievements.ACH_7.achName}\n${Achievements.ACH_7.achDesc}", Labels.ACH_COMPLETED_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = false
                        }
                        stackCell.top().width(350f).height(50f).pad(4f)
                    }
                    row()
                    stack { stackCell ->
                        this@AchievementsView.ach8LabelLocked = label("8: ${Achievements.ACH_8.achName}\n${Achievements.ACH_8.achDesc}", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = true
                        }
                        this@AchievementsView.ach8LabelCompleted = label("8: ${Achievements.ACH_8.achName}\n${Achievements.ACH_8.achDesc}", Labels.ACH_COMPLETED_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = false
                        }
                        stackCell.top().width(350f).height(50f).pad(4f)
                    }
                    row()
                    stack { stackCell ->
                        this@AchievementsView.ach9LabelLocked = label("9: ${Achievements.ACH_9.achName}\n${Achievements.ACH_9.achDesc}", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = true
                        }
                        this@AchievementsView.ach9LabelCompleted = label("9: ${Achievements.ACH_9.achName}\n${Achievements.ACH_9.achDesc}", Labels.ACH_COMPLETED_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = false
                        }
                        stackCell.top().width(350f).height(50f).pad(4f)
                    }
                    row()
                    stack { stackCell ->
                        this@AchievementsView.ach10LabelLocked = label("10: ${Achievements.ACH_10.achName}\n${Achievements.ACH_10.achDesc}", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = true
                        }
                        this@AchievementsView.ach10LabelCompleted = label("10: ${Achievements.ACH_10.achName}\n${Achievements.ACH_10.achDesc}", Labels.ACH_COMPLETED_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = false
                        }
                        stackCell.top().width(350f).height(50f).pad(4f)
                    }
                    row()
                    stack { stackCell ->
                        this@AchievementsView.ach11LabelLocked = label("11: ${Achievements.ACH_11.achName}\n${Achievements.ACH_11.achDesc}", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = true
                        }
                        this@AchievementsView.ach11LabelCompleted = label("11: ${Achievements.ACH_11.achName}\n${Achievements.ACH_11.achDesc}", Labels.ACH_COMPLETED_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = false
                        }
                        stackCell.top().width(350f).height(50f).pad(4f)
                    }
                    row()
                    stack { stackCell ->
                        this@AchievementsView.ach12LabelLocked = label("12: ${Achievements.ACH_12.achName}\n${Achievements.ACH_12.achDesc}", Labels.SMALL_GREY_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = true
                        }
                        this@AchievementsView.ach12LabelCompleted = label("12: ${Achievements.ACH_12.achName}\n${Achievements.ACH_12.achDesc}", Labels.ACH_COMPLETED_BGD.skinKey) { cell ->
                            this.setAlignment(Align.center)
                            isVisible = false
                        }
                        stackCell.top().width(350f).height(50f).pad(4f)
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
        model.onPropertyChange(AchievementsModel::ach3) { ach -> updateAch("ach3") }
        model.onPropertyChange(AchievementsModel::ach4) { ach -> updateAch("ach4") }
        model.onPropertyChange(AchievementsModel::ach5) { ach -> updateAch("ach5") }
        model.onPropertyChange(AchievementsModel::ach6) { ach -> updateAch("ach6") }
        model.onPropertyChange(AchievementsModel::ach7) { ach -> updateAch("ach7") }
        model.onPropertyChange(AchievementsModel::ach8) { ach -> updateAch("ach8") }
        model.onPropertyChange(AchievementsModel::ach9) { ach -> updateAch("ach9") }
        model.onPropertyChange(AchievementsModel::ach10) { ach -> updateAch("ach10") }
        model.onPropertyChange(AchievementsModel::ach11) { ach -> updateAch("ach11") }
        model.onPropertyChange(AchievementsModel::ach12) { ach -> updateAch("ach12") }
        model.onPropertyChange(AchievementsModel::ach13) { ach -> updateAch("ach13") }
        model.onPropertyChange(AchievementsModel::ach14) { ach -> updateAch("ach14") }
        model.onPropertyChange(AchievementsModel::ach15) { ach -> updateAch("ach15") }
        model.onPropertyChange(AchievementsModel::ach16) { ach -> updateAch("ach16") }
        model.onPropertyChange(AchievementsModel::ach17) { ach -> updateAch("ach17") }
        model.onPropertyChange(AchievementsModel::ach18) { ach -> updateAch("ach18") }
        model.onPropertyChange(AchievementsModel::ach19) { ach -> updateAch("ach19") }
        model.onPropertyChange(AchievementsModel::ach20) { ach -> updateAch("ach20") }
        model.onPropertyChange(AchievementsModel::ach21) { ach -> updateAch("ach21") }
        model.onPropertyChange(AchievementsModel::ach22) { ach -> updateAch("ach22") }
    }

    private fun updateCount(count : Int) {
        achCompletedCountLabel.txt = "Achievements Completed: $count / 22"
    }

    private fun updateAch(ach : String) {
        log.debug { "AchievementsView: updateAch .. achId: $ach" }
        when(ach) {
            "ach1" -> {
                ach1LabelLocked.isVisible = false
                ach1LabelCompleted.isVisible = true
            }
            "ach2" -> {
                ach2LabelLocked.isVisible = false
                ach2LabelCompleted.isVisible = true
            }
            "ach3" -> {
                ach3LabelLocked.isVisible = false
                ach3LabelCompleted.isVisible = true
            }
            "ach4" -> {
                ach4LabelLocked.isVisible = false
                ach4LabelCompleted.isVisible = true
            }
            "ach5" -> {
                ach5LabelLocked.isVisible = false
                ach5LabelCompleted.isVisible = true
            }
            "ach6" -> {
                ach6LabelLocked.isVisible = false
                ach6LabelCompleted.isVisible = true
            }
            "ach7" -> {
                ach7LabelLocked.isVisible = false
                ach7LabelCompleted.isVisible = true
            }
            "ach8" -> {
                ach8LabelLocked.isVisible = false
                ach8LabelCompleted.isVisible = true
            }
            "ach9" -> {
                ach9LabelLocked.isVisible = false
                ach9LabelCompleted.isVisible = true
            }
            "ach10" -> {
                ach10LabelLocked.isVisible = false
                ach10LabelCompleted.isVisible = true
            }
            "ach11" -> {
                ach11LabelLocked.isVisible = false
                ach11LabelCompleted.isVisible = true
            }
            "ach12" -> {
                ach12LabelLocked.isVisible = false
                ach12LabelCompleted.isVisible = true
            }
        }
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
