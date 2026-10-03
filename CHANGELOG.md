# Changelog

All notable changes to this project will be documented in this file.

## 0.2.0

### Compatibility
- Added Minecraft 26.3 support
- Updated Fabric Loader baseline to 0.19.5
- Updated Fabric API to 0.161.0+26.3
- Updated build tooling (Fabric Loom 1.17, Gradle 9.6.0, Java 25)

### Compatibility Fixes
- Updated Minecraft 26.3 API references (`DisplayInfo` record accessors: `title()`, `description()`, `icon()`, `type()`)
- Updated client mixins and accessors (`ClientAdvancements.tree()`)
- Updated advancement handling for dynamic criteria counts
- Updated resolver compatibility for Minecraft 26.3 additions (including `Dappled Forest` biome in `Adventuring Time` totaling 56 criteria)

### Preserved Features
- Detailed completed/remaining criteria
- AND/OR/mixed requirement semantics
- Existing world support
- Datapack support
- Search
- Filters
- Draggable scrollbars
- Responsive UI

## 0.1.0

### Initial Release
- Initial release for Minecraft 26.2 (Fabric Loader 0.19.3, Fabric API 0.161.0+26.2, Java 25).
- Detailed multi-criteria advancement progress tracking (Adventuring Time, Monsters Hunted, A Balanced Diet, etc.).
- AND/OR/Mixed CNF requirements calculation.
- Responsive two-panel UI with draggable scrollbar, search, and status filters.
- Integration into vanilla Advancements screen (`L`) and direct hotkey toggle (`H`).
- Extensible criterion resolver architecture (Biome, Item, Entity, TrimPattern, Variant, Generic fallback).
- Multi-language localization (`en_us` and `id_id`).
