package com.github.jacks.planetaryIdle.rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.jacks.planetaryIdle.events.BarnUnlockedEvent
import com.github.jacks.planetaryIdle.events.BuyResourceEvent
import com.github.jacks.planetaryIdle.events.ResourceUpdateEvent
import com.github.jacks.planetaryIdle.events.ViewStateChangeEvent
import com.github.jacks.planetaryIdle.ui.ViewState
import ktx.assets.disposeSafely
import ktx.log.logger
import java.math.BigDecimal

class IsometricMapRenderer : EventListener {

    private val camera = OrthographicCamera()
    private val batch = SpriteBatch()
    private var map: TiledMap? = null
    private var renderer: IsometricTiledMapRenderer? = null
    private var currentViewState: ViewState = ViewState.FARM

    // Tracks which named layers should currently be visible
    private val activeLayers = mutableSetOf("layer_base")

    init {
        tryLoadMap()
    }

    private fun tryLoadMap() {
        try {
            if (Gdx.files.internal("graphics/farm_map.tmx").exists()) {
                map = TmxMapLoader().load("graphics/farm_map.tmx")
                renderer = IsometricTiledMapRenderer(map, batch)
                applyLayerVisibility()
                log.debug { "Loaded farm_map.tmx" }
            } else {
                log.debug { "farm_map.tmx not found — isometric map will not render" }
            }
        } catch (e: Exception) {
            log.debug { "Failed to load farm_map.tmx: ${e.message}" }
        }
    }

    /** Called by RenderSystem each frame, before stage.draw(). */
    fun render(screenWidth: Float, screenHeight: Float) {
        if (currentViewState != ViewState.FARM) return
        val r = renderer ?: return

        camera.setToOrtho(false, screenWidth, screenHeight)
        camera.update()
        r.setView(camera)
        r.render()
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is ViewStateChangeEvent -> {
                currentViewState = event.state
            }
            is BarnUnlockedEvent -> {
                activeLayers.add("layer_barn")
                applyLayerVisibility()
            }
            is BuyResourceEvent -> {
                // Activate the layer for this resource immediately on purchase
                activeLayers.add("layer_${event.resourceType.lowercase()}")
                applyLayerVisibility()
            }
            is ResourceUpdateEvent -> {
                // Catch resources already owned on game load: first production tick activates their layer
                if (event.rscComp.amountOwned > BigDecimal.ZERO) {
                    val layerName = "layer_${event.rscComp.name.lowercase()}"
                    if (activeLayers.add(layerName)) {
                        // Only update visibility when the set actually changes
                        applyLayerVisibility()
                    }
                }
            }
        }
        return false
    }

    private fun applyLayerVisibility() {
        val m = map ?: return
        m.layers.forEach { layer ->
            layer.isVisible = layer.name in activeLayers
        }
    }

    fun dispose() {
        map.disposeSafely()
        batch.disposeSafely()
    }

    companion object {
        private val log = logger<IsometricMapRenderer>()
    }
}
