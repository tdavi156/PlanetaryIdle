package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.github.jacks.planetaryIdle.ui.models.BarnViewModel
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor

class BarnView(
    @Suppress("UNUSED_PARAMETER") model: BarnViewModel,
    skin: Skin,
) : Table(skin), KTable {

    init {
        setFillParent(true)
    }
}

@Scene2dDsl
fun <S> KWidget<S>.barnView(
    model: BarnViewModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: BarnView.(S) -> Unit = {},
): BarnView = actor(BarnView(model, skin), init)
