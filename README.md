# Advancements Progress Tracker

A lightweight, modern Minecraft Fabric mod that makes advancement progress fully detailed, informative, and explorable — especially for multi-criteria advancements like **Adventuring Time**, **Monsters Hunted**, **Two by Two**, **A Balanced Diet**, and more.

---

## Features

* **Detailed Multi-Criteria Progress**: Inspect every individual criterion required for complex advancements.
* **Separation of Completed & Remaining**: Visually distinguishes completed criteria (`✓`) from remaining goals (`○`).
* **Meaningful Criterion Names**: Automatically resolves criteria into human-readable, localized names (biomes, mobs, items, armor trim patterns, animal variants) rather than raw internal criterion IDs.
* **Requirements Semantics Aware**: Correctly handles Minecraft's advancement logic:
  * **Strict AND**: All criteria required (e.g. *Adventuring Time* with 56 biomes, *Monsters Hunted* with 41 mobs).
  * **Strict OR**: Any 1 criterion required (e.g. *Monster Hunter*, *Crafting a New Look*, *A Seedy Place*).
  * **Mixed (CNF)**: Combined requirements (e.g. *Careful Restoration* requiring both a structure loot and a pottery sherd).
* **Dynamic Requirement Inspection**: Never relies on static hardcoded lists. Dynamically inspects Minecraft's active advancement definitions (seamlessly adapts to datapacks, vanilla updates, and modded advancements).
* **Progress Bar & Counters**: Clear numerical counters (`x / y`), percentage indicators, and visual progress bar.
* **Search & Real-time Filtering**: Filter advancements by status (*All*, *In Progress*, *Completed*, *Not Started*, *Multi-Criteria*) and search by advancement or criterion names.
* **Live In-Game Refresh**: Automatically updates the displayed progress in real-time when criteria are achieved without closing the screen or losing scroll position.
* **Modern Lightweight UI**: Responsive two-panel design with mouse wheel scrolling, scrollbar indicator, keyboard navigation, and colorblind-friendly indicators.
* **Vanilla Advancements Screen Integration**: Injects a clean "Tracker" button into the vanilla Advancements screen (`L`).
* **Configurable Keybinding**: Toggle the tracker anytime in-game via a hotkey (default: `H`, remappable in Controls).
* **100% Client-Side**: Operates entirely on synced client data. Works on singleplayer worlds, LAN, and multiplayer servers without requiring the mod on the server.
* **Zero Save Tampering**: Strictly read-only viewer on vanilla's advancement progress. Never overwrites, resets, or duplicates vanilla save files.
* **Localization**: Full multi-language support (includes `en_us` and `id_id`).

---

## Requirements

| Component | Target Version |
| :--- | :--- |
| **Minecraft Java Edition** | `26.3` |
| **Fabric Loader** | `0.19.5` or higher |
| **Fabric API** | `0.161.0+26.3` |
| **Java** | Java 25 (Temurin / Eclipse Adoptium recommended) |
| **Mod Loader** | **Fabric only** |

---

## Installation

1. Ensure **Minecraft Java Edition 26.3** is installed.
2. Install **Fabric Loader** version `0.19.5` or higher for Minecraft 26.3.
3. Download the compatible **Fabric API** (`0.161.0+26.3`) jar and place it into your `.minecraft/mods/` folder.
4. Download the `advancements-progress-tracker-<version>.jar` from releases or build from source.
5. Place the mod jar into your `.minecraft/mods/` directory.
6. Launch Minecraft using the Fabric profile.

---

## Usage

You can access the Progress Tracker through two methods:

1. **Via Keybind Toggle**:
   * Press **`H`** (default) during gameplay to directly open the Advancements Progress Tracker.
   * Press **`H`** again while the screen is open to smoothly close it.
   * Keybind can be customized in **Options** → **Controls** → **Key Binds** → **Advancements Progress Tracker**.
2. **Via Vanilla Advancements Screen**:
   * Press **`L`** to open the standard Minecraft Advancements menu.
   * Click the **"Tracker"** button located in the top-right corner of the window.

### UI Controls
* **Search Box**: Type in the search box to filter advancements or criteria dynamically in real-time.
* **Filter Button**: Click to toggle between `[All]`, `[In Progress]`, `[Completed]`, `[Not Started]`, and `[Multi-Criteria]`.
* **Mouse Wheel**: Scroll either panel independently to navigate long criteria lists (e.g. 56 biomes for *Adventuring Time* in Minecraft 26.3).
* **Done / Esc**: Returns to the previous screen or resumes gameplay.

---

## Compatibility

| Environment | Compatibility | Details |
| :--- | :---: | :--- |
| **New Worlds** | Compatible | Progress is tracked and displayed from the moment of creation. |
| **Existing Worlds** | Compatible | Reads existing vanilla advancement saves without modification or risk of progress reset. |
| **Multiplayer Servers** | Compatible | Client-only operation. Displays the connected player's individual progress. No server installation needed. |
| **Datapacks** | Compatible | Automatically parses custom advancement requirements and criteria from active datapacks. |
| **Modded Advancements** | Compatible | Generic criterion resolver architecture gracefully handles modded criteria and custom namespaces. |

---

## Criterion Resolvers & Limitations

The mod uses an extensible resolver pipeline:
* **Biome Resolver**: Resolves Overworld (56 biomes including *Dappled Forest*) and Nether biomes to localized vanilla names (`biome.minecraft.<id>`).
* **Entity Resolver**: Resolves hostile mobs and breedable animals to localized entity names (`entity.minecraft.<id>`).
* **Item Resolver**: Resolves food items (*Balanced Diet*), armor pieces (*Suit Up*, *Cover Me in Diamonds*), fish, and seeds to localized item names (`item.minecraft.<id>`).
* **Trim Pattern Resolver**: Resolves armor trim smithing templates (*Smithing with Style*, *Crafting a New Look*) to official trim pattern names (`trim_pattern.minecraft.<id>`).
* **Variant Resolver**: Resolves animal breed and variant criteria (*Complete Catalogue* cats, *Goodest Boy* wolves, *When the Squad Hops into Town* frogs) to clean variant names.
* **Generic Fallback**: Formats compound, structural, or unknown criterion IDs into clean title-cased labels.

### Known Limitations
* **Structural Criteria without Translation Keys**: Certain criteria representing structural loot locations (e.g. Bastion sections in *War Pigs*, archaeological dig sites in *Careful Restoration*) lack dedicated translation keys in vanilla Minecraft and are formatted as clean Title Case text (*Desert Pyramid*, *Bastion Bridge*).
* **Opaque Datapack Criteria**: Custom criteria defined by datapacks or mods using arbitrary identifiers without localized description metadata will fall back to clean title-cased ID representations without crashing.

---

## Building from Source

```bash
# Clone the repository
git clone https://github.com/Rifan214/advancements-progress-tracker.git
cd advancements-progress-tracker

# Build the mod jar and run tests
./gradlew build

# Launch the Minecraft test client
./gradlew runClient
```

The compiled mod JAR will be located in `build/libs/`.

---

## License

This project is licensed under the MIT License.
