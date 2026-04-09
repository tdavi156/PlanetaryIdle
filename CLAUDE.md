# PlanetaryIdle — Claude Code Reference

## Workflow
Always present a plan and wait for explicit approval before making any code changes.

## Project
Idle/incremental game about producing resources to earn gold coins. Kotlin + LibGDX 1.13.1 + LibKTX 1.13.1-rc1 + Fleks 1.6-JVM ECS. Targets Desktop (LWJGL3) and Android.

## Modules
- **core** — All shared game logic and UI
- **lwjgl3** — Desktop launcher and config (1440×900, vsync)
- **android** — Android launcher
- **html** — GWT web target (mostly unused)

## Build & Run
```bash
./gradlew lwjgl3:run          # Run desktop
./gradlew core:compileKotlin  # Compile check (faster than full build)
./gradlew lwjgl3:jar          # Runnable JAR → lwjgl3/build/libs/
./gradlew android:run         # Run on connected device/emulator
```

## Architecture

### ECS (Fleks)
- **Components** — Pure data, no logic: `ResourceComponent`, `UpgradeComponent`, `AchievementComponent`, `ConfigurationComponent`
- **Systems** — Process entities:
  - `InitializeGameSystem` — Loads save data from preferences, creates entities
  - `ResourceUpdateSystem` — Fixed 60fps tick; advances resource production, applies upgrade multipliers
  - `RenderSystem` — Calls `IsometricMapRenderer.render()` then delegates to LibGDX Stage
  - `SettingsSystem` — Holds `Settings` data class at runtime (masterVolume, musicVolume, effectsVolume)
  - `AudioSystem` — `IntervalSystem` + `EventListener`; deferred sound queue; starts background music on first tick; reads volume from `SettingsSystem`

### UI (MVC-inspired)
- **Models** (`ui/models/`) — Game state + business logic; extend `PropertyChangeSource`; listen to ECS events; notify views via `onPropertyChange`
- **Views** (`ui/views/`) — Scene2D `Table` subclasses; bind to model properties in `init`
- **Skin** (`ui/Skin.kt`) — Centralized LibGDX skin; enums for `Buttons`, `Labels`, `Drawables`

### View Navigation
`ViewState` enum (`ui/ViewState.kt`): `FARM, BARN, KITCHEN, ACHIEVEMENTS, STATISTICS, SETTINGS`.
Menu buttons fire `ViewStateChangeEvent(state)`. `BackgroundView` and `IsometricMapRenderer` both listen to this event.

### Rendering — Background & Isometric Map
- **`BackgroundView`** — dynamic; shows `graphics/barn_background.png` or `graphics/kitchen_background.png` per state; grey fallback for views without imagery; `null` (transparent) for FARM so the tile map shows through
- **`IsometricMapRenderer`** (`rendering/`) — separate `SpriteBatch` + `OrthographicCamera`; loads `graphics/farm_map.tmx` (graceful no-op if missing); renders before `stage.draw()` only when `ViewState == FARM`; toggles TMX layer visibility on `BuyResourceEvent` and `ResourceUpdateEvent`

### TMX Layer Convention (`farm_map.tmx`)
`layer_base` (always on), `layer_red`, `layer_orange`, `layer_yellow`, `layer_green`, `layer_blue`, `layer_purple`, `layer_pink`, `layer_brown`, `layer_white`, `layer_black`, `layer_barn`, `layer_kitchen` — names match `ResourceComponent` color enum values (lowercase).

### Barn Upgrade Tree
`BarnUpgrade` enum (`components/BarnUpgrade.kt`) defines 24 upgrades with node positions, costs, prerequisites, and category. `BarnViewModel` handles purchases, persists levels, and fires `BarnEffectsChangedEvent` with per-color payout multipliers, speed multiplier, and soil base multiplier. Barn unlocks on first green crop purchase (`BarnUnlockedEvent`). Soil upgrade lives in the Barn (root node); the FarmView soil button is removed. No barn upgrades reset on soil prestige.

### Audio System
`AudioSystem` is a Fleks `IntervalSystem` + `EventListener`. Sounds are queued during event handling and played on the next tick (prevents duplicate rapid sounds). Volume is applied at playback time from `SettingsSystem`.

