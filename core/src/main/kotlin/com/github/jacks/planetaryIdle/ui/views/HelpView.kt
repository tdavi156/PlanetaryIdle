package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.HelpSection
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.models.HelpViewModel
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor

/**
 * The Help view.
 *
 * Layout:
 *   Top:   Tab bar (General | Planetary | Galaxy🔒 | Universe🔒 | Dimension🔒)
 *   Body:  Left glossary panel (220px) | vertical separator | Right content panel
 *
 * The General tab shows a scrollable glossary of HelpSection entries that unlock
 * progressively as the player advances. Each entry shows a green dot while unread.
 * The Planetary tab unlocks on GameCompletedEvent and shows a completion message.
 * The remaining three tabs are permanently locked placeholders for future phases.
 */
class HelpView(
    private val viewModel: HelpViewModel,
    skin: Skin,
) : Table(skin), KTable {

    // -------------------------------------------------------------------------
    // Inner enum for tabs
    // -------------------------------------------------------------------------

    private enum class HelpTab(val label: String) {
        GENERAL("General"),
        PLANETARY("Planetary"),
        GALAXY("Galaxy"),
        UNIVERSE("Universe"),
        DIMENSION("Dimension"),
    }

    // -------------------------------------------------------------------------
    // Shared drawables (created once, reused)
    // -------------------------------------------------------------------------

    private val glossaryPanelBg:  TextureRegionDrawable
    private val contentHeaderBg:  TextureRegionDrawable
    private val separatorDrawable: TextureRegionDrawable
    private val selectedRowBg:    TextureRegionDrawable

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private var activeTab     = HelpTab.GENERAL
    private var selectedSection: HelpSection? = null

    // -------------------------------------------------------------------------
    // Widget references updated on rebuild
    // -------------------------------------------------------------------------

    private val tabButtons       = mutableMapOf<HelpTab, TextButton>()
    private val glossaryItemRows = mutableListOf<Pair<HelpSection, Table>>()  // section → its row table

    // Panels rebuilt on tab/unlock changes
    private val glossaryInner  = Table(skin)   // content inside the glossary ScrollPane
    private val contentTitle   = Label("", skin, Labels.LARGE.skinKey)
    private val contentBody    = Label("", skin, Labels.SMALL.skinKey)

    // Top-level structural tables (built once)
    private val tabBar         = Table(skin)
    private val bodyTable      = Table(skin)
    private val glossaryPanel  = Table(skin)
    private val contentPanel   = Table(skin)

    // -------------------------------------------------------------------------
    // Init
    // -------------------------------------------------------------------------

    init {
        setFillParent(true)

        // Build shared drawables
        glossaryPanelBg   = solidDrawable(0.17f, 0.17f, 0.17f, 1f)
        contentHeaderBg   = solidDrawable(0.13f, 0.13f, 0.13f, 1f)
        separatorDrawable = solidDrawable(0.10f, 0.10f, 0.10f, 1f)
        selectedRowBg     = solidDrawable(0.26f, 0.38f, 0.55f, 1f)   // subtle blue highlight

        buildTabBar()
        buildBodyLayout()

        add(tabBar).expandX().fillX().height(TAB_HEIGHT)
        row()
        add(bodyTable).expand().fill()

        // Show the General tab with the Welcome section by default
        showTab(HelpTab.GENERAL)
        selectSection(HelpSection.WELCOME)

        // Rebuild glossary whenever unlock state or read state changes
        viewModel.onPropertyChange(HelpViewModel::updateCount) {
            refreshGlossary()
        }

        // Enable / disable the Planetary tab when the game is completed
        viewModel.onPropertyChange(HelpViewModel::planetaryUnlocked) { unlocked ->
            tabButtons[HelpTab.PLANETARY]?.isDisabled = !unlocked
        }
    }

    // -------------------------------------------------------------------------
    // Tab bar
    // -------------------------------------------------------------------------

    private fun buildTabBar() {
        tabBar.background = contentHeaderBg

        HelpTab.entries.forEach { tab ->
            val btn = TextButton(tab.label, skin, Buttons.GREY_BUTTON_MEDIUM.skinKey)

            // Lock tabs that are not accessible
            val initiallyLocked = when (tab) {
                HelpTab.GENERAL    -> false
                HelpTab.PLANETARY  -> !viewModel.planetaryUnlocked
                HelpTab.GALAXY,
                HelpTab.UNIVERSE,
                HelpTab.DIMENSION  -> true
            }
            btn.isDisabled = initiallyLocked

            btn.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    if (!btn.isDisabled && tab != activeTab) {
                        showTab(tab)
                    }
                }
            })

            tabButtons[tab] = btn
            tabBar.add(btn).expandX().fillX().height(TAB_HEIGHT).minHeight(0f).minWidth(0f).pad(2f, 2f, 0f, 2f).prefWidth(0f)
        }
    }

    // -------------------------------------------------------------------------
    // Body layout (left glossary + separator + right content)
    // -------------------------------------------------------------------------

    private fun buildBodyLayout() {
        // ---- Left glossary panel ----
        glossaryPanel.background = glossaryPanelBg

        val glossaryScroll = ScrollPane(glossaryInner, skin)
        glossaryScroll.setScrollingDisabled(true, false)
        glossaryScroll.setFadeScrollBars(true)

        glossaryPanel.add(glossaryScroll).expand().fill().pad(4f)

        // ---- Vertical separator ----
        val separatorImage = Table(skin)
        separatorImage.background = separatorDrawable

        // ---- Right content panel ----
        buildContentPanel()

        bodyTable.add(glossaryPanel).width(GLOSSARY_WIDTH).fillY()
        bodyTable.add(separatorImage).width(2f).fillY()
        bodyTable.add(contentPanel).expand().fill()
    }

    // -------------------------------------------------------------------------
    // Content panel (title header + scrollable body)
    // -------------------------------------------------------------------------

    private fun buildContentPanel() {
        contentPanel.clear()

        // Header row
        val headerTable = Table(skin)
        headerTable.background = contentHeaderBg
        contentTitle.setWrap(false)
        headerTable.add(contentTitle).expandX().left().pad(10f, 18f, 10f, 18f)

        // Separator line under header
        val headerLine = Table(skin)
        headerLine.background = separatorDrawable

        // Body scroll
        contentBody.setWrap(true)

        val bodyInner = Table(skin)
        bodyInner.add(contentBody).expandX().fillX().top().left().pad(16f, 18f, 16f, 18f)
        bodyInner.top()

        val bodyScroll = ScrollPane(bodyInner, skin)
        bodyScroll.setScrollingDisabled(true, false)
        bodyScroll.setFadeScrollBars(true)

        contentPanel.add(headerTable).expandX().fillX().height(CONTENT_HEADER_HEIGHT)
        contentPanel.row()
        contentPanel.add(headerLine).expandX().fillX().height(2f)
        contentPanel.row()
        contentPanel.add(bodyScroll).expand().fill()
    }

    // -------------------------------------------------------------------------
    // Tab switching
    // -------------------------------------------------------------------------

    private fun showTab(tab: HelpTab) {
        activeTab = tab

        // Update tab button visual state (active = light-blue style, others = grey)
        tabButtons.forEach { (t, btn) ->
            if (!btn.isDisabled) {
                btn.style = skin.get(
                    if (t == activeTab) Buttons.LIGHT_BLUE_BUTTON_MEDIUM.skinKey
                    else Buttons.GREY_BUTTON_MEDIUM.skinKey,
                    TextButton.TextButtonStyle::class.java
                )
            }
        }

        when (tab) {
            HelpTab.GENERAL -> {
                glossaryPanel.isVisible = true
                refreshGlossary()
                // Re-select the previously selected section, or default to WELCOME
                val toSelect = selectedSection?.let {
                    if (viewModel.getUnlockedSections().contains(it)) it else HelpSection.WELCOME
                } ?: HelpSection.WELCOME
                selectSection(toSelect)
            }
            HelpTab.PLANETARY -> {
                glossaryPanel.isVisible = false
                contentTitle.setText("Phase 1: Planetary")
                contentBody.setText(PLANETARY_CONTENT)
            }
            HelpTab.GALAXY, HelpTab.UNIVERSE, HelpTab.DIMENSION -> {
                // These tabs are permanently disabled — this branch is unreachable
                // but kept for completeness.
            }
        }
    }

    // -------------------------------------------------------------------------
    // Glossary rebuild
    // -------------------------------------------------------------------------

    private fun refreshGlossary() {
        glossaryInner.clear()
        glossaryItemRows.clear()

        val sections = viewModel.getUnlockedSections()

        sections.forEachIndexed { index, section ->
            val rowTable = buildGlossaryRow(section)
            glossaryItemRows.add(section to rowTable)
            glossaryInner.add(rowTable).expandX().fillX().height(GLOSSARY_ROW_HEIGHT).minHeight(0f).pad(
                if (index == 0) 4f else 2f, 4f, 2f, 4f
            )
            glossaryInner.row()
        }

        // Locked sections
        val lockedSections = HelpSection.entries.filter { it !in sections }
        lockedSections.forEach { section ->
            val lockedBtn = TextButton(section.title, skin, Buttons.GREY_BUTTON_MEDIUM.skinKey)
            lockedBtn.isDisabled = true
            glossaryInner.add(lockedBtn).expandX().fillX().height(GLOSSARY_ROW_HEIGHT).minHeight(0f).pad(2f, 4f, 2f, 4f)
            glossaryInner.row()
        }

        // Highlight the currently selected section after rebuild
        refreshSelectedHighlight()
    }

    private fun buildGlossaryRow(section: HelpSection): Table {
        val rowTable = Table(skin)
        val isUnread = viewModel.isUnread(section.id)

        val btn = TextButton(section.title, skin, Buttons.GREY_BUTTON_MEDIUM.skinKey)
        btn.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                selectSection(section)
            }
        })

        // Green dot indicator for unread sections
        val dotLabel = Label(if (isUnread) "●" else "", skin, Labels.GREEN.skinKey)

        rowTable.add(btn).expandX().fillX().height(GLOSSARY_ROW_HEIGHT)
        rowTable.add(dotLabel).width(DOT_WIDTH).center().padLeft(2f)

        return rowTable
    }

    // -------------------------------------------------------------------------
    // Section selection
    // -------------------------------------------------------------------------

    private fun selectSection(section: HelpSection) {
        selectedSection = section
        viewModel.markRead(section.id)

        contentTitle.setText(section.title)
        contentBody.setText(section.content)

        refreshSelectedHighlight()
    }

    private fun refreshSelectedHighlight() {
        glossaryItemRows.forEach { (section, rowTable) ->
            val isSelected = section == selectedSection
            rowTable.background = if (isSelected) selectedRowBg else null
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun solidDrawable(r: Float, g: Float, b: Float, a: Float): TextureRegionDrawable {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            drawPixel(0, 0, Color.rgba8888(r, g, b, a))
        }
        return TextureRegionDrawable(Texture(pixmap)).also { pixmap.dispose() }
    }

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    companion object {
        private const val TAB_HEIGHT           = 40f
        private const val GLOSSARY_WIDTH       = 220f
        private const val GLOSSARY_ROW_HEIGHT  = 40f
        private const val CONTENT_HEADER_HEIGHT = 52f
        private const val DOT_WIDTH            = 20f

        private val PLANETARY_CONTENT = """
Congratulations — you have reached the end of Phase 1: Planetary!

You have built a planetary-scale production operation from the ground up. Through crops, the Barn, the Kitchen, and the Observatory, you have pushed your gold production to astronomical levels and completed the first chapter of Planetary Idle.

This is the current end of the implemented content. Phase 2: Galaxy and beyond are in development. Stay tuned for future updates that will expand the game with entirely new mechanics, currencies, and progression systems.

Thank you for playing!
        """.trimIndent()
    }
}

@Scene2dDsl
fun <S> KWidget<S>.helpView(
    viewModel: HelpViewModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: HelpView.(S) -> Unit = {},
): HelpView = actor(HelpView(viewModel, skin), init)
