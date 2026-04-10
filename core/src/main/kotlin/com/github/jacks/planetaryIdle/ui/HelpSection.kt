package com.github.jacks.planetaryIdle.ui

enum class HelpUnlockGroup {
    ALWAYS,
    BARN,
    KITCHEN,
    OBSERVATORY
}

enum class HelpSection(
    val id: String,
    val title: String,
    val content: String,
    val unlockGroup: HelpUnlockGroup = HelpUnlockGroup.ALWAYS,
) {

    WELCOME(
        id = "welcome",
        title = "Welcome",
        content = """Welcome to the Help Menu!

This is your in-game reference for everything in Planetary Idle. If you ever want to know how something works, what a button does, or why a number is going up, this is the place to look.

As you progress and unlock new buildings and features, new entries will appear in this glossary. Keep an eye on the Help button in the side menu — a small indicator will appear whenever new information becomes available.

Use the glossary on the left to jump to any topic. New entries are marked with a green dot until you have visited them."""
    ),

    SETTINGS(
        id = "settings",
        title = "Settings",
        content = """Settings are accessible from the side menu at any time.

Number Notation
The game supports two display formats for large numbers:

  Letter (default) — Uses shorthand suffixes: K, M, B, T, Qa, Qi, Sx, Sp, Oc, No, Dc, and beyond. Keeps numbers compact and easy to read at a glance.

  Scientific — Displays numbers in standard scientific notation (e.g. 1.23e6). Useful if you prefer precise formatting.

You can switch between these at any time. The change takes effect immediately across the entire game.

Audio
Separate volume sliders are provided for Master, Music, and Effects audio. Adjust them to your preference and press Save to apply the changes."""
    ),

    GOLD(
        id = "gold",
        title = "Gold",
        content = """Gold is the primary currency of Planetary Idle. Nearly everything in the game revolves around earning and spending it.

Earning Gold
Gold is produced by your crops. Each crop completes production cycles over time, and when a cycle finishes the gold is deposited to your total. The more crops you own and the faster they cycle, the faster your gold accumulates.

Spending Gold
Gold is used to purchase and improve almost everything:

  Buying more of an existing crop
  Unlocking new crop colors
  Purchasing Barn upgrades
  Funding Observatory Discoveries

Your current gold total is always displayed in the header bar at the top of the screen."""
    ),

    CROPS(
        id = "crops",
        title = "Crops",
        content = """Crops are the heart of Planetary Idle. They produce your gold and are the primary thing you interact with on the Farm view.

Buying Crops
Each crop has a purchase cost that scales as you buy more. The more you own, the higher each additional one costs. You can change how many are purchased per click using the x1, x10, x100, and Max buttons at the top of the Farm view. Max buys as many as you can currently afford.

Production
Each crop type has a base production value and a cycle duration. When a cycle completes, a gold payout is generated based on how many you own multiplied by your current bonuses. The cycle progress is shown as a progress bar on each crop row.

Unlocking New Colors
Crops come in different colors representing tiers of increasing power. Owning a certain number of your current highest-color crop will unlock the next color. The milestone required is displayed on each crop row so you always know how far away your next unlock is.

Milestones
Reaching certain ownership counts (10, 25, 50, 100, 500, 5000) provides permanent production multipliers for that crop, automatically applied as you reach each threshold."""
    ),

    ACHIEVEMENTS(
        id = "achievements",
        title = "Achievements",
        content = """Achievements reward you for reaching milestones throughout the game. Once earned, their bonuses are permanent and never reset — even across prestiges.

Production Multiplier
Every achievement you complete grants a x1.05 multiplier to your overall production. These stack multiplicatively, so the more you earn, the more each new one is worth. Later in the game a special achievement upgrades this from x1.05 to x1.06 per achievement.

Special Achievements
Some achievements (highlighted in gold in the Achievements view) provide unique bonuses beyond the standard multiplier. These include things like boosted production for specific crop colors, gold income bonuses, reduced upgrade costs, and more.

Tracking Progress
The Achievements view shows all achievements with their completion status and your current progress toward incomplete ones. You can filter by All, Completed, In Progress, or Locked to focus on what matters most right now."""
    ),

    SOIL(
        id = "soil",
        title = "Soil Upgrades",
        content = """Soil Upgrades unlock alongside the Barn and represent the game's first prestige-style mechanic.

What Soil Does
Upgrading your soil increases the production output of a specific crop color. Each level of soil provides a meaningful multiplier boost to that color's gold generation. Better soil means each crop of that color is worth significantly more per cycle.

Resetting Soil
Soil upgrades can be reset at any time. Resetting returns the soil level to zero and refunds a portion of the investment. This is intentional — resetting and rebuilding soil is one of the core strategies for pushing production into higher ranges, since costs scale but so do the refund amounts.

Unlocking Higher Tier Crops
Reaching certain soil upgrade levels is required before you can unlock higher-tier crop colors. If you find yourself unable to progress to the next color tier, check whether your soil level meets the unlock threshold shown on the Farm view."""
    ),

    BARN(
        id = "barn",
        title = "The Barn",
        content = """The Barn unlocks after you purchase your first green crop. It provides a large tree of permanent upgrades that enhance your farm in a variety of ways.

Upgrade Tree
Barn upgrades are arranged in a node tree. You must purchase prerequisite upgrades before more advanced ones become available. Branches cover:

  Per-color production multipliers
  Overall farm speed bonuses
  Soil effectiveness improvements
  Unlocking new buildings

Barn upgrades are permanent. They do not reset when you prestige or reset your soil. They represent long-term investment in your operation.

Key Unlocks
Two of the most important Barn upgrades are:

  Kitchen — Unlocks the Kitchen building, opening up a second layer of production via crops and recipes.

  Observatory — A high-cost, high-reward upgrade that unlocks the Observatory building and the Insight/Discoveries progression system."""
    ),

    KITCHEN(
        id = "kitchen",
        title = "The Kitchen",
        content = """The Kitchen unlocks via a Barn upgrade and adds a second layer of production on top of your farm crops.

Crops
The Kitchen tracks 50 crops across 10 colors and 5 tiers. These are discovered over time through the Research system and represent increasingly powerful production ingredients.

Recipes
Recipes combine two or more discovered crops to generate substantial gold payouts on a cycle. Each recipe has its own duration and payout that scales with your current multipliers. Only one recipe can be active per researcher slot at a time.

The best recipe for each slot is highlighted with a star (★) to help you optimize quickly. Estimated payouts per cycle are displayed for every recipe.

Research
Researchers are workers that discover new crops and recipes over time. Each researcher operates at a set speed (upgradeable) and generates discoveries automatically. The more researchers you have active, the faster your Kitchen content expands.

Payouts
Recipe payouts scale with your achievement multiplier, Barn bonuses, and Observatory discoveries. The Kitchen view shows estimated payouts that update as your multipliers change."""
    ),

    CODEX(
        id = "codex",
        title = "The Codex",
        content = """The Codex unlocks alongside the Kitchen and serves as your in-game encyclopedia of discovered content.

Crops Tab
Shows all 50 possible crops organized by color group and tier. Discovered crops display their full name and details in color. Undiscovered crops appear as grey question marks, giving you a sense of how much is still left to find within each group.

Recipes Tab
Shows all recipe combinations organized by color pairing. Each discovered recipe displays its name and estimated payout per cycle. Undiscovered slots show a count of how many more exist in that pairing group, so you can see where the most undiscovered content remains.

Why Use It
The Codex is useful for planning your Kitchen strategy. You can see which crop color pairings have the most undiscovered recipes and compare recipe payouts across your entire discovered catalog."""
    ),

    OBSERVATORY(
        id = "observatory",
        title = "The Observatory",
        content = """The Observatory unlocks via a high-tier Barn upgrade and introduces Insight — a secondary currency that powers the Discoveries progression system.

Insight
Insight is generated passively each second based on your total crop production rate. The generation formula is: rate ^ 0.4 Insight per second, so faster farms earn Insight more quickly with diminishing returns. Discoveries can multiply this rate significantly.

Insight is used exclusively to purchase Discoveries.

Discoveries
There are 20 Discoveries arranged across 5 tiers:

  Common (Grey) — Small production multipliers, speed bonuses, and Insight generation boosts.

  Treasured (Green) — Per-color multipliers, recipe payout boosts, and achievement bonuses.

  Legendary (Blue) — Large all-production multipliers and tick speed boosts.

  Fabled (Purple) — Massive multipliers, including squaring your achievement bonus.

  Mythical (Gold) — Endgame-tier bonuses with astronomical multiplier values. The final Discovery, Unified Principle, multiplies all other discoveries' effects.

Unlocking Tiers
You begin with only Common Discoveries available. Purchasing enough in a tier unlocks the next:

  3 Common unlocks Treasured
  3 Treasured unlocks Legendary
  2 Legendary unlocks Fabled
  2 Fabled unlocks Mythical

Effects
Discovery effects apply globally and are reflected automatically in your Farm production, Kitchen recipe payouts, and achievement calculations once purchased."""
    ),
}
