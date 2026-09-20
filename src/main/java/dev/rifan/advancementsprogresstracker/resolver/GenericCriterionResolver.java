package dev.rifan.advancementsprogresstracker.resolver;

import dev.rifan.advancementsprogresstracker.data.CriterionDetail;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

public class GenericCriterionResolver implements CriterionResolver {
	@Override
	public int priority() {
		return Integer.MIN_VALUE;
	}

	@Override
	public boolean canResolve(String criterionId, String advancementId) {
		return true; // Always matches as fallback
	}

	@Override
	public CriterionDetail resolve(String criterionId, boolean completed, @Nullable Instant obtainedTime, String advancementId) {
		String humanReadable = formatCriterionId(criterionId);
		return new CriterionDetail(criterionId, Component.literal(humanReadable), completed, obtainedTime, "generic");
	}

	public static String formatCriterionId(String id) {
		if (id == null || id.isBlank()) {
			return "Unknown Criterion";
		}
		// Strip namespace if present (e.g. "minecraft:plains" -> "plains")
		int colon = id.indexOf(':');
		String path = colon >= 0 ? id.substring(colon + 1) : id;

		// Strip folder prefixes if any (e.g. "adventure/kill_mob" -> "kill_mob")
		int slash = path.lastIndexOf('/');
		if (slash >= 0) {
			path = path.substring(slash + 1);
		}

		// Replace underscores and dots with spaces, and capitalize words
		String[] parts = path.replace('_', ' ').replace('.', ' ').split("\\s+");
		return Arrays.stream(parts)
			.filter(s -> !s.isEmpty())
			.map(s -> Character.toUpperCase(s.charAt(0)) + (s.length() > 1 ? s.substring(1) : ""))
			.collect(Collectors.joining(" "));
	}
}
