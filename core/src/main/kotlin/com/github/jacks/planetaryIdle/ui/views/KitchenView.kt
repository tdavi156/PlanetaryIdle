package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.jacks.planetaryIdle.components.CropRegistry
import com.github.jacks.planetaryIdle.components.CropType
import com.github.jacks.planetaryIdle.components.Recipe
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.Drawables
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.get
import com.github.jacks.planetaryIdle.ui.models.KitchenViewModel
import com.github.jacks.planetaryIdle.ui.models.ResearcherState
import com.github.jacks.planetaryIdle.ui.views.HeaderView.Companion.formatShort
import ktx.actors.txt
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.scrollPane
import ktx.scene2d.stack
import ktx.scene2d.table
import ktx.scene2d.textButton

class KitchenView(
    private val model: KitchenViewModel,
    skin: Skin,
) : Table(skin), KTable {

    private lateinit var researcherContainer: Table
    private lateinit var recipeContainer: Table
    private lateinit var hireBtn: TextButton

    private data class ResearcherWidgets(
        val selectedCrops: MutableList<CropType?>,
        val cropButtons: List<TextButton>,
        val chanceLabel: Label,
        val durationLabel: Label,
        val progressFill: Image,
        val startCancelButton: TextButton,
    )

    private val researcherWidgets = mutableMapOf<Int, ResearcherWidgets>()

    private val dimDrawable: TextureRegionDrawable = run {
        val px = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        px.setColor(Color(0f, 0f, 0f, 0.4f))
        px.fill()
        val t = Texture(px); px.dispose()
        TextureRegionDrawable(t)
    }

    init {
        setFillParent(true)
        val view = this@KitchenView

        table { outerCell ->
            // ── Left panel: Researchers ───────────────────────────────────
            table { leftCell ->
                label("Researchers", Labels.MEDIUM.skinKey) { c ->
                    c.expandX().left().pad(6f, 10f, 4f, 0f)
                }
                row()

                view.researcherContainer = Table(skin)
                add(view.researcherContainer).expandX().fillX().top().left()
                row()

                view.hireBtn = TextButton(view.hireButtonText(), skin, Buttons.GREY_BUTTON_MEDIUM.skinKey)
                view.hireBtn.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        view.model.hireResearcher()
                    }
                })
                add(view.hireBtn).left().pad(6f, 8f, 6f, 8f).height(40f)

                leftCell.expandY().fillY().width(780f).top().left()
            }

            // ── Divider ───────────────────────────────────────────────────
            image(skin[Drawables.BAR_BLACK_THIN]) { c ->
                c.fillY().width(2f)
            }

            // ── Right panel: Recipes ──────────────────────────────────────
            table { rightCell ->
                label("Recipes", Labels.MEDIUM.skinKey) { c ->
                    c.expandX().left().pad(6f, 10f, 4f, 0f)
                }
                row()

                view.recipeContainer = Table(skin)
                val recipeScroll = com.badlogic.gdx.scenes.scene2d.ui.ScrollPane(view.recipeContainer, skin)
                recipeScroll.setScrollingDisabled(true, false)
                add(recipeScroll).expand().fill().pad(4f)

                rightCell.expand().fill().top()
            }

            outerCell.expand().fill()
        }

        model.onPropertyChange(KitchenViewModel::researchers) { list ->
            rebuildResearcherPanel(list)
        }
        model.onPropertyChange(KitchenViewModel::discoveredRecipes) { _ ->
            rebuildRecipePanel()
        }
        model.onPropertyChange(KitchenViewModel::activeRecipes) { _ ->
            rebuildRecipePanel()
        }
        model.onPropertyChange(KitchenViewModel::unlockedCrops) { _ ->
            rebuildResearcherPanel(model.researchers)
        }

        rebuildResearcherPanel(model.researchers)
        rebuildRecipePanel()
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    override fun act(delta: Float) {
        super.act(delta)
        model.update(delta)
        updateProgressBars()
    }

    private fun updateProgressBars() {
        model.researchers.forEachIndexed { index, researcher ->
            val widgets = researcherWidgets[index] ?: return@forEachIndexed
            val job = researcher.activeJob
            if (job != null) {
                widgets.progressFill.scaleX = (job.elapsed / job.duration).coerceIn(0f, 1f)
                val remaining = ((job.duration - job.elapsed).coerceAtLeast(0f)).toInt()
                val mins = remaining / 60; val secs = remaining % 60
                widgets.startCancelButton.txt = "Cancel"
                widgets.chanceLabel.txt = "Chance: ${"%.0f".format(job.discoveryChance * 100f)}%"
                widgets.durationLabel.txt = if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
            } else {
                widgets.progressFill.scaleX = 0f
                widgets.startCancelButton.txt = "Start"
                updateResearcherChance(index)
            }
        }
    }

    private fun updateResearcherChance(index: Int) {
        val widgets = researcherWidgets[index] ?: return
        val selected = widgets.selectedCrops.filterNotNull()
        if (selected.isEmpty()) {
            widgets.chanceLabel.txt = "Chance: --"
            widgets.durationLabel.txt = "--"
        } else {
            val chance = model.computeDiscoveryChance(selected)
            val researcher = model.researchers.getOrNull(index)
            val rawDur = selected.sumOf { it.tier * it.tier } * KitchenViewModel.BASE_RESEARCH_SECONDS
            val dur = (rawDur / (researcher?.speedMultiplier ?: 1f)).toInt()
            val mins = dur / 60; val secs = dur % 60
            widgets.chanceLabel.txt = "Chance: ${"%.0f".format(chance * 100f)}%"
            widgets.durationLabel.txt = if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
        }
    }

    // ── Researcher panel ──────────────────────────────────────────────────────

    private fun rebuildResearcherPanel(list: List<ResearcherState>) {
        researcherContainer.clear()
        researcherWidgets.clear()
        list.forEachIndexed { index, researcher ->
            buildResearcherRow(index, researcher)
            val sep = Image(skin[Drawables.BAR_BLACK_THIN])
            researcherContainer.add(sep).expandX().fillX().height(1f).padTop(2f).padBottom(2f)
            researcherContainer.row()
        }
        researcherContainer.add().expandY()
        researcherContainer.invalidateHierarchy()
        // Update hire button cost (cost scales with current researcher count)
        if (::hireBtn.isInitialized) hireBtn.txt = hireButtonText()
    }

    private fun buildResearcherRow(index: Int, researcher: ResearcherState) {
        val selectedCrops = MutableList<CropType?>(KitchenViewModel.MAX_INPUT_SLOTS) { null }
        val cropButtons   = mutableListOf<TextButton>()

        val row = Table(skin)
        row.left()

        // Header label
        val headerLabel = Label("Researcher ${index + 1}", skin, Labels.SMALL.skinKey)
        row.add(headerLabel).left().pad(4f, 8f, 2f, 8f).colspan(6)
        row.row()

        // Crop slot buttons
        for (slotIdx in 0 until researcher.inputSlotCount) {
            val btn = TextButton("[+]", skin, Buttons.GREY_BUTTON_SMALL.skinKey)
            val capturedSlot = slotIdx
            btn.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    if (model.researchers.getOrNull(index)?.activeJob != null) return
                    showCropPicker(index, capturedSlot, selectedCrops, cropButtons)
                }
            })
            cropButtons.add(btn)
            row.add(btn).width(110f).height(36f).pad(2f, 4f, 2f, 4f)
        }
        // Locked slot placeholders
        for (slotIdx in researcher.inputSlotCount until KitchenViewModel.MAX_INPUT_SLOTS) {
            val locked = TextButton("[locked]", skin, Buttons.GREY_BUTTON_SMALL.skinKey)
            locked.isDisabled = true
            row.add(locked).width(110f).height(36f).pad(2f, 4f, 2f, 4f)
        }
        row.row()

        // Chance / duration labels
        val chanceLabel   = Label("Chance: --", skin, Labels.TINY.skinKey)
        val durationLabel = Label("--", skin, Labels.TINY.skinKey)
        row.add(chanceLabel).left().pad(2f, 8f, 0f, 8f)
        row.add(durationLabel).left().pad(2f, 8f, 0f, 8f)
        row.row()

        // Progress bar
        val barBase = Image(skin[Drawables.BAR_GREY_THICK])
        val fillImg = Image(skin[Drawables.BAR_GREEN_THICK]).apply { scaleX = 0f }
        val barStack = com.badlogic.gdx.scenes.scene2d.ui.Stack()
        barStack.add(barBase)
        barStack.add(fillImg)
        row.add(barStack).expandX().fillX().height(18f).pad(2f, 8f, 2f, 8f).colspan(4)
        row.row()

        // Start/Cancel + upgrade buttons
        val startCancel = TextButton("Start", skin, Buttons.GREEN_BUTTON_SMALL.skinKey)
        startCancel.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                val r = model.researchers.getOrNull(index) ?: return
                if (r.activeJob != null) {
                    model.cancelResearch(index)
                } else {
                    val inputs = selectedCrops.filterNotNull()
                    if (inputs.isNotEmpty()) model.startResearch(index, inputs)
                }
            }
        })
        row.add(startCancel).height(32f).pad(2f, 4f, 4f, 4f).minWidth(80f)

        val maxSlots = researcher.inputSlotCount >= KitchenViewModel.MAX_INPUT_SLOTS
        val slotLabel = if (maxSlots) "+Slot (max)" else "+Slot (${formatShort(model.slotUpgradeCost(index))}g)"
        val slotBtn = TextButton(slotLabel, skin, Buttons.GREY_BUTTON_SMALL.skinKey)
        slotBtn.isDisabled = maxSlots
        slotBtn.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) { model.upgradeResearcherSlots(index) }
        })
        row.add(slotBtn).height(32f).pad(2f, 2f, 4f, 2f).minWidth(100f)

        val speedLabel = "+Speed (${formatShort(model.speedUpgradeCost(researcher))}g)"
        val speedBtn = TextButton(speedLabel, skin, Buttons.GREY_BUTTON_SMALL.skinKey)
        speedBtn.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) { model.upgradeResearcherSpeed(index) }
        })
        row.add(speedBtn).height(32f).pad(2f, 2f, 4f, 4f).minWidth(110f)

        // Auto-research toggle — only visible when AUTOMATION_KITCHEN upgrade is purchased
        if (model.kitchenAutoUnlocked) {
            val autoEnabled = researcher.autoResearchEnabled
            val autoBtn = TextButton(if (autoEnabled) "Auto ON" else "Auto OFF", skin, Buttons.GREY_BUTTON_SMALL.skinKey)
            autoBtn.color = if (autoEnabled) com.badlogic.gdx.graphics.Color.GREEN else com.badlogic.gdx.graphics.Color.LIGHT_GRAY
            autoBtn.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    val current = model.researchers.getOrNull(index)?.autoResearchEnabled ?: false
                    model.setAutoResearch(index, !current)
                }
            })
            row.add(autoBtn).height(32f).pad(2f, 6f, 4f, 4f).minWidth(90f)
        }

        researcherContainer.add(row).expandX().fillX().left().pad(4f)
        researcherContainer.row()

        researcherWidgets[index] = ResearcherWidgets(
            selectedCrops     = selectedCrops,
            cropButtons       = cropButtons,
            chanceLabel       = chanceLabel,
            durationLabel     = durationLabel,
            progressFill      = fillImg,
            startCancelButton = startCancel,
        )
    }

    // ── Crop picker popup ─────────────────────────────────────────────────────

    private fun showCropPicker(
        researcherIndex: Int,
        slotIndex: Int,
        selectedCrops: MutableList<CropType?>,
        cropButtons: List<TextButton>,
    ) {
        val available = model.unlockedCrops.flatMap { (color, names) ->
            names.mapNotNull { name -> CropRegistry.forColor(color).find { it.cropName == name } }
        }.sortedWith(compareBy({ it.color }, { it.tier }))
        if (available.isEmpty()) return

        val popup = Table(skin)
        popup.setBackground(dimDrawable)
        popup.pad(6f)

        available.forEach { crop ->
            val btn = TextButton("${crop.cropName} (T${crop.tier})", skin, Buttons.GREY_BUTTON_SMALL.skinKey)
            btn.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    selectedCrops[slotIndex] = crop
                    cropButtons.getOrNull(slotIndex)?.txt = crop.cropName
                    popup.remove()
                    updateResearcherChance(researcherIndex)
                }
            })
            popup.add(btn).expandX().fillX().height(32f).pad(1f)
            popup.row()
        }

        val clearBtn = TextButton("[Clear]", skin, Buttons.GREY_BUTTON_SMALL.skinKey)
        clearBtn.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                selectedCrops[slotIndex] = null
                cropButtons.getOrNull(slotIndex)?.txt = "[+]"
                popup.remove()
                updateResearcherChance(researcherIndex)
            }
        })
        popup.add(clearBtn).expandX().fillX().height(32f).pad(1f)

        val stg = stage ?: return
        popup.pack()
        popup.setPosition(
            (stg.width / 2f - popup.width / 2f).coerceIn(0f, stg.width - popup.width),
            (stg.height / 2f - popup.height / 2f).coerceIn(0f, stg.height - popup.height),
        )
        stg.addActor(popup)
    }

    // ── Recipe panel ──────────────────────────────────────────────────────────

    private fun rebuildRecipePanel() {
        recipeContainer.clear()
        if (model.discoveredRecipes.isEmpty()) {
            recipeContainer.add(Label("No recipes discovered yet.", skin, Labels.SMALL.skinKey)).pad(10f)
            recipeContainer.row()
        } else {
            recipeContainer.add(Label("Discovered:", skin, Labels.SMALL.skinKey))
                .expandX().left().pad(4f, 6f, 2f, 6f)
            recipeContainer.row()
            // Determine best recipe by estimated payout for the star indicator
            val bestId = model.discoveredRecipes
                .maxByOrNull { model.estimatedRecipePayout(it) }?.id
            model.discoveredRecipes.forEach { recipe ->
                buildRecipeRow(recipe, isBest = recipe.id == bestId)
                recipeContainer.row()
            }
        }
        recipeContainer.add().expandY()
        recipeContainer.invalidateHierarchy()
    }

    private fun buildRecipeRow(recipe: Recipe, isBest: Boolean = false) {
        val isActive  = model.activeRecipes.any { it.id == recipe.id }
        val payout    = model.estimatedRecipePayout(recipe)
        val nameText  = if (isBest) "★ ${recipe.displayName}" else recipe.displayName

        val row = Table(skin)

        // Name — gold colour if best recipe
        val nameLabel = Label(nameText, skin, Labels.SMALL.skinKey)
        if (isBest) nameLabel.color = com.badlogic.gdx.graphics.Color.GOLD
        row.add(nameLabel).expandX().left().pad(2f, 6f, 2f, 4f)

        // Cycle time + estimated payout
        val infoLabel = Label(
            "%.1fs  ~%sg".format(recipe.combinedTime, formatShort(payout)),
            skin, Labels.TINY.skinKey
        )
        row.add(infoLabel).pad(2f, 0f, 2f, 4f)

        val toggleBtn = TextButton(if (isActive) "Clear" else "Set", skin, Buttons.GREY_BUTTON_SMALL.skinKey)
        toggleBtn.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (model.activeRecipes.any { it.id == recipe.id }) model.deactivateRecipe(recipe)
                else model.activateRecipe(recipe)
            }
        })
        row.add(toggleBtn).width(60f).height(30f).pad(2f, 4f, 2f, 6f)
        recipeContainer.add(row).expandX().fillX().pad(1f)
    }

    // ── Text helpers ──────────────────────────────────────────────────────────

    private fun hireButtonText(): String {
        val cost = formatShort(model.hireCost(model.researchers.size))
        return "+ Hire Researcher ($cost g)"
    }

    companion object {
        @Suppress("unused")
        private val log = ktx.log.logger<KitchenView>()
    }
}

@Scene2dDsl
fun <S> KWidget<S>.kitchenView(
    model: KitchenViewModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: KitchenView.(S) -> Unit = {},
): KitchenView = actor(KitchenView(model, skin), init)
