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
		Identifier id = Identifier.tryParse(criterionId);
		Component displayName;
		if (id != null) {
			displayName = Component.translatableWithFallback(
				"entity." + id.getNamespace() + "." + id.getPath(),
				formatFallback(id.getPath())
			);
		} else {
			displayName = Component.literal(formatFallback(criterionId));
		}
		return new CriterionDetail(criterionId, displayName, completed, obtainedTime, "entity");
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
