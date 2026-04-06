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

### Event Flow
Events fired via `Stage.fire()`. Key events in `events/Events.kt`:
`BuyResourceEvent` → `FarmModel` → `ResourceUpdateEvent` → views update
`ViewStateChangeEvent` → `BackgroundView`, `IsometricMapRenderer`, `MenuView`

### Persistence
LibGDX `Preferences` API. Keys: `gold_coins`, `{color}_owned/cost/value/rate/current_ticks/unlocked`, `soil_is_unlocked/upgrades/cost`, `ach1`–`ach22`.

## Game Resources
10 resources in order: Red → Orange → Yellow → Green → Blue → Purple → Pink → Brown → White → Black. Defined as enum in `ResourceComponent.kt`.

## Key Files
| File | Role |
|------|------|
| `PlanetaryIdle.kt` | KtxGame entry point; creates Stage, skin, GameScreen |
| `GameScreen.kt` | Builds Fleks World; wires systems and UI hierarchy |
| `FarmModel.kt` | Core game logic; gold coins, buy/sell, multipliers, save/load |
| `FarmView.kt` | Main farm UI; resource rows, buy buttons, production rate display |
| `BarnView.kt` / `BarnViewModel.kt` | Stub — upgrades feature (next) |
| `KitchenView.kt` / `KitchenViewModel.kt` | Stub — recipes feature (future) |
| `IsometricMapRenderer.kt` | Loads farm_map.tmx; toggles layers; renders pre-stage |
| `BackgroundView.kt` | Dynamic background switcher (PNG or grey fallback) |
| `ResourceComponent.kt` | BigDecimal resource math; `tickCount` property drives production timing |
| `UpgradeComponent.kt` | Soil upgrades; cost and speed multiplier (×2 per level, ×2.5 at max) |
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
