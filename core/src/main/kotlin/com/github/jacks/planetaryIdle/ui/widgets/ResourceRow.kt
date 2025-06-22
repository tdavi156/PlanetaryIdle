package com.github.jacks.planetaryIdle.ui.widgets

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.utils.Align
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.get
import ktx.actors.alpha
import ktx.actors.plusAssign
import ktx.scene2d.*

class ResourceRow(
    private val backgroundDrawable : Drawables?,
    private val skin : Skin,
    private val name : String,
    private var multiplier : String,
    private var owned : String,
    private val button : TextButton
) : WidgetGroup(), KGroup {

    val background : Image = if (backgroundDrawable == null) Image(skin[Drawables.BAR_GREY_THICK]) else Image(skin[backgroundDrawable])
    val resourceName : String = name
    val resourceMultiplier : String = multiplier
    val resourceOwned : String = owned
    val resourceButton : TextButton = button


    // text for the name of the row
    // modifiable text for the multiplier (based on the number of 100 units owned)
    // modifiable text for the number owned
    // button
    // text on the button for how many to buy
    // text on button for "Assign:"
    // text on the button for the actual cost number
    // text on the button for "population"
    init {

        this += background.apply {
            setFillParent(true)
            alpha = 0.5f
        }
        this += label(resourceName, Labels.SEMI_MEDIUM.skinKey).apply {
            setAlignment(Align.left)
        }
        this += label(resourceMultiplier, Labels.SEMI_MEDIUM.skinKey).apply {
            setAlignment(Align.center)
        }
        this += label(resourceOwned, Labels.SEMI_MEDIUM.skinKey).apply {
            setAlignment(Align.center)
        }
        this += button.apply {
            this.right()
        }
    }

    override fun getPrefWidth(): Float = background.drawable.minWidth
    override fun getPrefHeight(): Float = background.drawable.minHeight
}

@Scene2dDsl
fun <S>KWidget<S>.resourceRow(
    backgroundDrawable: Drawables? = null,
    skin: Skin = Scene2DSkin.defaultSkin,
    name: String,
    multiplier: String,
    owned: String,
    button : TextButton,
    init: ResourceRow.(S) -> Unit = { }
) : ResourceRow = actor(ResourceRow(backgroundDrawable, skin, name, multiplier, owned, button), init)
