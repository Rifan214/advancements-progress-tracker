# Vanilla Advancements & Criterion Coverage Audit (Minecraft 26.2)

Dokumen ini berisi hasil audit komprehensif terhadap seluruh 126 advancement vanilla player-facing pada Minecraft 26.2.

## 1. Ringkasan Klasifikasi Coverage

- **Total Vanilla Advancements (Player-Facing)**: 126
- **Multi-Criteria Advancements**: 24
- **Single-Criterion Advancements**: 102

### Kategori Resolver Status
* **Full (Multi-Criteria)**: Kriteria diterjemahkan menjadi nama lokal resmi Minecraft yang 100% bermakna (biome, entity mob/animal, item pangan/armor/benih, motif hiasan armor trim).
* **Full (Variant)**: Kriteria varian hewan (kucing, serigala, katak) diterjemahkan menjadi nama ras/varian visual yang ramah pemain.
* **Partial / Generic (Multi-Criteria)**: Kriteria majemuk atau kondisi runtime struktural (loot bastion, ruin arkeologi, placement redstone) yang di-format oleh `GenericCriterionResolver` menjadi Title Case yang rapi.
* **Full (Single-Criterion)**: Advancement dengan 1 kriteria tunggal di mana penyelesaian trigger langsung memenuhi kriteria 1/1.

---

## 2. Tabel Multi-Criteria Advancements (24 Advancements)

| Advancement ID | Title Key | Criteria | Req Groups | Semantic Type | Primary Trigger | Resolver | Detail Status |
| :--- | :--- | :---: | :---: | :--- | :--- | :--- | :--- |
| `adventure/adventuring_time` | `advancements.adventure.adventuring_time.title` | 55 | 55 | AND (All) | `location` | **Biome** | Full |
| `adventure/kill_a_mob` | `advancements.adventure.kill_a_mob.title` | 41 | 1 | OR (Any 1) | `player_killed_entity` | **Entity** | Full |
| `adventure/kill_all_mobs` | `advancements.adventure.kill_all_mobs.title` | 41 | 41 | AND (All) | `player_killed_entity` | **Entity** | Full |
| `husbandry/balanced_diet` | `advancements.husbandry.balanced_diet.title` | 40 | 40 | AND (All) | `consume_item` | **Item** | Full |
| `husbandry/bred_all_animals` | `advancements.husbandry.breed_all_animals.title` | 26 | 26 | AND (All) | `bred_animals` | **Entity** | Full |
| `adventure/trim_with_any_armor_pattern` | `advancements.adventure.trim_with_any_armor_pattern.title` | 18 | 1 | OR (Any 1) | `recipe_crafted` | **TrimPattern** | Full |
| `husbandry/complete_catalogue` | `advancements.husbandry.complete_catalogue.title` | 11 | 11 | AND (All) | `tame_animal` | **Variant** | Full |
| `husbandry/whole_pack` | `advancements.husbandry.whole_pack.title` | 9 | 9 | AND (All) | `tame_animal` | **Variant** | Full |
| `adventure/trim_with_all_exclusive_armor_patterns` | `advancements.adventure.trim_with_all_exclusive_armor_patterns.title` | 8 | 8 | AND (All) | `recipe_crafted` | **TrimPattern** | Full |
| `adventure/salvage_sherd` | `advancements.adventure.salvage_sherd.title` | 7 | 2 | Mixed (CNF) | `player_generates_container_loot, inventory_changed` | **Generic** | Partial |
| `husbandry/plant_seed` | `advancements.husbandry.plant_seed.title` | 7 | 1 | OR (Any 1) | `placed_block` | **Item** | Full |
| `nether/explore_nether` | `advancements.nether.explore_nether.title` | 5 | 5 | AND (All) | `location` | **Biome** | Full |
| `husbandry/fishy_business` | `advancements.husbandry.fishy_business.title` | 4 | 1 | OR (Any 1) | `fishing_rod_hooked` | **Item** | Full |
| `husbandry/tactical_fishing` | `advancements.husbandry.tactical_fishing.title` | 4 | 1 | OR (Any 1) | `filled_bucket` | **Item** | Full |
| `nether/loot_bastion` | `advancements.nether.loot_bastion.title` | 4 | 1 | OR (Any 1) | `player_generates_container_loot` | **Generic** | Partial |
| `story/obtain_armor` | `advancements.story.obtain_armor.title` | 4 | 1 | OR (Any 1) | `inventory_changed` | **Item** | Full |
| `story/shiny_gear` | `advancements.story.shiny_gear.title` | 4 | 1 | OR (Any 1) | `inventory_changed` | **Item** | Full |
| `adventure/heart_transplanter` | `advancements.adventure.heart_transplanter.title` | 3 | 1 | OR (Any 1) | `placed_block` | **Generic** | Partial |
| `husbandry/leash_all_frog_variants` | `advancements.husbandry.leash_all_frog_variants.title` | 3 | 3 | AND (All) | `player_interacted_with_entity` | **Variant** | Full |
| `adventure/read_power_of_chiseled_bookshelf` | `advancements.adventure.read_power_from_chiseled_bookshelf.title` | 2 | 1 | OR (Any 1) | `placed_block` | **Generic** | Partial |
| `adventure/root` | `advancements.adventure.root.title` | 2 | 1 | OR (Any 1) | `player_killed_entity, entity_killed_player` | **Generic** | Partial |
| `husbandry/plant_any_sniffer_seed` | `advancements.husbandry.plant_any_sniffer_seed.title` | 2 | 1 | OR (Any 1) | `placed_block` | **Item** | Full |
| `husbandry/uh_oh` | `advancements.husbandry.uh_oh.title` | 2 | 1 | OR (Any 1) | `player_interacted_with_entity, thrown_item_picked_up_by_entity` | **Generic** | Partial |
| `nether/distract_piglin` | `advancements.nether.distract_piglin.title` | 2 | 1 | OR (Any 1) | `player_interacted_with_entity, thrown_item_picked_up_by_entity` | **Generic** | Partial |

