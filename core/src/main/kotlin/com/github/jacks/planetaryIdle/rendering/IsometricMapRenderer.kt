package com.github.jacks.planetaryIdle.rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.jacks.planetaryIdle.components.PlanetResources
import com.github.jacks.planetaryIdle.events.BarnUnlockedEvent
import com.github.jacks.planetaryIdle.events.BuyResourceEvent
import com.github.jacks.planetaryIdle.events.KitchenUnlockedEvent
import com.github.jacks.planetaryIdle.events.ObservatoryUnlockedEvent
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

    private val colorOrder = PlanetResources.entries.map { it.resourceName }

    // Colors unlocked so far, in unlock order — never cleared (persists through resets)
    private val unlockedColors = mutableListOf<String>()
    private var barnUnlocked = false
    private var kitchenUnlocked = false
    private var observatoryUnlocked = false

    private val prefs by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }

    companion object {
        private val log = logger<IsometricMapRenderer>()
        private const val PREF_MAX_COLOR_ORDINAL = "map_max_color_ordinal"
    }

    init {
        tryLoadMap()
    }

    private fun tryLoadMap() {
        try {
            loadPersistedColorState()
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

    private fun loadPersistedColorState() {
        val maxOrdinal = prefs.getInteger(PREF_MAX_COLOR_ORDINAL, -1)
        if (maxOrdinal >= 0) {
            for (i in 0..minOf(maxOrdinal, colorOrder.lastIndex)) {
                val color = colorOrder[i]
                if (color !in unlockedColors) unlockedColors.add(color)
            }
        }
    }

    private fun unlockColor(color: String) {
        if (color in unlockedColors) return
        unlockedColors.add(color)
        val ordinal = colorOrder.indexOf(color)
        if (ordinal >= 0) {
            val savedMax = prefs.getInteger(PREF_MAX_COLOR_ORDINAL, -1)
            if (ordinal > savedMax) {
                prefs.putInteger(PREF_MAX_COLOR_ORDINAL, ordinal)
                prefs.flush()
            }
        }
        applyLayerVisibility()
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
            is ViewStateChangeEvent -> currentViewState = event.state
            is BarnUnlockedEvent -> {
                barnUnlocked = true
                applyLayerVisibility()
            }
            is KitchenUnlockedEvent -> {
                kitchenUnlocked = true
                applyLayerVisibility()
            }
            is ObservatoryUnlockedEvent -> {
                observatoryUnlocked = true
                applyLayerVisibility()
            }
            is BuyResourceEvent -> unlockColor(event.resourceType.lowercase())
            is ResourceUpdateEvent -> {
                if (event.rscComp.amountOwned > BigDecimal.ZERO) {
                    unlockColor(event.rscComp.name.lowercase())
                }
            }
        }
        return false
    }

    private fun applyLayerVisibility() {
        val m = map ?: return
        val latestGround = unlockedColors.lastOrNull()
        m.layers.forEach { layer ->
            layer.isVisible = when {
                layer.name.endsWith("_ground") -> layer.name == "layer_${latestGround}_ground"
                layer.name.endsWith("_tree")   -> {
                    val color = layer.name.removePrefix("layer_").removeSuffix("_tree")
                    color in unlockedColors
                }
                layer.name == "layer_barn"        -> barnUnlocked
                layer.name == "layer_kitchen"     -> kitchenUnlocked
                layer.name == "layer_observatory" -> observatoryUnlocked
                else                              -> false
            }
        }
    }

    fun dispose() {
        map.disposeSafely()
        batch.disposeSafely()
    }
}
