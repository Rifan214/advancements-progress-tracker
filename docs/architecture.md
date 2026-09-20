# Minecraft 26.2 Advancements Architecture & Discovery

Dokumen ini merangkum hasil riset dan verifikasi mendalam terhadap sistem advancement pada **Minecraft Java Edition 26.2** (Fabric Loader 0.19.3, Java 25).

---

## 1. Bagaimana Advancement Progress Tersedia

Pada Minecraft 26.2, sistem advancement pada client didorong sepenuhnya oleh paket jaringan vanilla dari server:
- Saat player masuk ke world (baik world baru, world lama, singleplayer, maupun multiplayer server), server mengirimkan packet `ClientboundUpdateAdvancementsPacket`.
- Packet ini diterima oleh `net.minecraft.client.multiplayer.ClientAdvancements`.
- `ClientAdvancements` mengelola dua komponen penting:
  1. `tree`: `net.minecraft.advancements.AdvancementTree` — menyimpan seluruh node hierarki advancement (`AdvancementNode` dan `AdvancementHolder`).
  2. `progress`: `Map<AdvancementHolder, AdvancementProgress>` — menyimpan progress terkini untuk setiap advancement milik player yang sedang aktif.
  3. `listener`: `ClientAdvancements.Listener` — interface yang menerima event saat root/task advancement ditambahkan, dihapus, atau saat progress di-update (`onUpdateAdvancementProgress(AdvancementNode, AdvancementProgress)`).

Karena data ini sudah disinkronkan secara realtime oleh vanilla ke client, mod dapat beroperasi sebagai **Client-side mod** murni tanpa membutuhkan mod di server atau protokol jaringan khusus.

---

## 2. Bagaimana Criteria Direpresentasikan

Struktur data internal Minecraft 26.2:

### `AdvancementHolder` (Record)
- `Identifier id()`: ID advancement (misal: `minecraft:adventure/adventuring_time`).
- `Advancement value()`: Object advancement sebenarnya.

### `Advancement` (Record)
- `Optional<DisplayInfo> display()`: Informasi visual (title, description, icon `ItemStackTemplate`, `AdvancementType`, `isHidden`).
- `Map<String, Criterion<?>> criteria()`: Map dari criterion ID ke trigger definition.
- `AdvancementRequirements requirements()`: Aturan pemenuhan criteria (AND/OR kelompok criteria).
  - `requirements().requirements()` bertipe `List<List<String>>`.
  - `requirements().size()`: total requirement groups yang harus dipenuhi.

### `AdvancementProgress`
- `isDone()`: `boolean` — apakah advancement sudah selesai.
- `hasProgress()`: `boolean` — apakah ada progress minimal satu criterion.
- `getPercent()`: `float` — persentase penyelesaian (0.0f - 1.0f).
- `getProgressText()`: `Component` — text progress vanilla (contoh: `"35/53"`).
- `getCompletedCriteria()`: `Iterable<String>` — daftar nama/ID criteria yang sudah terpenuhi.
- `getRemainingCriteria()`: `Iterable<String>` — daftar nama/ID criteria yang belum terpenuhi.
- `getCriterion(String id)`: `CriterionProgress` — status per criterion (memiliki `isDone()` dan `getObtained() : Instant`).

---

## 3. Bagaimana `Adventuring Time` Bekerja

Pada Minecraft 26.2:
- Advancement ID: `minecraft:adventure/adventuring_time`.
- Vanilla mendefinisikannya melalui `VanillaAdventureAdvancements.createAdventuringTime(...)`.
- Daftar biome diperoleh dari generator parameter list `MultiNoiseBiomeSourceParameterList.Preset.usedBiomes()`, yang menghasilkan 53 biome Overworld.
- Setiap biome ditambahkan sebagai criterion dengan nama ID:
  ```java
  String criterionName = biomeKey.identifier().toString(); // Contoh: "minecraft:plains"
  ```
