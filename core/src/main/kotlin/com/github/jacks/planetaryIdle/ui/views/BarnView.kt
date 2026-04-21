package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.github.jacks.planetaryIdle.data.BarnUpgrade
import com.github.jacks.planetaryIdle.events.BuyBarnUpgradeEvent
import com.github.jacks.planetaryIdle.events.fire
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.get
import com.github.jacks.planetaryIdle.ui.models.BarnUpgradeState
import com.github.jacks.planetaryIdle.ui.models.BarnViewModel
import com.github.jacks.planetaryIdle.ui.views.HeaderView.Companion.formatShort
import ktx.actors.txt
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor

private const val NODE_SIZE       = 96f
private const val CANVAS_WIDTH    = 4000f
private const val CANVAS_HEIGHT   = 1800f
private const val DOUBLE_CLICK_MS = 300L

/** Draws connection lines between revealed nodes using ShapeRenderer. */
private class ConnectionLinesActor(
    private val nodeActors: Map<BarnUpgrade, Actor>,
    private val revealedProvider: () -> Set<BarnUpgrade>,
) : Actor() {

    private val shapeRenderer = ShapeRenderer()

    override fun draw(batch: com.badlogic.gdx.graphics.g2d.Batch, parentAlpha: Float) {
        val revealed = revealedProvider()
        batch.end()
        shapeRenderer.projectionMatrix = batch.projectionMatrix
        shapeRenderer.transformMatrix  = batch.transformMatrix
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color(0.5f, 0.5f, 0.5f, 0.7f)
        BarnUpgrade.connections.forEach { (parent, child) ->
            if (parent !in revealed || child !in revealed) return@forEach
            val p = nodeActors[parent] ?: return@forEach
            val c = nodeActors[child]  ?: return@forEach
            shapeRenderer.line(p.x + NODE_SIZE / 2f, p.y + NODE_SIZE / 2f,
                               c.x + NODE_SIZE / 2f, c.y + NODE_SIZE / 2f)
        }
        shapeRenderer.end()
        batch.begin()
    }

    fun dispose() = shapeRenderer.dispose()
}

