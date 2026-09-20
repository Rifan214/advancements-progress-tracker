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
		if (advancementId.contains("kill_all_mobs") || advancementId.contains("bred_all_animals") || advancementId.contains("tame_all_animals")) {
			return true;
		}
		return false;
	}

	@Override
	public CriterionDetail resolve(String criterionId, boolean completed, @Nullable Instant obtainedTime, String advancementId) {
		Identifier id = Identifier.tryParse(criterionId);
		Component displayName;
		if (id != null) {
			displayName = Component.translatable("entity." + id.getNamespace() + "." + id.getPath());
		} else {
			displayName = Component.literal(criterionId);
		}
		return new CriterionDetail(criterionId, displayName, completed, obtainedTime, "entity");
	}
}