**Triggered events → sounds:**
- `BuyResourceEvent` → `SFX_BUY_CROP`
- `BuyBarnUpgradeEvent` → `SFX_BUY_UPGRADE`
- `BarnUnlockedEvent` → `SFX_BARN_UNLOCKED`
- `AchievementCompletedEvent` → `SFX_ACHIEVEMENT`
- `KitchenUnlockedEvent` → `SFX_KITCHEN_UNLOCKED`
- Background music loop starts on first tick via `BACKGROUND_MUSIC`

**Adding audio files:** Set the path constants in `AudioSystem.companion object` (currently blank strings). Drop WAV files in `assets/audio/`.

**Settings persistence:** Volume keys `settings_master_volume`, `settings_music_volume`, `settings_effects_volume` stored in `planetaryIdlePrefs`. Loaded on startup by `SettingsModel`; saved when the user clicks Save in the Settings view.

### Settings View
`SettingsView` is a full-screen overlay added to the main stack. Opening: clicking Settings in `MenuView` fires `SettingsOpenEvent` + `ViewStateChangeEvent(SETTINGS)`. Closing: Save/Cancel in `SettingsView` fires `SettingsClosedEvent`; `MenuView` handles this and returns to `ViewState.FARM`.

### Kitchen System
Unlocks via `BarnUpgrade.KITCHEN`. Named crops replace raw color names on the farm rows.

**Crops:** 10 colors × 5 tiers = 50 crops defined in `CropRegistry`. T1 base values match the original `PlanetResources` enum. Active crop per color stored in `KitchenViewModel.activeCrops`; switching fires `ActiveCropChangedEvent` which updates `ResourceComponent.basePayout` and `cycleDuration` in `FarmModel`.

**Recipes:** Defined in `RecipeRegistry.all` (see Key Files above). `Recipe.combinedTime` = sum of crop times; `Recipe.combinedBaseValue` = product of crop values. Activating a recipe syncs both resource cycle durations to `combinedTime` and registers a pair in `FarmModel.activeRecipePairs`. Payout: first color fires → buffered in `recipePendingPayouts`; second fires → credits `payoutA × payoutB × achMult`. Multi-color (3+) recipe payout mechanics are not yet implemented; `FarmModel` currently pairs `crops[0]` and `crops[1]` only.

**Adding curated multi-color recipes:** Edit `CuratedRecipes.kt` — add `listOf("CropA", "CropB", "CropC")` entries. Rules: sequential colors only, max 2-tier gap. Registry normalizes order and deduplicates automatically.

**Research:** `KitchenViewModel.update(delta)` called from `KitchenView.act(delta)` each frame. Research jobs draw from `RecipeRegistry.all` for recipe discoveries and `CropRegistry` for crop unlocks. Discovery chance = `0.3 + avgTier×0.1`; 70% crop / 30% recipe split when both are available.

**Persistence keys (kitchen):** `kitchen_unlocked`, `kitchen_unlocked_crops_{color}`, `kitchen_active_crop_{color}`, `kitchen_discovered_recipes`, `kitchen_active_recipes`, `kitchen_researcher_count`, `kitchen_researcher_{i}_input_slots`, `kitchen_researcher_{i}_speed_level`.

### Event Flow
Events fired via `Stage.fire()`. Key events in `events/Events.kt`:
`BuyResourceEvent` → `FarmModel` → `ResourceUpdateEvent` → views update
`ViewStateChangeEvent` → `BackgroundView`, `IsometricMapRenderer`, `MenuView`
`BarnUnlockedEvent` → `MenuModel`, `IsometricMapRenderer` (enables `layer_barn`)
`BuyBarnUpgradeEvent` → `BarnViewModel` → `BarnEffectsChangedEvent` → `FarmModel`, `ResourceUpdateSystem`
`KitchenUnlockedEvent` → `KitchenViewModel`, `MenuModel`, `IsometricMapRenderer` (enables `layer_kitchen`)
`ActiveCropChangedEvent` → `FarmModel` (updates basePayout + cycleDuration on ResourceComponent)
`RecipeActivatedEvent` → `FarmModel` (links cycle durations, registers pair)
`RecipeDeactivatedEvent` → `FarmModel` (unlinks pair, restores individual durations)
`SettingsOpenEvent` → `SettingsModel` (syncs UI from `SettingsSystem`)
`SettingsClosedEvent` → `MenuView` (returns to Farm view)

