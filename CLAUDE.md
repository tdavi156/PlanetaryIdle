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
  - `RenderSystem` — Delegates to LibGDX Stage

### UI (MVC-inspired)
- **Models** (`ui/models/`) — Game state + business logic; extend `PropertyChangeSource`; listen to ECS events; notify views via `onPropertyChange`
- **Views** (`ui/views/`) — Scene2D `Table` subclasses; bind to model properties in `init`
- **Skin** (`ui/Skin.kt`) — Centralized LibGDX skin; enums for `Buttons`, `Labels`, `Drawables`

### Event Flow
Events fired via `Stage.fire()`. Key events in `events/Events.kt`:
`BuyResourceEvent` → `PlanetModel` → `ResourceUpdateEvent` → views update

### Persistence
LibGDX `Preferences` API. Keys: `gold_coins`, `{color}_owned/cost/value/rate/current_ticks/unlocked`, `soil_is_unlocked/upgrades/cost`, `ach1`–`ach22`.

## Game Resources
10 resources in order: Red → Orange → Yellow → Green → Blue → Purple → Pink → Brown → White → Black. Defined as enum in `ResourceComponent.kt`.

## Key Files
| File | Role |
|------|------|
| `PlanetaryIdle.kt` | KtxGame entry point; creates Stage, skin, GameScreen |
| `GameScreen.kt` | Builds Fleks World; wires systems and UI hierarchy |
| `PlanetModel.kt` | Core game logic; gold coins, buy/sell, multipliers, save/load |
| `ResourceComponent.kt` | BigDecimal resource math; `tickCount` property drives production timing |
| `UpgradeComponent.kt` | Soil upgrades; cost and speed multiplier (×2 per level, ×2.5 at max) |
| `AchievementComponent.kt` | 22 achievements; multiplier = 1.05^completedCount |
| `PlanetView.kt` | Main game UI; resource rows, buy buttons, production rate display |
| `MenuView.kt` | Side menu with tooltip system via `addHoverTooltip()` |

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
- `PLANETARY_EXPONENT = BigDecimal(308)` — max exponent for production rate display (in `PlanetView.kt`)
