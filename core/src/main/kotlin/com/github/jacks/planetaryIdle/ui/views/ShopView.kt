package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.models.ShopModel
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.label
import ktx.scene2d.table

class ShopView(
    model : ShopModel,
    skin : Skin,
) : Table(skin), KTable {

    init {
        setFillParent(true)
        stage = getStage()

        table {
            label("test label on shop view", Labels.RED.skinKey) { cell ->
                cell.center().pad(3f)
            }
        }
    }

}
@Scene2dDsl
fun <S> KWidget<S>.shopView(
    model : ShopModel,
    skin : Skin = Scene2DSkin.defaultSkin,
    init : ShopView.(S) -> Unit = { }
) : ShopView = actor(ShopView(model, skin), init)
