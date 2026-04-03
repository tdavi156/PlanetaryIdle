package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.github.jacks.planetaryIdle.ui.Colors
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.models.PlanetModel
import ktx.actors.txt
import ktx.log.logger
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.label
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class HeaderView(
    model: PlanetModel,
    skin: Skin,
) : Table(skin), KTable {

    val goldLabel: Label
    private val achMultLabel: Label

    init {
        goldLabel = label(formatGold(model.goldCoins), Labels.DEFAULT.skinKey) { cell ->
            cell.expandX().center()
        }
        goldLabel.color = Colors.YELLOW.color

        achMultLabel = label("x${formatMult(model.achievementMultiplier)}", Labels.SMALL.skinKey) { cell ->
            cell.right().padRight(16f)
        }

        model.onPropertyChange(PlanetModel::goldCoins) { amount ->
            goldLabel.txt = formatGold(amount)
        }

        model.onPropertyChange(PlanetModel::achievementMultiplier) { mult ->
            achMultLabel.txt = "x${formatMult(mult)}"
        }
    }

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

        private fun formatMult(mult: BigDecimal): String =
            twoDecFormat.format(mult)
    }
}

@Scene2dDsl
fun <S> KWidget<S>.headerView(
    model: PlanetModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: HeaderView.(S) -> Unit = {},
): HeaderView = actor(HeaderView(model, skin), init)