---

## 3. Tabel Single-Criterion Advancements (102 Advancements)

| Advancement ID | Title Key | Criteria | Req Groups | Primary Trigger | Resolver | Detail Status |
| :--- | :--- | :---: | :---: | :--- | :--- | :--- |
| `adventure/arbalistic` | `advancements.adventure.arbalistic.title` | 1 | 1 | `killed_by_arrow` | Generic | Full (Single) |
| `adventure/avoid_vibration` | `advancements.adventure.avoid_vibration.title` | 1 | 1 | `avoid_vibration` | Generic | Full (Single) |
| `adventure/blowback` | `advancements.adventure.blowback.title` | 1 | 1 | `player_killed_entity` | Entity / Generic | Full (Single) |
| `adventure/brush_armadillo` | `advancements.adventure.brush_armadillo.title` | 1 | 1 | `player_interacted_with_entity` | Entity / Generic | Full (Single) |
| `adventure/bullseye` | `advancements.adventure.bullseye.title` | 1 | 1 | `target_hit` | Generic | Full (Single) |
| `adventure/craft_decorated_pot_using_only_sherds` | `advancements.adventure.craft_decorated_pot_using_only_sherds.title` | 1 | 1 | `recipe_crafted` | Item / Generic | Full (Single) |
| `adventure/crafters_crafting_crafters` | `advancements.adventure.crafters_crafting_crafters.title` | 1 | 1 | `crafter_recipe_crafted` | Item / Generic | Full (Single) |
| `adventure/fall_from_world_height` | `advancements.adventure.fall_from_world_height.title` | 1 | 1 | `fall_from_height` | Generic | Full (Single) |
| `adventure/hero_of_the_village` | `advancements.adventure.hero_of_the_village.title` | 1 | 1 | `hero_of_the_village` | Generic | Full (Single) |
| `adventure/honey_block_slide` | `advancements.adventure.honey_block_slide.title` | 1 | 1 | `slide_down_block` | Generic | Full (Single) |
| `adventure/kill_mob_near_sculk_catalyst` | `advancements.adventure.kill_mob_near_sculk_catalyst.title` | 1 | 1 | `kill_mob_near_sculk_catalyst` | Entity / Generic | Full (Single) |
| `adventure/lighten_up` | `advancements.adventure.lighten_up.title` | 1 | 1 | `item_used_on_block` | Item / Generic | Full (Single) |
| `adventure/lightning_rod_with_villager_no_fire` | `advancements.adventure.lightning_rod_with_villager_no_fire.title` | 1 | 1 | `lightning_strike` | Generic | Full (Single) |
| `adventure/minecraft_trials_edition` | `advancements.adventure.minecraft_trials_edition.title` | 1 | 1 | `location` | Biome / Generic | Full (Single) |
| `adventure/ol_betsy` | `advancements.adventure.ol_betsy.title` | 1 | 1 | `shot_crossbow` | Generic | Full (Single) |
| `adventure/overoverkill` | `advancements.adventure.overoverkill.title` | 1 | 1 | `player_hurt_entity` | Entity / Generic | Full (Single) |
| `adventure/play_jukebox_in_meadows` | `advancements.adventure.play_jukebox_in_meadows.title` | 1 | 1 | `item_used_on_block` | Item / Generic | Full (Single) |
| `adventure/revaulting` | `advancements.adventure.revaulting.title` | 1 | 1 | `item_used_on_block` | Item / Generic | Full (Single) |
| `adventure/shoot_arrow` | `advancements.adventure.shoot_arrow.title` | 1 | 1 | `player_hurt_entity` | Entity / Generic | Full (Single) |
| `adventure/sleep_in_bed` | `advancements.adventure.sleep_in_bed.title` | 1 | 1 | `slept_in_bed` | Generic | Full (Single) |
| `adventure/sniper_duel` | `advancements.adventure.sniper_duel.title` | 1 | 1 | `player_killed_entity` | Entity / Generic | Full (Single) |
| `adventure/spear_many_mobs` | `advancements.adventure.spear_many_mobs.title` | 1 | 1 | `spear_mobs` | Entity / Generic | Full (Single) |
| `adventure/spyglass_at_dragon` | `advancements.adventure.spyglass_at_dragon.title` | 1 | 1 | `using_item` | Item / Generic | Full (Single) |
| `adventure/spyglass_at_ghast` | `advancements.adventure.spyglass_at_ghast.title` | 1 | 1 | `using_item` | Item / Generic | Full (Single) |
| `adventure/spyglass_at_parrot` | `advancements.adventure.spyglass_at_parrot.title` | 1 | 1 | `using_item` | Item / Generic | Full (Single) |
| `adventure/summon_iron_golem` | `advancements.adventure.summon_iron_golem.title` | 1 | 1 | `summoned_entity` | Entity / Generic | Full (Single) |
| `adventure/throw_trident` | `advancements.adventure.throw_trident.title` | 1 | 1 | `player_hurt_entity` | Entity / Generic | Full (Single) |
| `adventure/totem_of_undying` | `advancements.adventure.totem_of_undying.title` | 1 | 1 | `used_totem` | Generic | Full (Single) |
| `adventure/trade` | `advancements.adventure.trade.title` | 1 | 1 | `villager_trade` | Generic | Full (Single) |
| `adventure/trade_at_world_height` | `advancements.adventure.trade_at_world_height.title` | 1 | 1 | `villager_trade` | Generic | Full (Single) |
| `adventure/two_birds_one_arrow` | `advancements.adventure.two_birds_one_arrow.title` | 1 | 1 | `killed_by_arrow` | Generic | Full (Single) |
| `adventure/under_lock_and_key` | `advancements.adventure.under_lock_and_key.title` | 1 | 1 | `item_used_on_block` | Item / Generic | Full (Single) |
| `adventure/use_lodestone` | `advancements.adventure.use_lodestone.title` | 1 | 1 | `item_used_on_block` | Item / Generic | Full (Single) |
| `adventure/very_very_frightening` | `advancements.adventure.very_very_frightening.title` | 1 | 1 | `channeled_lightning` | Generic | Full (Single) |
| `adventure/voluntary_exile` | `advancements.adventure.voluntary_exile.title` | 1 | 1 | `player_killed_entity` | Entity / Generic | Full (Single) |
| `adventure/walk_on_powder_snow_with_leather_boots` | `advancements.adventure.walk_on_powder_snow_with_leather_boots.title` | 1 | 1 | `location` | Biome / Generic | Full (Single) |
| `adventure/who_needs_rockets` | `advancements.adventure.who_needs_rockets.title` | 1 | 1 | `fall_after_explosion` | Generic | Full (Single) |
| `adventure/whos_the_pillager_now` | `advancements.adventure.whos_the_pillager_now.title` | 1 | 1 | `killed_by_arrow` | Generic | Full (Single) |
| `end/dragon_breath` | `advancements.end.dragon_breath.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `end/dragon_egg` | `advancements.end.dragon_egg.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `end/elytra` | `advancements.end.elytra.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `end/enter_end_gateway` | `advancements.end.enter_end_gateway.title` | 1 | 1 | `enter_block` | Generic | Full (Single) |
| `end/find_end_city` | `advancements.end.find_end_city.title` | 1 | 1 | `location` | Biome / Generic | Full (Single) |
| `end/kill_dragon` | `advancements.end.kill_dragon.title` | 1 | 1 | `player_killed_entity` | Entity / Generic | Full (Single) |
| `end/levitate` | `advancements.end.levitate.title` | 1 | 1 | `levitation` | Generic | Full (Single) |
| `end/respawn_dragon` | `advancements.end.respawn_dragon.title` | 1 | 1 | `summoned_entity` | Entity / Generic | Full (Single) |
| `end/root` | `advancements.end.root.title` | 1 | 1 | `changed_dimension` | Generic | Full (Single) |
| `husbandry/allay_deliver_cake_to_note_block` | `advancements.husbandry.allay_deliver_cake_to_note_block.title` | 1 | 1 | `allay_drop_item_on_block` | Item / Generic | Full (Single) |
| `husbandry/allay_deliver_item_to_player` | `advancements.husbandry.allay_deliver_item_to_player.title` | 1 | 1 | `thrown_item_picked_up_by_player` | Item / Generic | Full (Single) |
| `husbandry/axolotl_in_a_bucket` | `advancements.husbandry.axolotl_in_a_bucket.title` | 1 | 1 | `filled_bucket` | Generic | Full (Single) |
| `husbandry/breed_an_animal` | `advancements.husbandry.breed_an_animal.title` | 1 | 1 | `bred_animals` | Entity / Generic | Full (Single) |
| `husbandry/feed_snifflet` | `advancements.husbandry.feed_snifflet.title` | 1 | 1 | `player_interacted_with_entity` | Entity / Generic | Full (Single) |
| `husbandry/froglights` | `advancements.husbandry.froglights.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `husbandry/kill_axolotl_target` | `advancements.husbandry.kill_axolotl_target.title` | 1 | 1 | `effects_changed` | Generic | Full (Single) |
| `husbandry/make_a_sign_glow` | `advancements.husbandry.make_a_sign_glow.title` | 1 | 1 | `item_used_on_block` | Item / Generic | Full (Single) |
| `husbandry/obtain_netherite_hoe` | `advancements.husbandry.netherite_hoe.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `husbandry/obtain_sniffer_egg` | `advancements.husbandry.obtain_sniffer_egg.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `husbandry/place_dried_ghast_in_water` | `advancements.husbandry.place_dried_ghast_in_water.title` | 1 | 1 | `placed_block` | Generic | Full (Single) |
| `husbandry/remove_wolf_armor` | `advancements.husbandry.remove_wolf_armor.title` | 1 | 1 | `player_sheared_equipment` | Generic | Full (Single) |
| `husbandry/repair_wolf_armor` | `advancements.husbandry.repair_wolf_armor.title` | 1 | 1 | `player_interacted_with_entity` | Entity / Generic | Full (Single) |
| `husbandry/ride_a_boat_with_a_goat` | `advancements.husbandry.ride_a_boat_with_a_goat.title` | 1 | 1 | `started_riding` | Generic | Full (Single) |
| `husbandry/root` | `advancements.husbandry.root.title` | 1 | 1 | `consume_item` | Item / Generic | Full (Single) |
| `husbandry/safely_harvest_honey` | `advancements.husbandry.safely_harvest_honey.title` | 1 | 1 | `item_used_on_block` | Item / Generic | Full (Single) |
| `husbandry/silk_touch_nest` | `advancements.husbandry.silk_touch_nest.title` | 1 | 1 | `bee_nest_destroyed` | Generic | Full (Single) |
| `husbandry/tadpole_in_a_bucket` | `advancements.husbandry.tadpole_in_a_bucket.title` | 1 | 1 | `filled_bucket` | Generic | Full (Single) |
| `husbandry/tame_an_animal` | `advancements.husbandry.tame_an_animal.title` | 1 | 1 | `tame_animal` | Entity / Generic | Full (Single) |
| `husbandry/wax_off` | `advancements.husbandry.wax_off.title` | 1 | 1 | `item_used_on_block` | Item / Generic | Full (Single) |
| `husbandry/wax_on` | `advancements.husbandry.wax_on.title` | 1 | 1 | `item_used_on_block` | Item / Generic | Full (Single) |
| `nether/all_effects` | `advancements.nether.all_effects.title` | 1 | 1 | `effects_changed` | Generic | Full (Single) |
| `nether/all_potions` | `advancements.nether.all_potions.title` | 1 | 1 | `effects_changed` | Generic | Full (Single) |
| `nether/brew_potion` | `advancements.nether.brew_potion.title` | 1 | 1 | `brewed_potion` | Generic | Full (Single) |
| `nether/charge_respawn_anchor` | `advancements.nether.charge_respawn_anchor.title` | 1 | 1 | `item_used_on_block` | Item / Generic | Full (Single) |
| `nether/create_beacon` | `advancements.nether.create_beacon.title` | 1 | 1 | `construct_beacon` | Generic | Full (Single) |
| `nether/create_full_beacon` | `advancements.nether.create_full_beacon.title` | 1 | 1 | `construct_beacon` | Generic | Full (Single) |
| `nether/fast_travel` | `advancements.nether.fast_travel.title` | 1 | 1 | `nether_travel` | Generic | Full (Single) |
| `nether/find_bastion` | `advancements.nether.find_bastion.title` | 1 | 1 | `location` | Biome / Generic | Full (Single) |
| `nether/find_fortress` | `advancements.nether.find_fortress.title` | 1 | 1 | `location` | Biome / Generic | Full (Single) |
| `nether/get_wither_skull` | `advancements.nether.get_wither_skull.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `nether/netherite_armor` | `advancements.nether.netherite_armor.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `nether/obtain_ancient_debris` | `advancements.nether.obtain_ancient_debris.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `nether/obtain_blaze_rod` | `advancements.nether.obtain_blaze_rod.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `nether/obtain_crying_obsidian` | `advancements.nether.obtain_crying_obsidian.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `nether/return_to_sender` | `advancements.nether.return_to_sender.title` | 1 | 1 | `player_killed_entity` | Entity / Generic | Full (Single) |
| `nether/ride_strider` | `advancements.nether.ride_strider.title` | 1 | 1 | `item_durability_changed` | Item / Generic | Full (Single) |
| `nether/ride_strider_in_overworld_lava` | `advancements.nether.ride_strider_in_overworld_lava.title` | 1 | 1 | `ride_entity_in_lava` | Entity / Generic | Full (Single) |
| `nether/root` | `advancements.nether.root.title` | 1 | 1 | `changed_dimension` | Generic | Full (Single) |
| `nether/summon_wither` | `advancements.nether.summon_wither.title` | 1 | 1 | `summoned_entity` | Entity / Generic | Full (Single) |
| `nether/uneasy_alliance` | `advancements.nether.uneasy_alliance.title` | 1 | 1 | `player_killed_entity` | Entity / Generic | Full (Single) |
| `story/cure_zombie_villager` | `advancements.story.cure_zombie_villager.title` | 1 | 1 | `cured_zombie_villager` | Generic | Full (Single) |
| `story/deflect_arrow` | `advancements.story.deflect_arrow.title` | 1 | 1 | `entity_hurt_player` | Entity / Generic | Full (Single) |
| `story/enchant_item` | `advancements.story.enchant_item.title` | 1 | 1 | `enchanted_item` | Item / Generic | Full (Single) |
| `story/enter_the_end` | `advancements.story.enter_the_end.title` | 1 | 1 | `changed_dimension` | Generic | Full (Single) |
| `story/enter_the_nether` | `advancements.story.enter_the_nether.title` | 1 | 1 | `changed_dimension` | Generic | Full (Single) |
| `story/follow_ender_eye` | `advancements.story.follow_ender_eye.title` | 1 | 1 | `location` | Biome / Generic | Full (Single) |
| `story/form_obsidian` | `advancements.story.form_obsidian.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `story/iron_tools` | `advancements.story.iron_tools.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `story/lava_bucket` | `advancements.story.lava_bucket.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `story/mine_diamond` | `advancements.story.mine_diamond.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `story/mine_stone` | `advancements.story.mine_stone.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `story/root` | `advancements.story.root.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `story/smelt_iron` | `advancements.story.smelt_iron.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |
| `story/upgrade_tools` | `advancements.story.upgrade_tools.title` | 1 | 1 | `inventory_changed` | Generic | Full (Single) |