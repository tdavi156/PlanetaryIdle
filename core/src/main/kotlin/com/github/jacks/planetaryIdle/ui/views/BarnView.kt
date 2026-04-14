package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
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
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton

private const val NODE_SIZE       = 80f
private const val CANVAS_WIDTH    = 2500f
private const val CANVAS_HEIGHT   = 900f
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

    private lateinit var infoName:        Label
    private lateinit var infoDescription: Label
    private lateinit var infoLevel:       Label
    private lateinit var infoCost:        Label
    private lateinit var infoBuyButton:   TextButton

    private var selectedUpgrade: BarnUpgrade? = null
    private var goldCoins = model.farmModel.goldCoins

    init {
        setFillParent(true)
        val view = this@BarnView

        // ── Canvas (nodes + lines) inside a ScrollPane ────────────────────
        val canvasGroup = Group().apply { setSize(CANVAS_WIDTH, CANVAS_HEIGHT) }

        BarnUpgrade.entries.forEach { upgrade ->
            val node = view.buildNodeActor(upgrade)
            view.nodeActors[upgrade] = node
            canvasGroup.addActor(node)
        }

        val lines = ConnectionLinesActor(nodeActors, { revealedNodes })
        linesActor = lines
        canvasGroup.addActorAt(0, lines)
        lines.setSize(CANVAS_WIDTH, CANVAS_HEIGHT)

        val scrollPane = ScrollPane(canvasGroup).apply {
            setScrollingDisabled(false, false)
            setOverscroll(false, false)
        }

        // ── Info panel at bottom ─────────────────────────────────────────
        val infoPanel = table { panelCell ->
            background = view.skin[Drawables.BACKGROUND_GREY]

            table { leftCell ->
                view.infoName = label("Select an upgrade", Labels.MEDIUM.skinKey) { cell ->
                    cell.left().padLeft(10f).row()
                    setAlignment(Align.left)
                }
                view.infoDescription = label("", Labels.SMALL.skinKey) { cell ->
                    cell.left().padLeft(10f).width(500f).row()
                    setAlignment(Align.topLeft)
                    wrap = true
                }
                leftCell.top().left().expandX().fillX().pad(5f)
            }

            table { rightCell ->
                view.infoLevel = label("", Labels.SMALL.skinKey) { cell ->
                    cell.right().padRight(10f).row()
                }
                view.infoCost = label("", Labels.SMALL.skinKey) { cell ->
                    cell.right().padRight(10f).row()
                }
                view.infoBuyButton = textButton("Buy", Buttons.GREEN_BUTTON_MEDIUM.skinKey) { cell ->
                    cell.right().width(140f).height(45f).pad(5f, 5f, 5f, 10f)
                    isDisabled = true
                    addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) {
                            view.selectedUpgrade?.let { upgrade ->
                                view.stage.fire(BuyBarnUpgradeEvent(upgrade))
                            }
                        }
                    })
                }
                rightCell.top().right().pad(5f)
            }

            panelCell.expandX().fillX().height(INFO_PANEL_HEIGHT)
        }

        add(scrollPane).expand().fill()
        row()
        add(infoPanel).expandX().fillX().height(INFO_PANEL_HEIGHT)

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
        nodeTable.add(nameLabel).width(NODE_SIZE - 4f).center().expandY().fillY()
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
        private const val INFO_PANEL_HEIGHT = 140f
    }
}

@Scene2dDsl
fun <S> KWidget<S>.barnView(
    model: BarnViewModel,
    stage: Stage,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: BarnView.(S) -> Unit = {},
): BarnView = actor(BarnView(model, stage, skin), init)
