package com.github.jacks.planetaryIdle.systems

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.github.jacks.planetaryIdle.components.FloatingTextComponent
import com.github.jacks.planetaryIdle.events.FloatingTextEvent
import com.github.jacks.planetaryIdle.ui.Colors
import com.github.jacks.planetaryIdle.ui.Fonts
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.math.vec2
import ktx.scene2d.Scene2DSkin

@AllOf([FloatingTextComponent::class])
class FloatingTextSystem(
    private val stage: Stage,
    private val textComponents: ComponentMapper<FloatingTextComponent>,
) : IteratingSystem(), EventListener {

    private val currentLocation = vec2()

    override fun onTickEntity(entity: Entity) {
        with(textComponents[entity]) {
            if (time >= textDuration) {
                world.remove(entity)
                return
            }

            time += deltaTime
            val progress = (time / textDuration).coerceAtMost(1f)

            // TODO: Check interpolation visually after first run.
            // Currently Interpolation.exp5Out (fast start, smooth arrival).
            // If motion doesn't feel right, try Interpolation.exp5In (slow start, fast finish).
            currentLocation.set(textStartLocation)
            currentLocation.interpolate(textTargetLocation, progress, Interpolation.exp5In)
            label.setPosition(currentLocation.x, currentLocation.y)
        }
    }

    override fun handle(event: Event): Boolean {
        if (event !is FloatingTextEvent) return false

        val style = Label.LabelStyle(Scene2DSkin.defaultSkin.getFont(Fonts.LARGE.skinKey), Colors.YELLOW.color)
        world.entity {
            add<FloatingTextComponent> {
                textStartLocation.set(event.startPosition)
                textTargetLocation.set(event.targetPosition)
                textDuration = 0.6f
                amount = event.amount
                label = Label(event.displayText, style)

            }
        }
        return true
    }
}
