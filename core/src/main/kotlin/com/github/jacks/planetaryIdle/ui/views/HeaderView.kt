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

        /** Toggled by SettingsModel when the player saves a notation preference change. */
        var useLetterNotation: Boolean = true

        private val THOUSAND     = BigDecimal(1_000L)
        private val LETTER_THRESHOLDS: List<Pair<BigDecimal, String>> = listOf(
            BigDecimal("1e3")  to "K",
            BigDecimal("1e6")  to "M",
            BigDecimal("1e9")  to "B",
            BigDecimal("1e12") to "T",
            BigDecimal("1e15") to "Qa",
            BigDecimal("1e18") to "Qi",
            BigDecimal("1e21") to "Sx",
            BigDecimal("1e24") to "Sp",
            BigDecimal("1e27") to "Oc",
            BigDecimal("1e30") to "No",
            BigDecimal("1e33") to "Dc",
            BigDecimal("1e36") to "Ud",
            BigDecimal("1e39") to "Dd",
            BigDecimal("1e42") to "Td",
            BigDecimal("1e45") to "Qad",
            BigDecimal("1e48") to "Qid",
            BigDecimal("1e51") to "Sxd",
        )

        fun formatGold(amount: BigDecimal): String = "${formatShort(amount)} Gold"

        fun formatShort(number: BigDecimal): String =
            if (useLetterNotation) formatLetter(number) else formatScientific(number)

        private fun formatLetter(number: BigDecimal): String {
            val entry = LETTER_THRESHOLDS.lastOrNull { number >= it.first }
                ?: return twoDecFormat.format(number)
            return twoDecFormat.format(number.divide(entry.first, 6, RoundingMode.HALF_UP)) + " " + entry.second
        }

        private fun formatScientific(number: BigDecimal): String =
            if (number < THOUSAND) twoDecFormat.format(number)
            else "%.2e".format(number.toDouble())
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
