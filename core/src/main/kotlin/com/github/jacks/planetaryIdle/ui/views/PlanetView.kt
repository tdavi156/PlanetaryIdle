package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Align.center
import com.github.jacks.planetaryIdle.PlanetaryIdle
import com.github.jacks.planetaryIdle.events.FoodGrowthEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.jacks.planetaryIdle.ui.get
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.Fonts
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.models.PlanetModel
import ktx.actors.alpha
import ktx.actors.txt
import ktx.log.logger
import ktx.scene2d.*

class PlanetView(
    model : PlanetModel,
    skin : Skin,
) : Table(skin), KTable {

    private lateinit var stage : Stage
    //private var button2 : TextButton
    private var value : String = "Number: "
    //private val foodAmountLabel : Label

    init {
        setDebug(true)
        setFillParent(true)
        stage = getStage()

        table {
            //setFillParent(true)
            pad(50f)
            textButton("button1", Buttons.TEXT_BUTTON.skinKey) { cell ->
                cell.expand().top().left().width(180f).height(50f).pad(10f).colspan(2)
                // this pads the widget within the cell
                pad(10f)
            }
            textButton("button2", Buttons.TEXT_BUTTON.skinKey) { cell ->
                cell.expand().top().right()
            }

            row()

            textButton("button1", Buttons.TEXT_BUTTON.skinKey) { cell ->
                cell.expand().bottom().left()
            }
            textButton("button1", Buttons.TEXT_BUTTON.skinKey) { cell ->
                cell.expand().bottom()
            }
            textButton("button2", Buttons.TEXT_BUTTON.skinKey) { cell ->
                cell.expand().bottom().right()
            }

            it.expand().fill()
        }





//        table {
//            defaults().expand().pad(10f)
//            label(this@PlanetView.value, Labels.SMALL.skinKey)
//            this@PlanetView.foodAmountLabel = label("0", Labels.SMALL.skinKey)
//            textButton("button 1", Buttons.TEXT_BUTTON.skinKey) { buttonCell ->
//                buttonCell.expand().fill().pad(10f)
//            }
//            this@PlanetView.button2 = textButton("button add", Buttons.TEXT_BUTTON.skinKey) {
//                it.expand().fill().pad(10f)
//            }
//            this@PlanetView.button2.addListener(object : ChangeListener() {
//                override fun changed(event: ChangeEvent, actor: Actor) {
//                    log.debug { "button changed event" }
//                    stage.fire(FoodGrowthEvent())
//                }
//            })
//        }

        // Data Binding
        model.onPropertyChange(PlanetModel::foodAmount) { foodAmount ->
            foodAmountChange(foodAmount)
        }
    }

    private fun foodAmountChange(foodAmount : Int) {
        //foodAmountLabel.txt = foodAmount.toString()
    }

    companion object {
        private val log = logger<PlanetView>()
    }
}

@Scene2dDsl
fun <S> KWidget<S>.planetView(
    model : PlanetModel,
    skin : Skin = Scene2DSkin.defaultSkin,
    init : PlanetView.(S) -> Unit = { }
) : PlanetView = actor(PlanetView(model, skin), init)
