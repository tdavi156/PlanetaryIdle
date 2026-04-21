package com.github.jacks.planetaryIdle.ui.views

import ch.obermuhlner.math.big.BigDecimalMath
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.github.jacks.planetaryIdle.events.ViewStateChangeEvent
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.ViewState
import com.github.jacks.planetaryIdle.ui.get
import com.github.jacks.planetaryIdle.ui.models.FarmModel
import com.github.jacks.planetaryIdle.ui.views.HeaderView.Companion.formatShort
import ktx.actors.txt
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.stack
import ktx.scene2d.table
import java.math.BigDecimal
import java.math.RoundingMode

class FooterView(
    model: FarmModel,
    stage: Stage,
    skin: Skin,
) : Table(skin), KTable {

    private lateinit var productionRateLabel: Label
    private lateinit var colonizationProgress: Image
    private lateinit var productionRateProgressLabel: Label

    init {
        val footer = this@FooterView

        // ── Production rate row ────────────────────────────────────────────
        table { rowCell ->
            footer.productionRateLabel = label(
                "Production: ${formatShort(model.productionRate)}/s",
                Labels.SMALL.skinKey
            ) { cell ->
                cell.expandX().left().padLeft(10f)
            }
            rowCell.expandX().fillX().height(28f)
        }
        row()

        // ── Colonization progress bar ──────────────────────────────────────
        stack { stackCell ->
            image(footer.skin[Drawables.BAR_GREY_THICK])
            footer.colonizationProgress = image(footer.skin[Drawables.BAR_GREEN_THICK]) {
                scaleX = 0f
            }
            footer.productionRateProgressLabel = label("0.00 %", Labels.MEDIUM.skinKey) {
                setAlignment(Align.center)
            }
            stackCell.expandX().fillX().height(30f).pad(0f, 10f, 5f, 10f)
        }

        // ── Observer ───────────────────────────────────────────────────────
        model.onPropertyChange(FarmModel::productionRate) { rate ->
            productionRateLabel.txt = "Production: ${formatShort(rate)}/s"
            updateColonizationBar(rate)
        }

        // Initialise bar with whatever the model already holds
        updateColonizationBar(model.productionRate)

        // Only show the footer while the Farm view is active.
        // Follows the same stage.addListener pattern as MenuView.
        stage.addListener(EventListener { event ->
            if (event is ViewStateChangeEvent) isVisible = event.state == ViewState.FARM
            false
        })
    }

    private fun updateColonizationBar(rate: BigDecimal) {
        val prodMantissa = BigDecimalMath.mantissa(rate)
        val prodExponent = BigDecimalMath.exponent(rate).toBigDecimal()
        val expPercent   = prodExponent.divide(PLANETARY_EXPONENT, 6, RoundingMode.UP)
        val manPercent   = expPercent.multiply(prodMantissa.divide(BigDecimal(10), 10, RoundingMode.HALF_UP))
        val prodPercent  = if (rate > BigDecimal.ONE) (expPercent + manPercent).toFloat() else manPercent.toFloat()
        val clamped      = prodPercent.coerceIn(0f, 1f)
        productionRateProgressLabel.txt = "${"%.2f".format(clamped * 100f)} %"
        colonizationProgress.scaleX = clamped
    }

    companion object {
        private val PLANETARY_EXPONENT = BigDecimal(308)
    }
}

@Scene2dDsl
fun <S> KWidget<S>.footerView(
    model: FarmModel,
    stage: Stage,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: FooterView.(S) -> Unit = {},
): FooterView = actor(FooterView(model, stage, skin), init)
