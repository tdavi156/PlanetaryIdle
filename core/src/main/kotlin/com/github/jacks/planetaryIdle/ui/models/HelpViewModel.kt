package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.events.AutomationUnlockedEvent
import com.github.jacks.planetaryIdle.events.BarnUnlockedEvent
import com.github.jacks.planetaryIdle.events.GameCompletedEvent
import com.github.jacks.planetaryIdle.events.KitchenUnlockedEvent
import com.github.jacks.planetaryIdle.events.ObservatoryUnlockedEvent
import com.github.jacks.planetaryIdle.events.ResetGameEvent
import com.github.jacks.planetaryIdle.ui.HelpSection
import com.github.jacks.planetaryIdle.ui.HelpUnlockGroup
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set

class HelpViewModel(stage: Stage) : PropertyChangeSource(), EventListener {

    private val prefs: Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }

    /** True when there are unlocked sections the player has not yet visited. Drives the badge dot in MenuView. */
    var hasUnread by propertyNotify(false)
        private set

    /** Set to a non-empty string to trigger a toast in HelpToastView. Reset to "" after display. */
    var toastMessage by propertyNotify("")
        private set

    /** True once the game has been completed. Unlocks the Planetary tab. */
    var planetaryUnlocked by propertyNotify(false)
        private set

    /**
     * Incremented whenever section availability or read state changes.
     * HelpView observes this to know when to rebuild the glossary.
     */
    var updateCount by propertyNotify(0)
        private set

    private val unlockedGroups = mutableSetOf(HelpUnlockGroup.ALWAYS)
    private val readSections   = mutableSetOf<String>()

    init {
        stage.addListener(this)
        loadState()
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Returns the sections that are currently available in the General tab glossary. */
    fun getUnlockedSections(): List<HelpSection> =
        HelpSection.entries.filter { it.unlockGroup in unlockedGroups }

    /** True if the section has been unlocked but not yet visited. */
    fun isUnread(sectionId: String): Boolean {
        val section = HelpSection.entries.find { it.id == sectionId } ?: return false
        return section.unlockGroup != HelpUnlockGroup.ALWAYS &&
               section.unlockGroup in unlockedGroups &&
               sectionId !in readSections
    }

    /** Called by HelpView when the player selects a section in the glossary. */
    fun markRead(sectionId: String) {
        if (readSections.add(sectionId)) {
            prefs.flush { this["help_read_$sectionId"] = true }
            updateHasUnread()
            updateCount++
        }
    }

    /** Called by HelpToastView at the end of the fade-out so a future identical message can re-trigger. */
    fun clearToastMessage() {
        toastMessage = ""
    }

    // -------------------------------------------------------------------------
    // Event handling
    // -------------------------------------------------------------------------

    override fun handle(event: Event): Boolean {
        when (event) {
            is BarnUnlockedEvent -> {
                unlockedGroups.add(HelpUnlockGroup.BARN)
                toastMessage = "The Barn is now open! Check Help for details."
                updateHasUnread()
                updateCount++
            }
            is AutomationUnlockedEvent -> {
                unlockedGroups.add(HelpUnlockGroup.AUTOMATION)
                toastMessage = "Automation is now available! Check Help for details."
                updateHasUnread()
                updateCount++
            }
            is KitchenUnlockedEvent -> {
                unlockedGroups.add(HelpUnlockGroup.KITCHEN)
                toastMessage = "The Kitchen is now open! Check Help for details."
                updateHasUnread()
                updateCount++
            }
            is ObservatoryUnlockedEvent -> {
                unlockedGroups.add(HelpUnlockGroup.OBSERVATORY)
                toastMessage = "The Observatory is now open! Check Help for details."
                updateHasUnread()
                updateCount++
            }
            is GameCompletedEvent -> {
                if (!planetaryUnlocked) {
                    planetaryUnlocked = true
                    prefs.flush { this["help_planetary_unlocked"] = true }
                }
            }
            is ResetGameEvent -> {
                unlockedGroups.retainAll(setOf(HelpUnlockGroup.ALWAYS))
                planetaryUnlocked = false
                toastMessage = ""   // reset so future unlock toasts can fire again
                prefs.flush { this["help_planetary_unlocked"] = false }
                updateHasUnread()
                updateCount++
            }
            else -> return false
        }
        return false  // let other listeners receive these events too
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun loadState() {
        // Restore unlock groups from existing save data
        if (prefs["barn_unlocked",        false]) unlockedGroups.add(HelpUnlockGroup.BARN)
        if (prefs["automation_unlocked",  false]) unlockedGroups.add(HelpUnlockGroup.AUTOMATION)
        if (prefs["kitchen_unlocked",     false]) unlockedGroups.add(HelpUnlockGroup.KITCHEN)
        if (prefs["observatory_unlocked", false]) unlockedGroups.add(HelpUnlockGroup.OBSERVATORY)

        // Restore planetary tab unlock
        if (prefs["help_planetary_unlocked", false]) planetaryUnlocked = true

        // Load per-section read state (only tracked for dynamically unlocked sections)
        HelpSection.entries
            .filter { it.unlockGroup != HelpUnlockGroup.ALWAYS }
            .forEach { section ->
                if (prefs["help_read_${section.id}", false]) {
                    readSections.add(section.id)
                }
            }

        updateHasUnread()
    }

    private fun updateHasUnread() {
        hasUnread = HelpSection.entries.any { section ->
            section.unlockGroup != HelpUnlockGroup.ALWAYS &&
            section.unlockGroup in unlockedGroups &&
            section.id !in readSections
        }
    }
}
