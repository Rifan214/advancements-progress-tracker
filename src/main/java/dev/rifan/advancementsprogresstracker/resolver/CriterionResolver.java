package dev.rifan.advancementsprogresstracker.resolver;

import dev.rifan.advancementsprogresstracker.data.CriterionDetail;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

/**
 * Strategy interface to resolve advancement criteria IDs into user-friendly names and metadata.
 */
public interface CriterionResolver {
	/**
	 * Priority of this resolver. Higher priority resolvers are evaluated first.
	 */
	default int priority() {
		return 0;
	}

	/**
	 * Determines whether this resolver can handle the given criterion.
	 */
	boolean canResolve(String criterionId, String advancementId);

	/**
	 * Resolves the criterion into a user-facing CriterionDetail.
	 */
	CriterionDetail resolve(String criterionId, boolean completed, @Nullable Instant obtainedTime, String advancementId);
}
