package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
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

    lateinit var button2 : TextButton
    private var number : Int = 0
    private var value : String = "Number: "

    init {
        // UI Elements
        setFillParent(true)
        setDebug(true)
        log.debug { "init Planet View" }
        table {
            defaults().expand().pad(10f)
            label(this@PlanetView.value, Labels.SMALL.skinKey)
            label(this@PlanetView.number.toString(), Labels.SMALL.skinKey)
            textButton("button 1", Buttons.TEXT_BUTTON.skinKey) { buttonCell ->
                buttonCell.expand().fill().pad(10f)
            }
            this@PlanetView.button2 = textButton("button add", Buttons.TEXT_BUTTON.skinKey) {
                it.expand().fill().pad(10f)
            }
            this@PlanetView.button2.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    log.debug { "button changed event" }
                    this@PlanetView.number++
                }
            })

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
