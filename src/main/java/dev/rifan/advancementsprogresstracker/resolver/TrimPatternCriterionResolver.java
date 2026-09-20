package dev.rifan.advancementsprogresstracker.resolver;

import dev.rifan.advancementsprogresstracker.data.CriterionDetail;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

/**
 * Resolves armor trim smithing template criteria to localized trim pattern names.
 */
public class TrimPatternCriterionResolver implements CriterionResolver {
	@Override
	public int priority() {
		return 90;
	}

	@Override
	public boolean canResolve(String criterionId, String advancementId) {
		return criterionId.startsWith("armor_trimmed_minecraft:") || advancementId.contains("trim_with");
	}

	@Override
	public CriterionDetail resolve(String criterionId, boolean completed, @Nullable Instant obtainedTime, String advancementId) {
		String pattern = extractPatternName(criterionId);
		Component displayName = Component.translatableWithFallback(
			"trim_pattern.minecraft." + pattern,
			formatFallback(pattern)
		);
		net.minecraft.world.item.ItemStack icon = net.minecraft.world.item.ItemStack.EMPTY;
		try {
			net.minecraft.resources.Identifier templateId = net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", pattern + "_armor_trim_smithing_template");
			var itemOpt = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(templateId);
			if (itemOpt.isPresent()) {
				var holder = itemOpt.get().builtInRegistryHolder();
				if (holder != null && holder.areComponentsBound()) {
					icon = new net.minecraft.world.item.ItemStack(itemOpt.get());
				}
			}
		} catch (Throwable ignored) {
		}
		return new CriterionDetail(criterionId, displayName, completed, obtainedTime, "trim", icon);
	}

	private String extractPatternName(String criterionId) {
		String clean = criterionId;
		if (clean.startsWith("armor_trimmed_minecraft:")) {
			clean = clean.substring("armor_trimmed_minecraft:".length());
		} else if (clean.startsWith("armor_trimmed_")) {
			clean = clean.substring("armor_trimmed_".length());
		}
		return clean.replace("_armor_trim_smithing_template_smithing_trim", "")
		            .replace("_smithing_trim", "")
		            .replace("_armor_trim", "");
	}

	private String formatFallback(String name) {
		String[] words = name.replace('_', ' ').split("\\s+");
		StringBuilder sb = new StringBuilder();
		for (String w : words) {
			if (!w.isEmpty()) {
				sb.append(Character.toUpperCase(w.charAt(0)))
				  .append(w.substring(1).toLowerCase())
				  .append(" ");
			}
		}
		return sb.toString().trim() + " Armor Trim";
	}
}
