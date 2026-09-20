package dev.rifan.advancementsprogresstracker.data;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the resolved progress and presentation state of an advancement.
 */
public record AdvancementProgressInfo(
	AdvancementHolder holder,
	int totalRequirements,
	int completedRequirements,
	int remainingRequirements,
	float percent,
	ProgressStatus status,
	List<CriterionDetail> criteria,
	List<CriterionDetail> completedCriteria,
	List<CriterionDetail> remainingCriteria
) {
	public AdvancementProgressInfo {
		criteria = Collections.unmodifiableList(criteria);
		completedCriteria = Collections.unmodifiableList(completedCriteria);
		remainingCriteria = Collections.unmodifiableList(remainingCriteria);
	}

	public Identifier id() {
		return holder.id();
	}

	public Advancement advancement() {
		return holder.value();
	}

	@Nullable
	public DisplayInfo display() {
		return holder.value().display().orElse(null);
	}

	public Component title() {
		DisplayInfo displayInfo = display();
		if (displayInfo != null) {
			return displayInfo.getTitle();
		}
		return holder.value().name().orElse(Component.literal(holder.id().toString()));
	}

	public Component description() {
		DisplayInfo displayInfo = display();
		if (displayInfo != null) {
			return displayInfo.getDescription();
		}
		return Component.empty();
	}

	public AdvancementType type() {
		DisplayInfo displayInfo = display();
		return displayInfo != null ? displayInfo.getType() : AdvancementType.TASK;
	}

	public boolean isCompleted() {
		return status == ProgressStatus.COMPLETED;
	}

	public boolean isMultiCriteria() {
		return totalRequirements > 1;
	}

	public String getProgressFormatted() {
		return completedRequirements + " / " + totalRequirements;
	}

	public int getPercentInt() {
		return Math.round(percent * 100.0f);
	}
}
