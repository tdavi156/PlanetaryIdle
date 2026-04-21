package com.github.jacks.planetaryIdle.rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
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
import kotlin.random.Random

class IsometricMapRenderer : EventListener {

    private val camera = OrthographicCamera()
    private val batch = SpriteBatch()
    private var map: TiledMap? = null
    private var renderer: IsometricTiledMapRenderer? = null
    private var atlas: TextureAtlas? = null
    private var currentViewState: ViewState = ViewState.FARM

    private val colorOrder = PlanetResources.entries.map { it.resourceName }

    private var zoom = 1f
    private var initialZoom = 0f
    private var zoomInitialized = false
    private var mapCenterX = 0f
    private var mapCenterY = 0f

    private var halfTileW = 0f
    private var halfTileH = 0f

    // Clouds
    private val clouds = mutableListOf<CloudInstance>()
    private var cloudsInitialized = false
    private val screenProjection = Matrix4()
    private val rng = Random(System.currentTimeMillis())

    // Colors unlocked so far, in unlock order — never cleared (persists through resets)
    private val unlockedColors = mutableListOf<String>()
    private var barnUnlocked = false
    private var kitchenUnlocked = false
    private var observatoryUnlocked = false

    private val prefs by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }

    companion object {
        private val log = logger<IsometricMapRenderer>()
        private const val PREF_MAX_COLOR_ORDINAL = "map_max_color_ordinal"

        // ── Cloud system ──────────────────────────────────────────────────────
        // Adjust baseSpeed (px/sec), baseOpacity, and baseScale to taste.
        // Small random variance is applied per-instance on spawn.
        private enum class CloudLayer(
            val baseSpeed: Float,
            val baseOpacity: Float,
            val baseScale: Float
        ) {
            FAR   (15f,  0.30f, 0.35f),
            MEDIUM(35f,  0.60f, 0.60f),
            CLOSE (65f,  0.85f, 0.90f),
        }

        private data class CloudInstance(
            val region: TextureAtlas.AtlasRegion,
            var x: Float,
            val y: Float,
            val width: Float,
            val height: Float,
            val speed: Float,
            val opacity: Float,
        )

        // ── Sprite system ─────────────────────────────────────────────────────
        // World-space positions for trees and buildings.
        // footFromLeft  — pixels from left edge to foot contact point (-1 = image centre X)
        // footFromBottom — pixels above the bottom edge where the foot sits (0 = exact bottom)
        // Tune worldX/worldY by running the game and adjusting to taste.
        private data class SpriteEntry(
            val worldX: Float,
            val worldY: Float,
            val footFromLeft: Float = -1f,
            val footFromBottom: Float = 0f
        )

        private val spriteDefinitions = mapOf(
            // Trees
            "tree_red"    to SpriteEntry(5320f, 100f),
            "tree_orange" to SpriteEntry(4150f, 240f),
            "tree_yellow" to SpriteEntry(4550f, -550f),
            "tree_green"  to SpriteEntry(5070f, 900f),
            "tree_blue"   to SpriteEntry(5120f, 900f),
            "tree_purple" to SpriteEntry(5120f, 900f),
            "tree_pink"   to SpriteEntry(5120f, 900f),
            "tree_brown"  to SpriteEntry(5120f, 900f),
            "tree_white"  to SpriteEntry(5120f, 900f),
            "tree_black"  to SpriteEntry(5120f, 900f),
            // Buildings
            "barn"        to SpriteEntry(6100f, 0f),
            "kitchen"     to SpriteEntry(5120f, 900f),
            "observatory" to SpriteEntry(5120f, 900f),
        )
    }

    init {
        tryLoadMap()
    }

    private fun tryLoadMap() {
        try {
            loadPersistedColorState()
            if (Gdx.files.internal("maps/farm_map.tmx").exists()) {
                map = TmxMapLoader().load("maps/farm_map.tmx")
                renderer = IsometricTiledMapRenderer(map, batch)
                applyLayerVisibility()
                log.debug { "Loaded farm_map.tmx" }
            } else {
                log.debug { "farm_map.tmx not found — isometric map will not render" }
            }
        } catch (e: Exception) {
            log.debug { "Failed to load farm_map.tmx: ${e.message}" }
        }

        try {
            if (Gdx.files.internal("graphics/PlanetaryIdle.atlas").exists()) {
                atlas = TextureAtlas(Gdx.files.internal("graphics/PlanetaryIdle.atlas"))
                log.debug { "Loaded PlanetaryIdle.atlas" }
            } else {
                log.debug { "PlanetaryIdle.atlas not found — sprites will not render" }
            }
        } catch (e: Exception) {
            log.debug { "Failed to load PlanetaryIdle.atlas: ${e.message}" }
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
        val m = map ?: return

        if (!zoomInitialized) {
            val props = m.properties
            val mapTileWidth  = props.get("width",      Int::class.java) ?: 0
            val mapTileHeight = props.get("height",     Int::class.java) ?: 0
            val tileWidth     = props.get("tilewidth",  Int::class.java) ?: 0
            val tileHeight    = props.get("tileheight", Int::class.java) ?: 0
            val mapPixelWidth  = (mapTileWidth + mapTileHeight) * tileWidth  / 2f
            val mapPixelHeight = (mapTileWidth + mapTileHeight) * tileHeight / 2f
            zoom = maxOf(mapPixelWidth / screenWidth, mapPixelHeight / screenHeight) / 2.5f
            initialZoom = zoom
            mapCenterX = mapPixelWidth / 2f
            mapCenterY = 800f
            halfTileW = tileWidth / 2f
            halfTileH = tileHeight / 2f
            zoomInitialized = true
            log.debug { "screen=${screenWidth}x${screenHeight} mapTiles=${mapTileWidth}x${mapTileHeight} tileSize=${tileWidth}x${tileHeight} mapPixels=${mapPixelWidth}x${mapPixelHeight} initialZoom=$zoom center=($mapCenterX,$mapCenterY)" }
        }

        camera.setToOrtho(false, screenWidth, screenHeight)
        camera.zoom = zoom
        camera.position.set(mapCenterX, mapCenterY, 0f)
        camera.update()
        r.setView(camera)
        r.render()
        renderSprites()
        renderClouds(screenWidth, screenHeight)
    }

    // ── Cloud rendering ───────────────────────────────────────────────────────

    private fun initClouds(screenWidth: Float, screenHeight: Float) {
        val a = atlas ?: return
        val cloudRegions = mutableListOf<TextureAtlas.AtlasRegion>()
        for (region in a.regions) {
            if (region.name.startsWith("cloud_")) cloudRegions.add(region)
        }
        if (cloudRegions.isEmpty()) return

        data class LayerConfig(val layer: CloudLayer, val count: Int, val yMin: Float, val yMax: Float)
        val layerConfigs = listOf(
            LayerConfig(CloudLayer.FAR,    4, screenHeight * 0.60f, screenHeight * 0.88f),
            LayerConfig(CloudLayer.MEDIUM, 3, screenHeight * 0.40f, screenHeight * 0.72f),
            LayerConfig(CloudLayer.CLOSE,  3, screenHeight * 0.22f, screenHeight * 0.58f),
        )

        for (config in layerConfigs) {
            val layer = config.layer
            repeat(config.count) {
                val region = cloudRegions[rng.nextInt(cloudRegions.size)]
                val scale = layer.baseScale + (rng.nextFloat() - 0.5f) * 0.10f
                val w = region.regionWidth * scale
                val h = region.regionHeight * scale
                // Spread initial positions across the full screen width so they
                // don't all appear at once on the right edge at startup
                val x = rng.nextFloat() * (screenWidth + w) - w
                val y = config.yMin + rng.nextFloat() * (config.yMax - config.yMin)
                val speedMult  = 0.85f + rng.nextFloat() * 0.30f
                val opacityOff = (rng.nextFloat() - 0.5f) * 0.10f
                clouds.add(CloudInstance(
                    region  = region,
                    x       = x,
                    y       = y,
                    width   = w,
                    height  = h,
                    speed   = layer.baseSpeed * speedMult,
                    opacity = (layer.baseOpacity + opacityOff).coerceIn(0.10f, 1.0f),
                ))
            }
        }
        cloudsInitialized = true
        log.debug { "Initialized ${clouds.size} clouds from ${cloudRegions.size} cloud regions" }
    }

    private fun renderClouds(screenWidth: Float, screenHeight: Float) {
        if (!cloudsInitialized) initClouds(screenWidth, screenHeight)
        if (clouds.isEmpty()) return
        val delta = Gdx.graphics.deltaTime
        batch.projectionMatrix = screenProjection.setToOrtho2D(0f, 0f, screenWidth, screenHeight)
        batch.begin()
        for (cloud in clouds) {
            cloud.x -= cloud.speed * delta
            if (cloud.x + cloud.width < 0f) {
                // Respawn off the right edge with a small random stagger
                cloud.x = screenWidth + rng.nextFloat() * 150f
            }
            batch.setColor(1f, 1f, 1f, cloud.opacity)
            batch.draw(cloud.region, cloud.x, cloud.y, cloud.width, cloud.height)
        }
        batch.setColor(1f, 1f, 1f, 1f)
        batch.end()
    }

    // ── Sprite rendering ──────────────────────────────────────────────────────

    private fun renderSprites() {
        val a = atlas ?: return
        batch.projectionMatrix = camera.combined
        batch.begin()
        for (color in unlockedColors) {
            val entry = spriteDefinitions["tree_$color"] ?: continue
            drawSprite(a, "tree_$color", entry)
        }
        if (barnUnlocked)        spriteDefinitions["barn"]?.let        { drawSprite(a, "barn",        it) }
        if (kitchenUnlocked)     spriteDefinitions["kitchen"]?.let     { drawSprite(a, "kitchen",     it) }
        if (observatoryUnlocked) spriteDefinitions["observatory"]?.let { drawSprite(a, "observatory", it) }
        batch.end()
    }

    private fun drawSprite(atlas: TextureAtlas, name: String, entry: SpriteEntry) {
        val region = atlas.findRegion(name) ?: return
        val w = region.regionWidth.toFloat()
        val h = region.regionHeight.toFloat()
        val fx = if (entry.footFromLeft < 0f) w / 2f else entry.footFromLeft
        batch.draw(region, entry.worldX - fx, entry.worldY - entry.footFromBottom, w, h)
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    /**
     * Scale factor for stage-space particles so they stay proportional to the tree's visual size
     * as the player zooms in/out. Returns 1.0 until the camera is initialised.
     */
    val particleScale: Float
        get() = if (zoomInitialized && zoom > 0f) (initialZoom / zoom).coerceIn(0.15f, 4f) else 1f

    /**
     * Projects a tree's world-space position through the isometric camera into stage/screen
     * coordinates. Returns null if the camera hasn't been initialised yet (first frame) or if
     * no sprite entry exists for the given color name.
     *
     * The returned point sits near the upper canopy of the sprite, which is a good origin for
     * rising particles.
     */
    fun getTreeScreenPosition(colorName: String): Vector2? {
        if (!zoomInitialized) return null
        val entry = spriteDefinitions["tree_$colorName"] ?: return null
        val a = atlas ?: return null
        val region = a.findRegion("tree_$colorName") ?: return null
        val w = region.regionWidth.toFloat()
        val h = region.regionHeight.toFloat()
        val fx = if (entry.footFromLeft < 0f) w / 2f else entry.footFromLeft
        // Sprite bottom-left corner in world space
        val spriteX = entry.worldX - fx
        val spriteY = entry.worldY - entry.footFromBottom
        // Project the canopy area (centre-x, ~70% up the sprite)
        val vec = Vector3(spriteX + w * 0.5f, spriteY + h * 0.70f, 0f)
        camera.project(vec)
        return Vector2(vec.x, vec.y)
    }

    /** Adjusts zoom on scroll — only active in FARM view. Returns true if the event was consumed. */
    fun adjustZoom(amount: Float): Boolean {
        if (currentViewState != ViewState.FARM) return false
        zoom = (zoom * if (amount > 0) 1.15f else 0.87f).coerceIn(0.5f, 15f)
        return true
    }

    // ── Events ────────────────────────────────────────────────────────────────

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
                layer.name == "layer_background_image" -> true
                layer.name.endsWith("_ground") -> layer.name == "layer_${latestGround ?: "red"}_ground"
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
        atlas.disposeSafely()
    }
}