class BarnView(
    private val model: BarnViewModel,
    private val stage: Stage,
    skin: Skin,
) : Table(skin), KTable {

    private val nodeActors    = mutableMapOf<BarnUpgrade, Table>()
    private val nodeLabels    = mutableMapOf<BarnUpgrade, Label>()
    private val revealedNodes = mutableSetOf<BarnUpgrade>()
    private var linesActor: ConnectionLinesActor? = null
    private var zoomScale     = 1f

    private lateinit var infoName:        Label
    private lateinit var infoDescription: Label
    private lateinit var infoLevel:       Label
    private lateinit var infoCost:        Label
    private lateinit var infoBuyButton:   TextButton

    // Held as a field so layout() can set the initial scroll position.
    private lateinit var barnScrollPane: ScrollPane
    private var scrollInitialized = false

    private var selectedUpgrade: BarnUpgrade? = null
    private var goldCoins = model.farmModel.goldCoins

    init {
        setFillParent(true)
        val view = this@BarnView

        // ── Canvas (nodes + lines) inside a ScrollPane ────────────────────
        // Plain Group (not WidgetGroup/Layout): ScrollPane reads getWidth/getHeight directly
        // for non-Layout content. We keep size == CANVAS_WIDTH/HEIGHT * zoomScale at all
        // times so ScrollPane always sees the correct scroll bounds after a zoom change.
        //
        // setCullingArea is overridden to a no-op: ScrollPane sets a culling area in the
        // Group's LOCAL (unscaled) coordinate space, but canvasGroup uses setScale(zoomScale)
        // for zoom. At any zoom != 1, local coord 800 maps to screen 800×zoom, so a node
        // on-screen at zoom=0.5 (local 800 → screen 400) would be incorrectly culled because
        // 800 > cullingArea.width (~700). The ScrollPane's scissor test operates in screen
        // coords and is always correct, so we only suppress the broken local-space culling.
        val canvasGroup = object : Group() {
            // ScrollPane sets a cullingArea in the Group's LOCAL (unscaled) coordinate space,
            // but canvasGroup uses setScale(zoomScale) for zoom. A node at local x=800 that
            // renders on-screen at x=400 when zoom=0.5 gets incorrectly culled because
            // 800 > cullingArea.width (~700 viewport px). Clearing it in draw() before the
            // super call ensures Group.drawChildren always uses the no-culling path; the
            // ScrollPane's scissor test (screen-space) still provides correct clipping.
            override fun draw(batch: com.badlogic.gdx.graphics.g2d.Batch, parentAlpha: Float) {
                cullingArea = null
                super.draw(batch, parentAlpha)
            }
        }.apply {
            setSize(CANVAS_WIDTH, CANVAS_HEIGHT)
            // Enable so empty-space touches land on this group (for drag-to-pan).
            touchable = Touchable.enabled
        }

        BarnUpgrade.entries.forEach { upgrade ->
            val node = view.buildNodeActor(upgrade)
            view.nodeActors[upgrade] = node
            canvasGroup.addActor(node)
        }

        val lines = ConnectionLinesActor(nodeActors, { revealedNodes })
        linesActor = lines
        canvasGroup.addActorAt(0, lines)
        lines.setSize(CANVAS_WIDTH, CANVAS_HEIGHT)

        // Drag-to-pan listener on the canvas group (content-follows-cursor / map-drag style).
        // Uses stage coordinates so panning remains stable as the scroll pane repositions the group.
        // Nodes consume their own touchDown (return true), so this listener only fires on empty space.
        var lastStageX = 0f
        var lastStageY = 0f
        canvasGroup.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                // Only fires when empty canvas space is hit (nodes consume their own events).
                lastStageX = event.stageX
                lastStageY = event.stageY
                return true
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                val dx = event.stageX - lastStageX
                val dy = event.stageY - lastStageY
                // Content-follows-cursor (map-drag) model:
                //   Drag right (dx>0) → canvas shifts right → reveal left content  → scrollX decreases
                //   Drag up   (dy>0)  → canvas shifts up    → reveal bottom content → scrollY increases
                barnScrollPane.scrollX = (barnScrollPane.scrollX - dx).coerceIn(0f, barnScrollPane.maxX)
                barnScrollPane.scrollY = (barnScrollPane.scrollY + dy).coerceIn(0f, barnScrollPane.maxY)
                lastStageX = event.stageX
                lastStageY = event.stageY
            }
        })

        barnScrollPane = ScrollPane(canvasGroup).apply {
            setScrollingDisabled(false, false)  // must be false so ScrollPane sizes content to getWidth/Height, not viewport
            setOverscroll(false, false)
            setFlickScroll(false)               // drag-pan handled by canvasGroup listener above
        }

        // ── Scroll-wheel zoom ─────────────────────────────────────────────
        // amountY < 0 = scroll up = zoom in; amountY > 0 = scroll down = zoom out.
        barnScrollPane.addListener(object : InputListener() {
            override fun scrolled(event: InputEvent, x: Float, y: Float,
                                  amountX: Float, amountY: Float): Boolean {
                val prevZoom = zoomScale

                // 1. Capture logical (unscaled) canvas coordinate at the viewport centre
                //    BEFORE changing anything. Reading scrollX after validate() would give
                //    a clamped value if the canvas just shrank, corrupting the centre point.
                val logCX = (barnScrollPane.scrollX + barnScrollPane.width  * 0.5f) / prevZoom
                val logCY = (barnScrollPane.scrollY + barnScrollPane.height * 0.5f) / prevZoom

                // 2. Apply zoom — ±7.4 % per tick feels smooth; 1/1.08 keeps zoom-in/out symmetric.
                zoomScale = (zoomScale * if (amountY < 0) 1.08f else (1f / 1.08f)).coerceIn(0.25f, 3f)
                canvasGroup.setScale(zoomScale)
                // Keep reported size == scaled canvas so ScrollPane computes correct maxX/maxY.
                canvasGroup.setSize(CANVAS_WIDTH * zoomScale, CANVAS_HEIGHT * zoomScale)

                // 3. Compute new scroll bounds directly — canvasGroup size is already updated,
                //    so we don't need validate() just to read maxX/maxY.
                val newMaxX = maxOf(0f, CANVAS_WIDTH  * zoomScale - barnScrollPane.width)
                val newMaxY = maxOf(0f, CANVAS_HEIGHT * zoomScale - barnScrollPane.height)

                // 4. Set scroll so the same logical point stays at the viewport centre.
                barnScrollPane.scrollX = (logCX * zoomScale - barnScrollPane.width  * 0.5f).coerceIn(0f, newMaxX)
                barnScrollPane.scrollY = (logCY * zoomScale - barnScrollPane.height * 0.5f).coerceIn(0f, newMaxY)

                // 5. Validate AFTER scroll is set so ScrollPane positions the content correctly
                //    this frame rather than one frame later (which caused the visible snap).
                barnScrollPane.invalidate()
                barnScrollPane.validate()
                return true
            }
        })

        // ── Info panel at bottom ─────────────────────────────────────────
        // Constructed as a plain Table (not via the KTX 'table {}' DSL) so it
        // is never auto-added as a phantom unconstrained Cell to BarnView.
        val infoPanel = Table(skin)
        // Use a solid 1×1 pixel background so there are no 9-patch edge artefacts.
        infoPanel.background = view.skin[Drawables.BACKGROUND_SOLID_GREY]

        val leftTable = Table(skin)
        view.infoName = Label("Select an upgrade", skin, Labels.MEDIUM.skinKey).also {
            it.setAlignment(Align.left)
        }
        view.infoDescription = Label("", skin, Labels.SMALL.skinKey).also {
            it.setAlignment(Align.topLeft)
            it.wrap = true
        }
        leftTable.add(view.infoName).left().padLeft(10f)
        leftTable.row()
        leftTable.add(view.infoDescription).left().padLeft(10f).expandX().fillX()
        infoPanel.add(leftTable).top().left().expandX().fillX().pad(5f)

        val rightTable = Table(skin)
        view.infoLevel = Label("", skin, Labels.SMALL.skinKey)
        view.infoCost  = Label("", skin, Labels.SMALL.skinKey)
        view.infoBuyButton = TextButton("Buy", skin, Buttons.GREEN_BUTTON_MEDIUM.skinKey).also { btn ->
            btn.isDisabled = true
            btn.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    view.selectedUpgrade?.let { upgrade ->
                        view.stage.fire(BuyBarnUpgradeEvent(upgrade))
                    }
                }
            })
        }
        rightTable.add(view.infoLevel).right().padRight(10f)
        rightTable.row()
        rightTable.add(view.infoCost).right().padRight(10f)
        rightTable.row()
        rightTable.add(view.infoBuyButton).right().width(140f).height(45f).pad(5f, 5f, 5f, 10f)
        infoPanel.add(rightTable).top().right().pad(5f)

        add(barnScrollPane).expand().fill()
        row()
        add(infoPanel).expandX().fillX().height(INFO_PANEL_HEIGHT).pad(0f, 8f, 4f, 8f)

        // ── Model bindings ───────────────────────────────────────────────
        model.onPropertyChange(BarnViewModel::upgradeStates) { states ->
            applyStates(states)
        }
        model.farmModel.onPropertyChange(
            com.github.jacks.planetaryIdle.ui.models.FarmModel::goldCoins
        ) { gold ->
            goldCoins = gold
            updateBuyButtonState()
        }

        applyStates(model.upgradeStates)
    }

    // ── Initial scroll positioning ────────────────────────────────────────────
    override fun layout() {
        super.layout()
        // Centre the viewport on the SOIL node the first time the pane is sized.
        if (!scrollInitialized && barnScrollPane.width > 0f && barnScrollPane.height > 0f) {
            scrollInitialized = true
            val soilCenterX = BarnUpgrade.SOIL.nodeX + NODE_SIZE / 2f   // 800 + 48 = 848
            val soilCenterY = BarnUpgrade.SOIL.nodeY + NODE_SIZE / 2f   // 1100 + 48 = 1148
            // scrollX/scrollY are in visual (scaled) content coordinates.
            // scrollY=0 = top of canvas (high y-values in LibGDX bottom-left origin).
            barnScrollPane.scrollX = (soilCenterX * zoomScale - barnScrollPane.width  / 2f)
                .coerceIn(0f, barnScrollPane.maxX)
            barnScrollPane.scrollY = ((CANVAS_HEIGHT - soilCenterY) * zoomScale - barnScrollPane.height / 2f)
                .coerceIn(0f, barnScrollPane.maxY)
        }
    }

    // ── Node actor construction ─────────────────────────────────────────────
    private fun buildNodeActor(upgrade: BarnUpgrade): Table {
        val view = this
        val nodeTable = Table(skin)
        nodeTable.setSize(NODE_SIZE, NODE_SIZE)
        nodeTable.setPosition(upgrade.nodeX, upgrade.nodeY)
        nodeTable.background = skin[Drawables.BACKGROUND_GREY]
        nodeTable.isVisible  = false

        val shortName = upgrade.displayName
            .replace("Expertise", "Exp.")
            .replace("Upgrade", "")
            .replace("Quality", "Qual.")
            .replace("Improved", "Impr.")
            .trim()
        val nameLabel = Label(shortName, skin, Labels.TINY.skinKey).also {
            it.setAlignment(Align.center)
            it.wrap = true
        }
        nodeTable.add(nameLabel).width(NODE_SIZE - 8f).center().expandY().fillY()
        nodeLabels[upgrade] = nameLabel

        var lastClick = 0L
        nodeTable.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (!nodeTable.isVisible) return false
                val now = System.currentTimeMillis()
                if (now - lastClick <= DOUBLE_CLICK_MS) {
                    view.stage.fire(BuyBarnUpgradeEvent(upgrade))
                } else {
                    view.selectedUpgrade = upgrade
                    view.updateInfoPanel()
                }
                lastClick = now
                return true
            }
        })
        return nodeTable
    }

    // ── State application ───────────────────────────────────────────────────
    private fun applyStates(states: Map<BarnUpgrade, BarnUpgradeState>) {
        revealedNodes.clear()
        states.forEach { (upgrade, state) ->
            val node = nodeActors[upgrade] ?: return@forEach
            node.isVisible = state.isRevealed
            if (state.isRevealed) revealedNodes.add(upgrade)
            node.color = if (state.isMaxed) Color(0.5f, 0.5f, 0.5f, 1f) else Color.WHITE
        }
        updateInfoPanel()
        updateBuyButtonState()
    }

    private fun updateInfoPanel() {
        val upgrade = selectedUpgrade
        if (upgrade == null) {
            infoName.txt        = "Select an upgrade"
            infoDescription.txt = ""
            infoLevel.txt       = ""
            infoCost.txt        = ""
            infoBuyButton.isDisabled = true
            return
        }
        val state = model.upgradeStates[upgrade] ?: return
        infoName.txt        = upgrade.displayName
        infoDescription.txt = upgrade.description
        infoLevel.txt = when {
            upgrade.maxLevel == 1 -> if (state.isMaxed) "Purchased" else "One-time"
            else                  -> "Level ${state.level} / ${upgrade.maxLevel}"
        }
        infoCost.txt = if (state.isMaxed) "—" else "Cost: ${formatShort(state.cost)} gold"
        updateBuyButtonState()
    }

    private fun updateBuyButtonState() {
        val upgrade = selectedUpgrade ?: run { infoBuyButton.isDisabled = true; return }
        val state   = model.upgradeStates[upgrade] ?: run { infoBuyButton.isDisabled = true; return }
        infoBuyButton.isDisabled = state.isMaxed || goldCoins < state.cost
    }

    override fun clear() {
        super.clear()
        linesActor?.dispose()
    }

    companion object {
        private const val INFO_PANEL_HEIGHT = 130f
    }
}

@Scene2dDsl
fun <S> KWidget<S>.barnView(
    model: BarnViewModel,
    stage: Stage,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: BarnView.(S) -> Unit = {},
): BarnView = actor(BarnView(model, stage, skin), init)
