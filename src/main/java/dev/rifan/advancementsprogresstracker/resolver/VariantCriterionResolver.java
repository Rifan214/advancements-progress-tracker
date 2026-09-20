package dev.rifan.advancementsprogresstracker.resolver;

import dev.rifan.advancementsprogresstracker.data.CriterionDetail;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

/**
 * Resolves entity variant criteria (Cat breeds, Wolf variants, Frog variants).
 */
public class VariantCriterionResolver implements CriterionResolver {
	@Override
	public int priority() {
		return 85;
	}

	@Override
	public boolean canResolve(String criterionId, String advancementId) {
		return advancementId.contains("complete_catalogue")
			|| advancementId.contains("whole_pack")
			|| advancementId.contains("frog_variants")
			|| advancementId.contains("leash_all_frog");
	}

	@Override
	public CriterionDetail resolve(String criterionId, boolean completed, @Nullable Instant obtainedTime, String advancementId) {
		Identifier id = Identifier.tryParse(criterionId);
		String path = id != null ? id.getPath() : criterionId;
		String category;
		String prefix;

		if (advancementId.contains("complete_catalogue")) {
			category = "cat_variant";
			prefix = "Cat: ";
		} else if (advancementId.contains("whole_pack")) {
			category = "wolf_variant";
			prefix = "Wolf: ";
		} else if (advancementId.contains("frog")) {
			category = "frog_variant";
			prefix = "Frog: ";
		} else {
			category = "variant";
			prefix = "";
		}

		String formattedName = formatTitleCase(path);
		Component displayName = Component.literal(prefix + formattedName);
		return new CriterionDetail(criterionId, displayName, completed, obtainedTime, category);
	}

	private String formatTitleCase(String text) {
		String[] words = text.replace('_', ' ').replace('-', ' ').split("\\s+");
		StringBuilder sb = new StringBuilder();
		for (String w : words) {
			if (!w.isEmpty()) {
				sb.append(Character.toUpperCase(w.charAt(0)))
				  .append(w.substring(1).toLowerCase())
				  .append(" ");
			}
		}
		return sb.toString().trim();
	}
}
