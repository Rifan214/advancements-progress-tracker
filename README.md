# Advancements Progress Tracker

A lightweight, modern Minecraft Fabric mod that makes advancement progress fully detailed, informative, and explorable — especially for multi-criteria advancements like **Adventuring Time**, **Monsters Hunted**, **Two by Two**, and more.

---

## Features

* **Detailed Multi-Criteria Progress**: Inspect every individual criterion required for complex advancements.
* **Separation of Completed & Remaining**: Visually distinguishes completed criteria (`✓`) from remaining goals (`○`).
* **Meaningful Criterion Names**: Automatically resolves criteria into human-readable, localized names (biomes, mobs, items, dimensions, locations) rather than raw internal criterion IDs.
* **Dynamic Requirement Inspection**: Never relies on static hardcoded lists. Dynamically inspects Minecraft's active advancement definitions (seamlessly adapts to datapacks, vanilla changes, and modded advancements).
* **Progress Bar & Counters**: Clear numerical counters (`x / y`), percentage indicators, and visual progress bar.
* **Search & Real-time Filtering**: Filter advancements by status (*All*, *In Progress*, *Completed*, *Not Started*) and search by advancement or criterion names.
* **Modern Lightweight UI**: Responsive two-panel design with mouse wheel scrolling, scrollbar indicator, keyboard navigation, and colorblind-friendly indicators.
* **Vanilla Advancements Screen Integration**: Injects a clean "Tracker" button into the vanilla Advancements screen (`L`).
* **Configurable Keybinding**: Open the tracker anytime in-game via a hotkey (default: `H`, remappable in Controls).
* **100% Client-Side**: Operates entirely on synced client data. Works on singleplayer worlds, LAN, and multiplayer servers without requiring the mod on the server.
* **Zero Save Tampering**: Strictly read-only viewer on vanilla's advancement progress. Never overwrites, resets, or duplicates vanilla save files.
* **Localization**: Full multi-language support (includes `en_us` and `id_id`).

---

## Requirements

| Component | Target Version |
| :--- | :--- |
| **Minecraft Java Edition** | `26.2` |
| **Fabric Loader** | `0.19.3` or higher |
| **Fabric API** | Compatible with Minecraft 26.2 |
| **Java** | Java 25 (Temurin / Eclipse Adoptium recommended) |
| **Mod Loader** | **Fabric only** |

---

## Installation

1. Ensure **Minecraft Java Edition 26.2** is installed.
2. Install **Fabric Loader** version `0.19.3` or higher for Minecraft 26.2.
3. Download the compatible **Fabric API** jar and place it into your `.minecraft/mods/` folder.
4. Download the `advancements-progress-tracker-<version>.jar` from releases or build from source.
5. Place the mod jar into your `.minecraft/mods/` directory.
6. Launch Minecraft using the Fabric profile.

---

## Usage

You can access the Progress Tracker through two methods:

1. **Via Keybind**:
   * Press **`H`** (default) during gameplay to directly open the Advancements Progress Tracker.
   * Keybind can be customized in **Options** → **Controls** → **Key Binds** → **Advancements Progress Tracker**.
2. **Via Vanilla Advancements Screen**:
   * Press **`L`** to open the standard Minecraft Advancements menu.
   * Click the **"Tracker"** button located in the top-right corner of the window.

### UI Controls
* **Search Box**: Type in the search box to filter advancements or criteria dynamically.
* **Filter Button**: Click to toggle between `[All]`, `[In Progress]`, `[Completed]`, and `[Not Started]`.
* **Mouse Wheel**: Scroll either panel independently to navigate long criteria lists (e.g. 53 biomes for *Adventuring Time*).
* **Done / Esc**: Returns to the previous screen or resumes gameplay.

---

## Compatibility

| Environment | Compatibility | Details |
| :--- | :---: | :--- |
| **New Worlds** | Compatible | Progress is tracked and displayed from the moment of creation. |
| **Existing Worlds** | Compatible | Reads existing vanilla advancement saves without modification or risk of progress reset. |
| **Multiplayer Servers** | Compatible | Client-only operation. Displays the connected player's individual progress. No server installation needed. |
| **Datapacks** | Compatible | Automatically parses custom advancement requirements and criteria from active datapacks. |
| **Modded Advancements** | Compatible | Generic criterion resolver architecture gracefully handles modded criteria. |

---

## Criterion Resolvers & Limitations

The mod uses a modular resolver pipeline:
* **Biome Resolver**: Resolves biome IDs (`minecraft:plains`, `minecraft:cherry_grove`, etc.) to localized biome names.
* **Entity / Mob Resolver**: Resolves entity criterion IDs to localized mob and entity names.
* **Item Resolver**: Resolves item IDs to localized item components.
* **Generic Fallback**: Formats unknown or composite criterion IDs into human-readable title-cased labels.

### Known Limitations
* Certain custom criteria defined by datapacks or mods that use opaque or encrypted internal identifiers without corresponding translation keys will be presented using clean title-cased names and their completion status. They will never crash the game.

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
