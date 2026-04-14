package com.github.jacks.planetaryIdle.events

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.data.Discovery
import com.github.jacks.planetaryIdle.data.Recipe
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.ui.ViewState
import java.math.BigDecimal

fun Stage.fire(event : Event) {
    this.root.fire(event)
}

class InitializeGameEvent : Event()
class SaveGameEvent : Event()
class QuitGameEvent : Event()
class ResetGameEvent : Event()

class GameCompletedEvent : Event()

class ResourceUpdateEvent(val rscComp : ResourceComponent) : Event()
class BuyResourceEvent(val resourceType : String) : Event()
class UpdateBuyAmountEvent(val amount : Float) : Event()
class UpgradeSoilEvent(val amount : BigDecimal = BigDecimal(1)) : Event()

class AchievementNotificationEvent(val achId: String = "") : Event()
class AchievementCompletedEvent(val achId: String = "") : Event()

class FloatingTextEvent(val startPosition: Vector2, val targetPosition: Vector2, val amount: BigDecimal, val displayText: String) : Event()
class CreditGoldEvent(val amount: BigDecimal) : Event()

class ViewStateChangeEvent(val state: ViewState) : Event()

class BarnUnlockedEvent : Event()
class BuyBarnUpgradeEvent(val upgrade: com.github.jacks.planetaryIdle.data.BarnUpgrade) : Event()
class BarnEffectsChangedEvent(
    /** Per-resource-name payout multiplier (e.g. "red" → 1.331 for 3 levels of Red Value). */
    val payoutMultipliers: Map<String, BigDecimal>,
    /** Speed multiplier from Improved Seeds applied on top of soil multiplier. */
    val speedMultiplier: BigDecimal,
    /** New base per-level soil speed multiplier (modified by Improved Soil Quality). */
    val soilBaseMultiplier: BigDecimal,
) : Event()

// ── Kitchen events ────────────────────────────────────────────────────────────

class KitchenUnlockedEvent : Event()

/**
 * Fired when the player switches to a different named crop for a color.
 * FarmModel listens and updates the ResourceComponent + resets owned count.
 */
class ActiveCropChangedEvent(
    val color: String,
    val cropName: String,
    val newBasePayout: BigDecimal,
    val newCycleDuration: BigDecimal,
) : Event()

/** Fired when a recipe is activated (set). FarmModel links the two color rows. */
class RecipeActivatedEvent(val recipe: Recipe) : Event()

/** Fired when a recipe is deactivated (cleared). FarmModel unlinks the two color rows. */
class RecipeDeactivatedEvent(val recipe: Recipe) : Event()

/**
 * Fired when a research job completes.
 * Exactly one of discoveredCropId / discoveredRecipeId will be non-null on success;
 * both null means the roll failed.
 */
class ResearchCompleteEvent(
    val researcherIndex: Int,
    val discoveredCropId: String?,
    val discoveredRecipeId: String?,
) : Event()

/** Fired when a new crop type is unlocked via research. Used by BarnViewModel for expertise. */
class CropUnlockedEvent(val color: String, val cropName: String) : Event()

// ── Observatory events ────────────────────────────────────────────────────────

class ObservatoryUnlockedEvent : Event()

/** Fired by ObservatorySystem once per second as a heartbeat for Insight generation. */
class InsightTickEvent : Event()

/** Fired when the player purchases a Discovery. */
class DiscoveryPurchasedEvent(val discovery: Discovery) : Event()

/**
 * Fired by ObservatoryViewModel whenever purchased discoveries change.
 * FarmModel and ResourceUpdateSystem consume the multipliers.
 */
data class ObservatoryEffects(
    /** Combined direct production multiplier from all applicable discoveries. */
    val productionMultiplier: BigDecimal,
    /** Cycle-speed multiplier (Orbital Survey). */
    val cycleSpeedMultiplier: BigDecimal,
    /** Recipe-payout multiplier (Binary Influence). */
    val recipePayoutMultiplier: BigDecimal,
    /** Additive bonus to soil base multiplier (Gravity Well). */
    val soilEffectivenessBonus: BigDecimal,
    /** Insight-generation multiplier (Solar Current, Abyssal Lens, Luminous Veil, Cascade). */
    val insightMultiplier: BigDecimal,
    /** Per-color production multiplier from Spectrum Analysis + Stellar Cartography. */
    val perColorMultipliers: Map<String, BigDecimal>,
    /** When true (Resonance Field active), achievement multiplier is applied twice. */
    val achievementMultiplierSquared: Boolean,
)

class ObservatoryEffectsChangedEvent(val effects: ObservatoryEffects) : Event()

// ── Automation events ─────────────────────────────────────────────────────────

/** Fired by BarnViewModel when AUTOMATION_BASIC is purchased for the first time. */
class AutomationUnlockedEvent : Event()

/** Fired by AutomationSystem each automation tick; AutomationModel handles the buy logic. */
class AutomationTickEvent : Event()

/** Fired by ObservatoryViewModel when the MARKET_ANALYSIS discovery is purchased. */
class SmartBuyUnlockedEvent : Event()

/** Fired by KitchenViewModel when an auto-researcher completes with no new discovery. */
class ResearchExhaustedEvent(val researcherIndex: Int) : Event()

// ── Settings events ───────────────────────────────────────────────────────────

class SettingsOpenEvent : Event()
class SettingsClosedEvent : Event()