- Trigger yang digunakan adalah `PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inBiome(biomeHolder))`.
- Ketika player menginjak suatu biome:
  - Server mencatat criterion tersebut selesai.
  - Server menyimpan data ke `<world>/advancements/<player-uuid>.json`.
  - Server mengirim update ke client via `ClientboundUpdateAdvancementsPacket`.
- Pada client:
  - `AdvancementProgress.getCompletedCriteria()` mengembalikan semua string biome yang sudah dikunjungi.
  - `AdvancementProgress.getRemainingCriteria()` mengembalikan semua string biome yang belum dikunjungi.
  - Total biome = `advancement.requirements().size()` (53 biome).

---

## 4. Kompatibilitas World Lama (Existing World)

- Minecraft menyimpan progress advancement player di direktori save world:
  `<saves>/<world_name>/advancements/<player_uuid>.json`.
- Saat world lama dibuka dengan mod ini:
  1. Vanilla server/internal server membaca file JSON tersebut secara native.
  2. Vanilla memvalidasi dan memuat `AdvancementProgress`.
  3. Vanilla mengirimkan seluruh data progress ke client melalui `ClientboundUpdateAdvancementsPacket`.
  4. Mod membaca data dari `ClientAdvancements`.
- Mod **tidak membaca atau menulis langsung ke file save world**, sehingga:
  - Tidak ada risiko korupsi world lama.
  - Tidak ada progress yang ter-reset.
  - Progress lama yang sudah ada langsung terbaca dan tertampil secara akurat.

---

## 5. API dan Mixin yang Diperlukan

1. **`ClientAdvancementsAccessor`**:
   - Mixin interface accessor ke field `ClientAdvancements.progress`:
   ```java
   @Accessor("progress")
   Map<AdvancementHolder, AdvancementProgress> getProgressMap();
   ```
   - Sangat ringan, non-invasif, hanya mengekspos getter tanpa memodifikasi logika vanilla.
2. **Fabric Screen API / Mixin ke `AdvancementsScreen`**:
   - Menambahkan tombol "Progress Tracker" pada GUI Advancement vanilla.
3. **Fabric KeyBinding API (`KeyBindingHelper`)**:
   - Mendaftarkan shortcut tombol keyboard untuk membuka tracker secara langsung (default: unbound atau tombol alternatif).

---

## 6. Resolusi Criteria (Criterion Resolver Architecture)

Untuk menerjemahkan criterion ID (misal `"minecraft:plains"`, `"minecraft:zombie"`) menjadi nama yang ramah pengguna:
- **`BiomeCriterionResolver`**:
  - Menerjemahkan identifier biome ke nama lokal: `Component.translatable("biome." + id.getNamespace() + "." + id.getPath())`.
- **`EntityCriterionResolver`**:
  - Menerjemahkan mob ID ke nama entitas: `EntityType.byString(id).map(EntityType::getDescription)`.
- **`ItemCriterionResolver`**:
  - Menerjemahkan item ID ke nama item: `BuiltInRegistries.ITEM.get(id).getName()`.
- **`GenericCriterionResolver` (Fallback)**:
  - Jika ID tidak cocok dengan registry tertentu, memformat ID menjadi text yang mudah dibaca (misal membersihkan namespace `minecraft:` dan merapikan underscore).
- Graceful error handling menjamin mod tidak akan crash jika menemukan criterion kustom dari datapack/mod lain.

---

## 7. Risiko Kompatibilitas & Mitigasi

| Potensi Masalah | Dampak | Strategi Mitigasi |
|---|---|---|
| Datapack kustom dengan kriteria non-standar | Unknown criterion format | Graceful fallback ke generic resolver; tetap menampilkan status terpenuhi/belum tanpa crash |
| Advancement tersembunyi (hidden) | Tampil sebelum waktunya | Periksa `display.isHidden()` dan status penyelesaian sebelum menampilkan |
| Server vanilla / multiplayer tanpa mod | Mod tidak bisa sinkron jika server-dependent | Desain 100% client-side; membaca packet vanilla standar yang selalu dikirim server |
| Heavy performance cost saat render GUI | Lag/FPS drop | Cache data yang telah di-resolve; invalidasi cache hanya saat menerima packet update advancement |
