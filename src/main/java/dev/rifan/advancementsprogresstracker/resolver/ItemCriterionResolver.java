package dev.rifan.advancementsprogresstracker.resolver;

import dev.rifan.advancementsprogresstracker.data.CriterionDetail;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class ItemCriterionResolver implements CriterionResolver {
	private static final java.util.Map<String, String> SPECIAL_ITEM_MAPPINGS = java.util.Map.of(
		"beetroots", "beetroot_seeds",
		"melon_stem", "melon_seeds",
		"pumpkin_stem", "pumpkin_seeds",
		"wheat", "wheat_seeds"
	);

	@Override
	public int priority() {
		return 70;
	}

	@Override
	public boolean canResolve(String criterionId, String advancementId) {
		if (advancementId.contains("balanced_diet")
			|| advancementId.contains("obtain_armor")
			|| advancementId.contains("shiny_gear")
			|| advancementId.contains("fishy_business")
			|| advancementId.contains("tactical_fishing")
			|| advancementId.contains("plant_seed")
			|| advancementId.contains("plant_any_sniffer_seed")) {
			return true;
		}
		Identifier id = Identifier.tryParse(criterionId.contains(":") ? criterionId : "minecraft:" + criterionId);
		if (id != null) {
			try {
				return net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(id);
			} catch (Throwable ignored) {
			}
		}
		return false;
	}

	@Override
	public CriterionDetail resolve(String criterionId, boolean completed, @Nullable Instant obtainedTime, String advancementId) {
		String itemPath = SPECIAL_ITEM_MAPPINGS.getOrDefault(criterionId, criterionId);
		Identifier id = Identifier.tryParse(itemPath.contains(":") ? itemPath : "minecraft:" + itemPath);
		net.minecraft.world.item.ItemStack icon = net.minecraft.world.item.ItemStack.EMPTY;
		Component displayName = null;

		if (id != null) {
			try {
				var itemOpt = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(id);
				if (itemOpt.isPresent()) {
					var item = itemOpt.get();
					displayName = Component.translatableWithFallback(
						item.getDescriptionId(),
						formatFallback(id.getPath())
					);
					var holder = item.builtInRegistryHolder();
					if (holder != null && holder.areComponentsBound()) {
						icon = new net.minecraft.world.item.ItemStack(item);
					}
				}
			} catch (Throwable ignored) {
			}
		}

		if (displayName == null) {
			if (id != null) {
				displayName = Component.translatableWithFallback(
					"item." + id.getNamespace() + "." + id.getPath(),
					formatFallback(id.getPath())
				);
			} else {
				displayName = Component.literal(formatFallback(criterionId));
			}
		}
		return new CriterionDetail(criterionId, displayName, completed, obtainedTime, "item", icon);
	}

	private String formatFallback(String path) {
		String[] parts = path.replace('_', ' ').split("\\s+");
		StringBuilder sb = new StringBuilder();
		for (String part : parts) {
			if (!part.isEmpty()) {
				sb.append(Character.toUpperCase(part.charAt(0)))
				  .append(part.substring(1).toLowerCase())
				  .append(" ");
			}
		}
		return sb.toString().trim();
	}
}
