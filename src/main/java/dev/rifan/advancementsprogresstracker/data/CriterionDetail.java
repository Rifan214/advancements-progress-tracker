package dev.rifan.advancementsprogresstracker.data;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents the resolved details and completion status of a single criterion.
 */
public record CriterionDetail(
	String id,
	Component displayName,
	boolean completed,
	@Nullable Instant obtainedTime,
	String category
) {
	public CriterionDetail {
		Objects.requireNonNull(id, "id must not be null");
		Objects.requireNonNull(displayName, "displayName must not be null");
		if (category == null) {
			category = "generic";
		}
	}

	public CriterionDetail(String id, Component displayName, boolean completed, @Nullable Instant obtainedTime) {
		this(id, displayName, completed, obtainedTime, "generic");
	}
}
