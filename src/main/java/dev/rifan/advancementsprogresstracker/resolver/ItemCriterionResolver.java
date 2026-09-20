package dev.rifan.advancementsprogresstracker.resolver;

import dev.rifan.advancementsprogresstracker.data.CriterionDetail;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class ItemCriterionResolver implements CriterionResolver {
	@Override
	public int priority() {
		return 70;
	}

	@Override
	public boolean canResolve(String criterionId, String advancementId) {
		if (advancementId.contains("balanced_diet")
			|| advancementId.contains("trim_with_all")
			|| advancementId.contains("smithing_with_style")
			|| advancementId.contains("crafting_a_new_look")) {
			return true;
		}
		return false;
	}

	@Override
	public CriterionDetail resolve(String criterionId, boolean completed, @Nullable Instant obtainedTime, String advancementId) {
		Identifier id = Identifier.tryParse(criterionId);
		Component displayName;
		if (id != null) {
			displayName = Component.translatable("item." + id.getNamespace() + "." + id.getPath());
		} else {
			displayName = Component.literal(criterionId);
		}
		return new CriterionDetail(criterionId, displayName, completed, obtainedTime, "item");
	}
}
