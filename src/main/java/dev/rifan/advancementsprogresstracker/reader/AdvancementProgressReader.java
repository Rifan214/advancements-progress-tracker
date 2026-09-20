package dev.rifan.advancementsprogresstracker.reader;

import dev.rifan.advancementsprogresstracker.data.AdvancementProgressInfo;
import dev.rifan.advancementsprogresstracker.data.CriterionDetail;
import dev.rifan.advancementsprogresstracker.data.ProgressStatus;
import dev.rifan.advancementsprogresstracker.resolver.CriterionResolverRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.CriterionProgress;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads and analyzes vanilla Advancement and AdvancementProgress into structured AdvancementProgressInfo.
 */
public class AdvancementProgressReader {
	private final CriterionResolverRegistry resolverRegistry;

	public AdvancementProgressReader() {
		this(CriterionResolverRegistry.getInstance());
	}

	public AdvancementProgressReader(CriterionResolverRegistry resolverRegistry) {
		this.resolverRegistry = resolverRegistry;
	}

	/**
	 * Reads an advancement with its optional player progress.
	 */
	public AdvancementProgressInfo read(AdvancementHolder holder, @Nullable AdvancementProgress progress) {
		Advancement advancement = holder.value();
		AdvancementRequirements requirements = advancement.requirements();
		Map<String, ?> criteriaMap = advancement.criteria();

		int total = requirements.size();
		if (total == 0) {
			total = criteriaMap.size();
		}
		if (total == 0) {
			total = 1;
		}

		int completed;
		if (progress != null && progress.isDone()) {
			completed = total;
		} else if (progress != null && progress.hasProgress()) {
			completed = requirements.count(criterionName -> {
				CriterionProgress cp = progress.getCriterion(criterionName);
				return cp != null && cp.isDone();
			});
		} else {
			completed = 0;
		}

		int remaining = Math.max(0, total - completed);
		float percent = total > 0 ? (float) completed / (float) total : (completed > 0 ? 1.0f : 0.0f);
		if (percent > 1.0f) {
			percent = 1.0f;
		}

		ProgressStatus status;
		if (completed >= total || (progress != null && progress.isDone())) {
			status = ProgressStatus.COMPLETED;
		} else if (completed > 0 || (progress != null && progress.hasProgress())) {
			status = ProgressStatus.IN_PROGRESS;
		} else {
			status = ProgressStatus.NOT_STARTED;
		}

		// Build individual criterion details
		String advIdStr = holder.id().toString();
		List<CriterionDetail> allCriteria = new ArrayList<>();
		List<CriterionDetail> completedList = new ArrayList<>();
		List<CriterionDetail> remainingList = new ArrayList<>();

		for (String criterionId : criteriaMap.keySet()) {
			CriterionProgress cp = progress != null ? progress.getCriterion(criterionId) : null;
			boolean isDone = cp != null && cp.isDone();
			Instant obtained = cp != null ? cp.getObtained() : null;

			CriterionDetail detail = resolverRegistry.resolve(criterionId, isDone, obtained, advIdStr);
			allCriteria.add(detail);
			if (isDone) {
				completedList.add(detail);
			} else {
				remainingList.add(detail);
			}
		}

		// Sort criteria alphabetically by display name string
		allCriteria.sort((a, b) -> a.displayName().getString().compareToIgnoreCase(b.displayName().getString()));
		completedList.sort((a, b) -> a.displayName().getString().compareToIgnoreCase(b.displayName().getString()));
		remainingList.sort((a, b) -> a.displayName().getString().compareToIgnoreCase(b.displayName().getString()));

		return new AdvancementProgressInfo(
			holder,
			total,
			completed,
			remaining,
			percent,
			status,
			allCriteria,
			completedList,
			remainingList
		);
	}
}
