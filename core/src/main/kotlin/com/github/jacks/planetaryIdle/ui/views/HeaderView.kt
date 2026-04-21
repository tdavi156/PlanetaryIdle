package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.github.jacks.planetaryIdle.ui.Colors
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.get
import com.github.jacks.planetaryIdle.ui.models.FarmModel
import com.github.jacks.planetaryIdle.ui.models.ObservatoryViewModel
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
    model: FarmModel,
    observatoryViewModel: ObservatoryViewModel,
    @Suppress("UNUSED_PARAMETER") stage: Stage,
    skin: Skin,
) : Table(skin), KTable {

    val goldLabel: Label
    private val insightLabel: Label

    // Persisted reveal flags — once a secondary resource has been seen (value > 0)
    // it stays visible forever, even if the value drops back to zero after a reset.
    private val prefs by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }
    private var insightRevealed = false

    init {
        background(skin[Drawables.BACKGROUND_SOLID_GREY])

        insightRevealed = prefs.getBoolean(PREF_INSIGHT_REVEALED, false)

        // Gold — primary resource, centred in remaining space
        goldLabel = label(formatGold(model.goldCoins), Labels.DEFAULT.skinKey) { cell ->
            cell.expandX().center()
        }
        goldLabel.color = Colors.YELLOW.color

        // Secondary resources — right-aligned, small font.
        // Label text is empty until first revealed; once revealed it is always shown.
        insightLabel = Label("", skin, Labels.SMALL.skinKey)
        insightLabel.color = Colors.BLUE.color
        add(insightLabel).top().right().padRight(8f)

        // Restore label immediately if already revealed in a previous session
        if (insightRevealed) {
            insightLabel.txt = formatInsight(observatoryViewModel.insight)
        }

        // Observers
        model.onPropertyChange(FarmModel::goldCoins) { amount ->
            goldLabel.txt = formatGold(amount)
        }
        observatoryViewModel.onPropertyChange(ObservatoryViewModel::insight) { value ->
            if (!insightRevealed && value > BigDecimal.ZERO) {
                insightRevealed = true
                prefs.putBoolean(PREF_INSIGHT_REVEALED, true)
                prefs.flush()
            }
            if (insightRevealed) {
                insightLabel.txt = formatInsight(value)
            }
        }
    }

    companion object {
        private val log = logger<HeaderView>()

        // Persistence keys for one-time reveal flags
        private const val PREF_INSIGHT_REVEALED = "header_insight_revealed"

        private val twoDecFormat = DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance())

        /** Toggled by SettingsModel when the player saves a notation preference change. */
        var useLetterNotation: Boolean = true

        private val THOUSAND           = BigDecimal(1_000L)
        private val SCIENTIFIC_CUTOFF  = BigDecimal("1e300")

        private val LETTER_THRESHOLDS: List<Pair<BigDecimal, String>> = listOf(
            // ── K – Vg  (1e3 – 1e63, traditional short-scale names) ──────────
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
            BigDecimal("1e54") to "Spd",
            BigDecimal("1e57") to "Ocd",
            BigDecimal("1e60") to "Nod",
            BigDecimal("1e63") to "Vg",
            // ── Vigintillion group  (1e66 – 1e90) ────────────────────────────
            BigDecimal("1e66") to "UVg",
            BigDecimal("1e69") to "DVg",
            BigDecimal("1e72") to "TVg",
            BigDecimal("1e75") to "QaVg",
            BigDecimal("1e78") to "QiVg",
            BigDecimal("1e81") to "SxVg",
            BigDecimal("1e84") to "SpVg",
            BigDecimal("1e87") to "OcVg",
            BigDecimal("1e90") to "NoVg",
            // ── Trigintillion group  (1e93 – 1e120) ──────────────────────────
            BigDecimal("1e93")  to "Tg",
            BigDecimal("1e96")  to "UTg",
            BigDecimal("1e99")  to "DTg",
            BigDecimal("1e102") to "TTg",
            BigDecimal("1e105") to "QaTg",
            BigDecimal("1e108") to "QiTg",
            BigDecimal("1e111") to "SxTg",
            BigDecimal("1e114") to "SpTg",
            BigDecimal("1e117") to "OcTg",
            BigDecimal("1e120") to "NoTg",
            // ── Quadragintillion group  (1e123 – 1e150) ──────────────────────
            BigDecimal("1e123") to "Qag",
            BigDecimal("1e126") to "UQag",
            BigDecimal("1e129") to "DQag",
            BigDecimal("1e132") to "TQag",
            BigDecimal("1e135") to "QaQag",
            BigDecimal("1e138") to "QiQag",
            BigDecimal("1e141") to "SxQag",
            BigDecimal("1e144") to "SpQag",
            BigDecimal("1e147") to "OcQag",
            BigDecimal("1e150") to "NoQag",
            // ── Quinquagintillion group  (1e153 – 1e180) ─────────────────────
            BigDecimal("1e153") to "Qig",
            BigDecimal("1e156") to "UQig",
            BigDecimal("1e159") to "DQig",
            BigDecimal("1e162") to "TQig",
            BigDecimal("1e165") to "QaQig",
            BigDecimal("1e168") to "QiQig",
            BigDecimal("1e171") to "SxQig",
            BigDecimal("1e174") to "SpQig",
            BigDecimal("1e177") to "OcQig",
            BigDecimal("1e180") to "NoQig",
            // ── Sexagintillion group  (1e183 – 1e210) ────────────────────────
            BigDecimal("1e183") to "Sxg",
            BigDecimal("1e186") to "USxg",
            BigDecimal("1e189") to "DSxg",
            BigDecimal("1e192") to "TSxg",
            BigDecimal("1e195") to "QaSxg",
            BigDecimal("1e198") to "QiSxg",
            BigDecimal("1e201") to "SxSxg",
            BigDecimal("1e204") to "SpSxg",
            BigDecimal("1e207") to "OcSxg",
            BigDecimal("1e210") to "NoSxg",
            // ── Septuagintillion group  (1e213 – 1e240) ──────────────────────
            BigDecimal("1e213") to "Spg",
            BigDecimal("1e216") to "USpg",
            BigDecimal("1e219") to "DSpg",
            BigDecimal("1e222") to "TSpg",
            BigDecimal("1e225") to "QaSpg",
            BigDecimal("1e228") to "QiSpg",
            BigDecimal("1e231") to "SxSpg",
            BigDecimal("1e234") to "SpSpg",
            BigDecimal("1e237") to "OcSpg",
            BigDecimal("1e240") to "NoSpg",
            // ── Octogintillion group  (1e243 – 1e270) ────────────────────────
            BigDecimal("1e243") to "Ocg",
            BigDecimal("1e246") to "UOcg",
            BigDecimal("1e249") to "DOcg",
            BigDecimal("1e252") to "TOcg",
            BigDecimal("1e255") to "QaOcg",
            BigDecimal("1e258") to "QiOcg",
            BigDecimal("1e261") to "SxOcg",
            BigDecimal("1e264") to "SpOcg",
            BigDecimal("1e267") to "OcOcg",
            BigDecimal("1e270") to "NoOcg",
            // ── Nonagintillion group  (1e273 – 1e297, then sci at 1e300) ─────
            BigDecimal("1e273") to "Nog",
            BigDecimal("1e276") to "UNog",
            BigDecimal("1e279") to "DNog",
            BigDecimal("1e282") to "TNog",
            BigDecimal("1e285") to "QaNog",
            BigDecimal("1e288") to "QiNog",
            BigDecimal("1e291") to "SxNog",
            BigDecimal("1e294") to "SpNog",
            BigDecimal("1e297") to "OcNog",
        )

        fun formatGold(amount: BigDecimal): String = "${formatShort(amount)} Gold"
        fun formatInsight(amount: BigDecimal): String = "${formatShort(amount)} Insight"

        fun formatShort(number: BigDecimal): String =
            if (useLetterNotation) formatLetter(number) else formatScientific(number)

        private fun formatLetter(number: BigDecimal): String {
            // Above 1e300 always use scientific notation regardless of the setting
            if (number >= SCIENTIFIC_CUTOFF) return formatScientific(number)
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
    observatoryViewModel: ObservatoryViewModel,
    stage: Stage,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: HeaderView.(S) -> Unit = {},
): HeaderView = actor(HeaderView(model, observatoryViewModel, stage, skin), init)
