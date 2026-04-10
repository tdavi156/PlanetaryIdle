# PlanetaryIdle — Claude Code Reference

## Workflow
Always present a plan and wait for explicit approval before making any code changes.

## Project
Idle/incremental game about producing resources to earn gold coins. Kotlin + LibGDX 1.13.1 + LibKTX 1.13.1-rc1 + Fleks 1.6-JVM ECS. Targets Desktop (LWJGL3) and Android.

## Modules
- **core** — All shared game logic and UI
- **lwjgl3** — Desktop launcher and config (1440×900, vsync)
- **android** — Android launcher

## Build & Run
```bash
./gradlew lwjgl3:run          # Run desktop
./gradlew core:compileKotlin  # Compile check (faster than full build)
./gradlew lwjgl3:jar          # Runnable JAR → lwjgl3/build/libs/
./gradlew android:run         # Run on connected device/emulator
```

## Architecture

### ECS (Fleks)
- **Components** — Pure data: `ResourceComponent`, `UpgradeComponent`, `AchievementComponent`, `ConfigurationComponent`
- **Systems** — `InitializeGameSystem` (save load), `ResourceUpdateSystem` (60fps production tick), `RenderSystem`, `SettingsSystem` (holds `Settings` data class), `AudioSystem` (deferred sound queue + music), `FloatingTextSystem` (animates payout labels farm→gold), `ObservatorySystem` (per-second Insight tick)

### UI (MVC-inspired)
- **Models** (`ui/models/`) — Extend `PropertyChangeSource`; listen to ECS events; notify views via `onPropertyChange`
- **Views** (`ui/views/`) — Scene2D `Table` subclasses; bind to model properties in `init`
- **Skin** (`ui/Skin.kt`) — Enums: `Buttons`, `Labels`, `Drawables`

### View Navigation
`ViewState` enum: `FARM, BARN, KITCHEN, CODEX, ACHIEVEMENTS, STATISTICS, SETTINGS, OBSERVATORY, HELP`.
`MenuView` controls visibility; menu buttons fire `ViewStateChangeEvent`. `BackgroundView` and `IsometricMapRenderer` both listen to this event.

**Stub views (future, not in-game):** `AutomationView`, `ChallengesView`, `GalaxyView`, `ShopView`.

### Rendering
- **`BackgroundView`** — shows barn/kitchen backgrounds per state; grey fallback; transparent for FARM (tile map shows through)
- **`IsometricMapRenderer`** — loads `farm_map.tmx`; renders before stage only when `ViewState == FARM`; toggles color layers on buy/update events. Layer names: `layer_base`, `layer_{color}`, `layer_barn`, `layer_kitchen`

## Key Systems

### Barn
25 upgrades (`BarnUpgrade` enum) in a node-tree. Unlocks on first green crop purchase. `BarnViewModel` fires `BarnEffectsChangedEvent` with per-color payout multipliers, speed multiplier, and soil multiplier. Soil upgrade lives here (not FarmView). No barn upgrades reset on prestige.

The `OBSERVATORY` upgrade (cost 1e20, requires KITCHEN) unlocks the Observatory building.

### Observatory
Unlocks via `BarnUpgrade.OBSERVATORY`. Uses **Insight** as its currency, earned passively based on total crop production rate: `rate^0.4` Insight/sec (capped at Double.MAX_VALUE).

**Discoveries** are 20 researchable items across 5 tiers. Purchasing discoveries in a tier unlocks the next tier (thresholds: 3 Common → Treasured, 3 Treasured → Legendary, 2 Legendary → Fabled, 2 Fabled → Mythical). Effects are applied globally via `ObservatoryEffectsChangedEvent`.

#### Discovery Tiers & Effects

