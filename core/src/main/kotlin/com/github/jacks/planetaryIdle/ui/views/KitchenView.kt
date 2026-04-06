package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.github.jacks.planetaryIdle.ui.models.KitchenViewModel
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor

class KitchenView(
    @Suppress("UNUSED_PARAMETER") model: KitchenViewModel,
    skin: Skin,
) : Table(skin), KTable {

    init {
        setFillParent(true)
    }
}

@Scene2dDsl
fun <S> KWidget<S>.kitchenView(
    model: KitchenViewModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: KitchenView.(S) -> Unit = {},
): KitchenView = actor(KitchenView(model, skin), init)
