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
- **Systems** — `InitializeGameSystem` (save load), `ResourceUpdateSystem` (60fps production tick), `RenderSystem`, `SettingsSystem` (holds `Settings` data class), `AudioSystem` (deferred sound queue + music), `FloatingTextSystem` (animates payout labels farm→gold)

### UI (MVC-inspired)
- **Models** (`ui/models/`) — Extend `PropertyChangeSource`; listen to ECS events; notify views via `onPropertyChange`
- **Views** (`ui/views/`) — Scene2D `Table` subclasses; bind to model properties in `init`
- **Skin** (`ui/Skin.kt`) — Enums: `Buttons`, `Labels`, `Drawables`

### View Navigation
`ViewState` enum: `FARM, BARN, KITCHEN, CODEX, ACHIEVEMENTS, STATISTICS, SETTINGS`.
`MenuView` controls visibility; menu buttons fire `ViewStateChangeEvent`. `BackgroundView` and `IsometricMapRenderer` both listen to this event.

**Stub views (future, not in-game):** `AutomationView`, `ChallengesView`, `GalaxyView`, `ShopView`, `NotificationView`.

### Rendering
- **`BackgroundView`** — shows barn/kitchen backgrounds per state; grey fallback; transparent for FARM (tile map shows through)
- **`IsometricMapRenderer`** — loads `farm_map.tmx`; renders before stage only when `ViewState == FARM`; toggles color layers on buy/update events. Layer names: `layer_base`, `layer_{color}`, `layer_barn`, `layer_kitchen`

## Key Systems

### Barn
24 upgrades (`BarnUpgrade` enum) in a node-tree. Unlocks on first green crop purchase. `BarnViewModel` fires `BarnEffectsChangedEvent` with per-color payout multipliers, speed multiplier, and soil multiplier. Soil upgrade lives here (not FarmView). No barn upgrades reset on prestige.

### Achievements
57 achievements (`Achievements` enum in `AchievementComponent.kt`). Each grants ×1.05 multiplier (compounding; upgrades to ×1.06 via "The End"). 7 have special `AchievementBonus` effects:

| Achievement | Bonus |
|---|---|
| Red Giant (`red_250`) | Red production ×1.15 |
| Full Spectrum (`combined_full_spectrum`) | All production ×1.10 |
| Tilled Earth (`soil_5`) | Soil upgrade cost ×0.90 |
| Perfect Soil (`soil_25`) | Soil effectiveness ×1.5 → ×1.75 |
| Trillionaire! (`gold_1t`) | Gold income ×1.05 |
| The End (`gold_1e50`) | Achievement scale 1.05 → 1.06 |
| Master Builder (`barn_15`) | Research speed ×1.2 |

**IDs:** `red_10/50/250/500/1000/5000`; `<color>_10/100/1000` (orange–white); `black_1/10/100`; `combined_full_spectrum`; `gold_1m/1b/1t/1q/1e33/1e50`; `soil_1/5/10/25`; `barn_1/5/10/15`; `kitchen_unlock/crop_1/5/10/50/recipe_1/5/10/50`.
**Persistence:** `ach_<achId>`. Triggered from `FarmView`, `BarnViewModel`, and `KitchenViewModel` via `AchievementNotificationEvent`.
**AchievementsModel** computes live progress (fraction + text) for all 57 achievements. **AchievementsView** shows filter tabs (All/Completed/In Progress/Locked) and per-card progress bars.

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
BarnUnlockedEvent / KitchenUnlockedEvent → MenuModel, IsometricMapRenderer (layer toggle)
BuyBarnUpgradeEvent → BarnViewModel → BarnEffectsChangedEvent → FarmModel, ResourceUpdateSystem
ActiveCropChangedEvent → FarmModel (updates basePayout + cycleDuration)
RecipeActivatedEvent / RecipeDeactivatedEvent → FarmModel
AchievementNotificationEvent → AchievementsModel → AchievementCompletedEvent → FarmModel, KitchenViewModel, AchievementsModel
FloatingTextEvent → FloatingTextSystem (animates payout label farm row → gold display)
SettingsOpenEvent / SettingsClosedEvent → SettingsModel / MenuView
CreditGoldEvent → FarmModel (direct gold credit, used for recipe payouts)
```

## Persistence
All keys in `planetaryIdlePrefs` (LibGDX `Preferences`):
- **Resources:** `gold_coins`, `{color}_owned/cost/value/rate/current_ticks/unlocked`
- **Soil:** `soil_is_unlocked/upgrades/cost`
- **Achievements:** `ach_<achId>` (57 keys), `achievement_multiplier`, `achievement_multiplier_scale`, `bonus_*` flags
- **Barn:** `barn_unlocked`, `barn_upgrade_{id}_level`
- **Kitchen:** `kitchen_unlocked`, `kitchen_unlocked_crops_{color}`, `kitchen_active_crop_{color}`, `kitchen_discovered_recipes`, `kitchen_active_recipes`, `kitchen_researcher_count`, `kitchen_researcher_{i}_input_slots/speed_level`
- **Settings:** `settings_master_volume`, `settings_music_volume`, `settings_effects_volume`, `settings_number_notation`

## Key Files
| File | Role |
|------|------|
| `PlanetaryIdle.kt` | Entry point; creates Stage, skin, GameScreen |
| `GameScreen.kt` | Builds Fleks World; wires all systems and UI |
| `FarmModel.kt` | Core logic; gold, buy/sell, multipliers, save/load |
| `FarmView.kt` | Farm UI; resource rows, production display, next-milestone labels, achievement triggers |
| `BarnView.kt` / `BarnViewModel.kt` | Upgrade tree UI and purchase logic |
| `BarnUpgrade.kt` | Enum of 24 upgrades with costs, prerequisites, categories |
| `KitchenView.kt` / `KitchenViewModel.kt` | Kitchen UI and logic; crops, recipes, research |
| `CropType.kt` / `CropRegistry` | 50 crops (10 colors × 5 tiers) |
| `Recipe.kt` / `RecipeRegistry.kt` | Recipe definitions; `twoColorRecipes` + `curatedRecipes` → `all` |
| `CuratedRecipes.kt` | Hand-editable multi-color recipe config |
| `AchievementComponent.kt` | 57 achievements; `AchievementBonus` sealed class; multiplier math |
| `AchievementsModel.kt` / `AchievementsView.kt` | Progress computation + filtered card grid with progress bars |
| `CodexModel.kt` / `CodexView.kt` | Crop/recipe encyclopedia; relays from KitchenViewModel |
| `HeaderView.kt` | Gold display, achievement count button, `formatShort()` |
| `MenuView.kt` | Side menu: Farm · Barn · Kitchen · Codex · Achievements · Statistics · Settings |
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
