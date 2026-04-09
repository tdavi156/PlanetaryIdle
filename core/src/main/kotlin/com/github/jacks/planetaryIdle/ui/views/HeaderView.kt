package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.github.jacks.planetaryIdle.components.Achievements
import com.github.jacks.planetaryIdle.events.ViewStateChangeEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.Colors
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.ViewState
import com.github.jacks.planetaryIdle.ui.models.AchievementsModel
import com.github.jacks.planetaryIdle.ui.models.FarmModel
import ktx.actors.txt
import ktx.log.logger
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.label
import ktx.scene2d.textButton
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class HeaderView(
    model: FarmModel,
    achModel: AchievementsModel,
    private val stage: Stage,
    skin: Skin,
) : Table(skin), KTable {

    val goldLabel: Label
    private val trophyButton: TextButton

    private val totalAchievements = Achievements.entries.size

    init {
        goldLabel = label(formatGold(model.goldCoins), Labels.DEFAULT.skinKey) { cell ->
            cell.expandX().center()
        }
        goldLabel.color = Colors.YELLOW.color

        trophyButton = textButton(
            trophyText(achModel.completedAchievements.size),
            Buttons.GREY_BUTTON_SMALL.skinKey
        ) { cell ->
            cell.right().padRight(8f).height(28f)
            addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    stage.fire(ViewStateChangeEvent(ViewState.ACHIEVEMENTS))
                }
            })
        }

        model.onPropertyChange(FarmModel::goldCoins) { amount ->
            goldLabel.txt = formatGold(amount)
        }

        achModel.onPropertyChange(AchievementsModel::completedAchievements) { completed ->
            trophyButton.txt = trophyText(completed.size)
        }
    }

    private fun trophyText(count: Int) = "Achievements: $count / $totalAchievements"

    companion object {
        private val log = logger<HeaderView>()
        private val twoDecFormat = DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance())

        private val MILLION     = BigDecimal(1_000_000L)
        private val BILLION     = BigDecimal(1_000_000_000L)
        private val TRILLION    = BigDecimal(1_000_000_000_000L)
        private val QUADRILLION = BigDecimal(1_000_000_000_000_000L)

        fun formatGold(amount: BigDecimal): String = "${formatShort(amount)} Gold"

        fun formatShort(number: BigDecimal): String = when {
            number < MILLION     -> twoDecFormat.format(number)
            number < BILLION     -> twoDecFormat.format(number.divide(MILLION, 6, RoundingMode.HALF_UP)) + " M"
            number < TRILLION    -> twoDecFormat.format(number.divide(BILLION, 6, RoundingMode.HALF_UP)) + " B"
            number < QUADRILLION -> twoDecFormat.format(number.divide(TRILLION, 6, RoundingMode.HALF_UP)) + " T"
            else                 -> "%.2eGold".format(number.toDouble())
        }
    }
}

@Scene2dDsl
fun <S> KWidget<S>.headerView(
    model: FarmModel,
    achModel: AchievementsModel,
    stage: Stage,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: HeaderView.(S) -> Unit = {},
): HeaderView = actor(HeaderView(model, achModel, stage, skin), init)
