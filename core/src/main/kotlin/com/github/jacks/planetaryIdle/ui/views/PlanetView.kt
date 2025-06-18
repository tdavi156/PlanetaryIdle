package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import com.github.jacks.planetaryIdle.PlanetaryIdle
import com.github.jacks.planetaryIdle.ui.get
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.models.PlanetModel
import ktx.actors.alpha
import ktx.log.logger
import ktx.scene2d.*

class PlanetView(
    skin : Skin
) : Table(skin), KTable {


    init {
        // UI Elements
        setFillParent(true)

        log.debug { "init Planet View" }
        table {
            label(text = "test label", style = Labels.TITLE.skinKey) { labelCell ->
                this.setAlignment(Align.topLeft)
                this.wrap = true
                labelCell.expand().fill().pad(10f)
            }

            this.alpha = 1f
            it.expand().width(130f).height(100f).top().padTop(10f)
        }

        // Data Binding

    }

    companion object {
        private val log = logger<PlanetView>()
    }
}

@Scene2dDsl
fun <S> KWidget<S>.planetView(
    skin : Skin = Scene2DSkin.defaultSkin,
    init : PlanetView.(S) -> Unit = { }
) : PlanetView = actor(PlanetView(skin), init)
