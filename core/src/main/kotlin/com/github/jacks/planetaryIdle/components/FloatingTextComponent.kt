package com.github.jacks.planetaryIdle.components

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.github.jacks.planetaryIdle.events.CreditGoldEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import ktx.actors.plusAssign
import ktx.math.vec2
import java.math.BigDecimal

class FloatingTextComponent {
    val textStartLocation  = vec2()
    val textTargetLocation = vec2()
    var textDuration = 0f
    var time = 0f
    var amount: BigDecimal = BigDecimal.ZERO
    lateinit var label: Label
}

/** Registers the floating-text label with the stage on add, credits gold and removes it on remove. */
class FloatingTextComponentListener(
    private val stage: Stage
) : ComponentListener<FloatingTextComponent> {

    override fun onComponentAdded(entity: Entity, component: FloatingTextComponent) {
        stage.addActor(component.label)
        component.label += fadeOut(component.textDuration, Interpolation.exp5In)
    }

    override fun onComponentRemoved(entity: Entity, component: FloatingTextComponent) {
        stage.root.removeActor(component.label)
    }
}