| Tier | Color | Effects |
|---|---|---|
| **Common** | Grey | Small production multipliers, speed bonus, insight generation boost |
| **Treasured** | Green | Per-color multipliers, recipe payout boost, achievement multiplier bonus |
| **Legendary** | Blue | Large all-production multipliers, tick speed boost, compounding bonuses |
| **Fabled** | Purple | Massive multipliers, achievement multiplier squared, insight multiplier |
| **Mythical** | Gold | Endgame-tier bonuses: Cascade (×10⁵⁰ production), Singularity (insight ×10²⁰), Unified Principle (multiplies all other discoveries' effects) |

#### ObservatoryEffects (applied to FarmModel and ResourceUpdateSystem)
- `productionMultiplier: BigDecimal` — multiplied into all crop production
- `perColorMultipliers: Map<String, BigDecimal>` — per-color production multiplier
- `recipePayoutMultiplier: BigDecimal` — multiplied into kitchen recipe payouts
- `speedMultiplier: Float` — reduces crop cycle tick count (via ResourceUpdateSystem)
- `insightMultiplier: BigDecimal` — scales Insight generation rate
- `achievementMultiplierSquared: Boolean` — if true, FarmModel squares the achievement multiplier
- `soilEffectivenessBonus: BigDecimal` — additive bonus to barn soil effectiveness

#### Key classes
- `Discovery.kt` — `DiscoveryCategory` enum + `Discovery` enum (20 entries); `isCategoryUnlocked(purchased, category)` companion method; `prefKey` = `"observatory_discovery_{id}_purchased"`
- `ObservatorySystem.kt` — Fleks `IteratingSystem(Fixed(1/fps))`; fires `InsightTickEvent` once/sec
- `ObservatoryViewModel.kt` — handles `ObservatoryUnlockedEvent`, `InsightTickEvent`, `DiscoveryPurchasedEvent`, `CreditGoldEvent`, `AchievementCompletedEvent`, `BarnEffectsChangedEvent`, `CropUnlockedEvent`, `ResetGameEvent`; exposes `observatoryUnlocked`, `insight`, `insightPerSecond`, `discoveryStates`, `totalDiscoveriesPurchased`
- `ObservatoryView.kt` — scrollable list grouped by category; `refreshDiscoveryStates()` updates button text (Researched/Locked/Research) and name label colors

### Achievements
65 achievements (`Achievements` enum in `AchievementComponent.kt`). Each grants ×1.05 multiplier (compounding; upgrades to ×1.06 via "The End"). 7 have special `AchievementBonus` effects:

| Achievement | Bonus |
|---|---|
| Red Giant (`red_250`) | Red production ×1.15 |
| Full Spectrum (`combined_full_spectrum`) | All production ×1.10 |
| Tilled Earth (`soil_5`) | Soil upgrade cost ×0.90 |
| Perfect Soil (`soil_25`) | Soil effectiveness ×1.5 → ×1.75 |
| Trillionaire! (`gold_1t`) | Gold income ×1.05 |
| The End (`gold_1e50`) | Achievement scale 1.05 → 1.06 |
| Master Builder (`barn_15`) | Research speed ×1.2 |

**IDs:** `red_10/50/250/500/1000/5000`; `<color>_10/100/1000` (orange–white); `black_1/10/100`; `combined_full_spectrum`; `gold_1m/1b/1t/1q/1e33/1e50/1e75/1e100/1e150/1e200/1e308`; `soil_1/5/10/25`; `barn_1/5/10/15`; `kitchen_unlock/crop_1/5/10/50/recipe_1/5/10/50`; `observatory_unlock`; `discovery_1/5/10/15`; `insight_1b/1t`; `mythical_first`.
**Persistence:** `ach_<achId>`. Triggered from `FarmView`, `BarnViewModel`, `KitchenViewModel`, and `ObservatoryViewModel` via `AchievementNotificationEvent`.
**AchievementsModel** computes live progress (fraction + text) for all 65 achievements. **AchievementsView** shows filter tabs (All/Completed/In Progress/Locked) and per-card progress bars.

### Phase 1 Completion
Phase 1 ends when total crop production rate ≥ **1e308** (crops/sec). This fires `GameCompletedEvent`. The extended path to reach this milestone is powered by the Observatory's Discoveries system, which provides multiplicative bonuses large enough to bridge the gap from the Kitchen system's natural ceiling (~1e60–1e80).

Crop milestone multipliers extended to: ×200.0 at 5000 owned (in addition to existing ×1.2/×1.5/×2.0/×3.0 at 10/25/50/100).

### Kitchen
Unlocks via `BarnUpgrade.KITCHEN`. 50 crops (10 colors × 5 tiers) in `CropRegistry`. Recipes in `RecipeRegistry`: 171 exhaustive 2-color recipes + curated multi-color recipes from `CuratedRecipes.kt`. Research jobs discover new crop tiers and recipes over time. Recipe payout = `combinedBaseValue × achMult × bonuses`. `KitchenViewModel.estimatedRecipePayout(recipe)` is the shared payout estimator used by KitchenView and CodexView. **KitchenView** shows payout estimates and a ★ best-recipe indicator per recipe row.

### Codex
`CodexView` (unlocks with Kitchen) has two tabs:
- **Crops** — 10 color groups × 5 tier tiles; discovered = colored label, undiscovered = grey "???"
- **Recipes** — 9 adjacent-color-pair sections; discovered recipes with name + `~payout/cycle`; undiscovered count per section

### Settings
Volume sliders (master/music/effects) + number notation toggle (Letter / Scientific). Saved to preferences on Save; `HeaderView.useLetterNotation` updated immediately. `SettingsOpenEvent` / `SettingsClosedEvent` drive open/close; closing returns to FARM.

### Audio
Sounds queued during events, played next tick. Add WAV files to `assets/audio/` and set path constants in `AudioSystem.companion object`. Events: `BuyResourceEvent`, `BuyBarnUpgradeEvent`, `BarnUnlockedEvent`, `AchievementCompletedEvent`, `KitchenUnlockedEvent` → respective SFX constants.

## Event Flow
```
BuyResourceEvent → FarmModel → ResourceUpdateEvent → views
ViewStateChangeEvent → BackgroundView, IsometricMapRenderer, MenuView
BarnUnlockedEvent / KitchenUnlockedEvent → MenuModel, IsometricMapRenderer (layer toggle), HelpViewModel (unlocks sections + toast)
ObservatoryUnlockedEvent → HelpViewModel (unlocks Observatory section + toast)
GameCompletedEvent → HelpViewModel (unlocks Planetary tab)
BuyBarnUpgradeEvent → BarnViewModel → BarnEffectsChangedEvent → FarmModel, ResourceUpdateSystem
BarnUpgrade.OBSERVATORY purchase → BarnViewModel → ObservatoryUnlockedEvent → MenuModel, ObservatoryViewModel
ActiveCropChangedEvent → FarmModel (updates basePayout + cycleDuration)
RecipeActivatedEvent / RecipeDeactivatedEvent → FarmModel
AchievementNotificationEvent → AchievementsModel → AchievementCompletedEvent → FarmModel, KitchenViewModel, AchievementsModel
FloatingTextEvent → FloatingTextSystem (animates payout label farm row → gold display)
SettingsOpenEvent / SettingsClosedEvent → SettingsModel / MenuView
CreditGoldEvent → FarmModel (direct gold credit, used for recipe payouts) + ObservatoryViewModel (tracks total gold earned for insight)
InsightTickEvent → ObservatoryViewModel (generates insight, fires ObservatoryEffectsChangedEvent)
DiscoveryPurchasedEvent → ObservatoryViewModel → ObservatoryEffectsChangedEvent → FarmModel, ResourceUpdateSystem
ObservatoryEffectsChangedEvent → FarmModel (production/recipe multipliers, achievement squaring), ResourceUpdateSystem (speed multiplier)
```

## Persistence
All keys in `planetaryIdlePrefs` (LibGDX `Preferences`):
- **Resources:** `gold_coins`, `{color}_owned/cost/value/rate/current_ticks/unlocked`
- **Soil:** `soil_is_unlocked/upgrades/cost`
- **Achievements:** `ach_<achId>` (65 keys), `achievement_multiplier`, `achievement_multiplier_scale`, `bonus_*` flags
- **Barn:** `barn_unlocked`, `barn_upgrade_{id}_level`
- **Kitchen:** `kitchen_unlocked`, `kitchen_unlocked_crops_{color}`, `kitchen_active_crop_{color}`, `kitchen_discovered_recipes`, `kitchen_active_recipes`, `kitchen_researcher_count`, `kitchen_researcher_{i}_input_slots/speed_level`
- **Observatory:** `observatory_unlocked`, `observatory_insight`, `observatory_total_gold_earned`, `observatory_discovery_{id}_purchased` (20 keys)
- **Help:** `help_planetary_unlocked`, `help_read_{sectionId}` (10 keys, one per HelpSection)
- **Settings:** `settings_master_volume`, `settings_music_volume`, `settings_effects_volume`, `settings_number_notation`

## Key Files
| File | Role |
|------|------|
| `PlanetaryIdle.kt` | Entry point; creates Stage, skin, GameScreen |
| `GameScreen.kt` | Builds Fleks World; wires all systems and UI |
| `FarmModel.kt` | Core logic; gold, buy/sell, multipliers, save/load |
| `FarmView.kt` | Farm UI; resource rows, production display, next-milestone labels, achievement triggers; fires `GameCompletedEvent` at rate ≥ 1e308 |
| `BarnView.kt` / `BarnViewModel.kt` | Upgrade tree UI and purchase logic |
| `BarnUpgrade.kt` | Enum of 25 upgrades with costs, prerequisites, categories |
| `KitchenView.kt` / `KitchenViewModel.kt` | Kitchen UI and logic; crops, recipes, research |
| `CropType.kt` / `CropRegistry` | 50 crops (10 colors × 5 tiers) |
| `Recipe.kt` / `RecipeRegistry.kt` | Recipe definitions; `twoColorRecipes` + `curatedRecipes` → `all` |
| `CuratedRecipes.kt` | Hand-editable multi-color recipe config |
| `AchievementComponent.kt` | 65 achievements; `AchievementBonus` sealed class; multiplier math |
| `AchievementsModel.kt` / `AchievementsView.kt` | Progress computation + filtered card grid with progress bars |
| `CodexModel.kt` / `CodexView.kt` | Crop/recipe encyclopedia; relays from KitchenViewModel |
| `Discovery.kt` | `DiscoveryCategory` enum + `Discovery` enum (20 entries, 5 tiers); category unlock thresholds |
| `ObservatorySystem.kt` | Fleks system; fires `InsightTickEvent` once per second |
| `ObservatoryViewModel.kt` | Observatory state; insight generation, discovery purchases, effect computation |
| `ObservatoryView.kt` | Scrollable discovery list grouped by tier; insight header; buy/lock/researched state |
| `HeaderView.kt` | Gold display, achievement count button, `formatShort()` |
| `MenuView.kt` | Side menu: Farm · Barn · Kitchen · Codex · Achievements · Statistics · Help · Settings · Observatory; Help button has orange unread badge dot |
| `HelpSection.kt` | `HelpUnlockGroup` enum + `HelpSection` enum (10 entries) with id, title, content text, and unlock group |
| `HelpViewModel.kt` | Manages Help unlock state, unread tracking (persisted), toast messages, `planetaryUnlocked`; listens to Barn/Kitchen/Observatory/GameCompleted/Reset events |
| `HelpView.kt` | Two-panel Help view: tab bar (General/Planetary locked until completion/Galaxy+Universe+Dimension permanently locked), left glossary with green NEW dots, right scrollable content panel |
| `HelpToastView.kt` | Full-screen overlay; shows auto-dismissing dark pill toast at bottom-center on building unlocks |
| `SettingsView.kt` / `SettingsModel.kt` | Volume + notation controls |
| `AudioSystem.kt` | Deferred sound queue + background music |
| `IsometricMapRenderer.kt` | TMX map; layer toggling |
| `BackgroundView.kt` | Dynamic background per view state |
| `StatisticsView.kt` | Stub only |

## Number Formatting
`HeaderView.Companion.formatShort(number: BigDecimal): String` — shared across all views.
- **Letter** (default): K / M / B / T / Qa / Qi / Sx / Sp / Oc / No / Dc / Ud / Dd / Td / Qad / Qid / Sxd (1e3–1e51)
- **Scientific**: `%.2e` at or above 1K
Controlled by `HeaderView.useLetterNotation` companion var (set by Settings).

## BigDecimal Division — Critical
Always use the 3-arg form. The Kotlin `/` operator throws `ArithmeticException` on non-terminating decimals.
```kotlin
val result = a.divide(b, 10, RoundingMode.HALF_UP)  // correct
val result = a / b                                   // WRONG — crashes
```
