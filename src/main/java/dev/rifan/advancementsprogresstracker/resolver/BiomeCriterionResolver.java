package dev.rifan.advancementsprogresstracker.resolver;

import dev.rifan.advancementsprogresstracker.data.CriterionDetail;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class BiomeCriterionResolver implements CriterionResolver {
	@Override
	public int priority() {
		return 100;
	}

	@Override
	public boolean canResolve(String criterionId, String advancementId) {
		if (advancementId.contains("adventuring_time")) {
			return true;
		}
		Identifier id = Identifier.tryParse(criterionId);
		return id != null && criterionId.contains("biome");
	}

	@Override
	public CriterionDetail resolve(String criterionId, boolean completed, @Nullable Instant obtainedTime, String advancementId) {
		Identifier id = Identifier.tryParse(criterionId);
		Component displayName;
		if (id != null) {
			displayName = Component.translatable("biome." + id.getNamespace() + "." + id.getPath());
		} else {
			displayName = Component.literal(criterionId);
		}
		return new CriterionDetail(criterionId, displayName, completed, obtainedTime, "biome");
	}
}
