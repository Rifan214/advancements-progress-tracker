package dev.rifan.advancementsprogresstracker.resolver;

import dev.rifan.advancementsprogresstracker.data.CriterionDetail;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class EntityCriterionResolver implements CriterionResolver {
	@Override
	public int priority() {
		return 80;
	}

	@Override
	public boolean canResolve(String criterionId, String advancementId) {
		if (advancementId.contains("kill_all_mobs") || advancementId.contains("kill_a_mob")
			|| advancementId.contains("bred_all_animals") || advancementId.contains("tame_all_animals")) {
			return true;
		}
		Identifier id = Identifier.tryParse(criterionId);
		if (id != null) {
			try {
				return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.containsKey(id);
			} catch (Throwable ignored) {
			}
		}
		return false;
	}

	@Override
	public CriterionDetail resolve(String criterionId, boolean completed, @Nullable Instant obtainedTime, String advancementId) {
		Identifier id = Identifier.tryParse(criterionId.contains(":") ? criterionId : "minecraft:" + criterionId);
		Component displayName;
		net.minecraft.world.item.ItemStack icon = net.minecraft.world.item.ItemStack.EMPTY;
		if (id != null) {
			displayName = Component.translatableWithFallback(
				"entity." + id.getNamespace() + "." + id.getPath(),
				formatFallback(id.getPath())
			);
			try {
				net.minecraft.resources.Identifier eggId = net.minecraft.resources.Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_spawn_egg");
				var itemOpt = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(eggId);
				if (itemOpt.isPresent()) {
					var holder = itemOpt.get().builtInRegistryHolder();
					if (holder != null && holder.areComponentsBound()) {
						icon = new net.minecraft.world.item.ItemStack(itemOpt.get());
					}
				}
			} catch (Throwable ignored) {
			}
		} else {
			displayName = Component.literal(formatFallback(criterionId));
		}
		return new CriterionDetail(criterionId, displayName, completed, obtainedTime, "entity", icon);
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