### Persistence
LibGDX `Preferences` API. Keys: `gold_coins`, `{color}_owned/cost/value/rate/current_ticks/unlocked`, `soil_is_unlocked/upgrades/cost`, `ach1`–`ach22`, `barn_unlocked`, `barn_upgrade_{id}_level`, `settings_master_volume`, `settings_music_volume`, `settings_effects_volume`, plus kitchen keys listed in the Kitchen System section above.

## Game Resources
10 resources in order: Red → Orange → Yellow → Green → Blue → Purple → Pink → Brown → White → Black. Defined as enum in `ResourceComponent.kt`.

## Key Files
| File | Role |
|------|------|
| `PlanetaryIdle.kt` | KtxGame entry point; creates Stage, skin, GameScreen |
| `GameScreen.kt` | Builds Fleks World; wires systems and UI hierarchy |
| `FarmModel.kt` | Core game logic; gold coins, buy/sell, multipliers, save/load |
| `FarmView.kt` | Main farm UI; resource rows, buy buttons, production rate display |
| `BarnView.kt` / `BarnViewModel.kt` | Upgrade tree UI (node graph + info panel) and purchase logic |
| `BarnUpgrade.kt` | Enum of 24 upgrades; prerequisites map defines tree edges |
| `KitchenView.kt` / `KitchenViewModel.kt` | Kitchen UI and logic; crop tiers, recipe activation, research lab |
| `CropType.kt` / `CropRegistry` | `CropType(name, color, tier, baseValue, baseProductionTime)`; 50 crops (10 colors × 5 tiers); `forColor()`, `tier1()`, `byId()` helpers |
| `Recipe.kt` | `Recipe(crops: List<CropType>)`; `id`, `displayName`, `combinedTime`, `combinedBaseValue`; supports N-color recipes |
| `RecipeRegistry.kt` | Single source of truth for all recipes; `twoColorRecipes` (171 exhaustive, generated); `curatedRecipes` (parsed from `CuratedRecipes.kt`); `all` = combined deduplicated list |
| `CuratedRecipes.kt` | Hand-editable config: `val CURATED_RECIPES: List<List<String>>`; add multi-color recipes as lists of crop names; see inline format docs |
| `SettingsView.kt` / `SettingsModel.kt` | Volume controls (master/music/effects); save/cancel; persists to preferences |
| `AudioSystem.kt` | Sound queue + background music; volume from `SettingsSystem`; asset paths as blank constants |
| `SettingsSystem.kt` | Fleks system holding `Settings` data class at runtime |
| `configurations/Settings.kt` | Data class with volume fields and preference key constants |
| `IsometricMapRenderer.kt` | Loads farm_map.tmx; toggles layers; renders pre-stage |
| `BackgroundView.kt` | Dynamic background switcher (PNG or grey fallback) |
| `ResourceComponent.kt` | BigDecimal resource math; `tickCount` property drives production timing |
| `UpgradeComponent.kt` | Soil speed multiplier; `soilSpeedMultiplier` updated by `BarnEffectsChangedEvent` (Improved Soil Quality) |
| `AchievementComponent.kt` | 22 achievements; multiplier = 1.05^completedCount |
| `MenuView.kt` | Side menu: Farm · Barn · Kitchen · Achievements · Statistics · Settings · Reset · Quit |

## BigDecimal Division — Important
Always use the 3-arg `divide(divisor, scale, RoundingMode)` form. The Kotlin `/` operator on `BigDecimal` uses the unchecked 1-arg `divide()` which throws `ArithmeticException` on non-terminating decimals. This has caused recurring crashes.

```kotlin
// Wrong — throws for non-terminating results
val result = a / b

// Correct
val result = a.divide(b, 10, RoundingMode.HALF_UP)
```

## Constants
- `FRAMES_PER_SECOND_INT` / `FRAMES_PER_SECOND_FLOAT` — defined in `PlanetaryIdle.kt`
- `PLANETARY_EXPONENT = BigDecimal(308)` — max exponent for production rate display (in `FarmView.kt`)
