package dev.rifan.advancementsprogresstracker.resolver;

import dev.rifan.advancementsprogresstracker.data.CriterionDetail;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CriterionResolverRegistry {
	private static final CriterionResolverRegistry INSTANCE = new CriterionResolverRegistry();

	private final List<CriterionResolver> resolvers = new ArrayList<>();
	private final GenericCriterionResolver fallbackResolver = new GenericCriterionResolver();

	private CriterionResolverRegistry() {
		register(new BiomeCriterionResolver());
		register(new EntityCriterionResolver());
		register(new ItemCriterionResolver());
		register(fallbackResolver);
	}

	public static CriterionResolverRegistry getInstance() {
		return INSTANCE;
	}

	public synchronized void register(CriterionResolver resolver) {
		resolvers.add(resolver);
		resolvers.sort(Comparator.comparingInt(CriterionResolver::priority).reversed());
	}

	public CriterionDetail resolve(String criterionId, boolean completed, @Nullable Instant obtainedTime, String advancementId) {
		for (CriterionResolver resolver : resolvers) {
			try {
				if (resolver.canResolve(criterionId, advancementId)) {
					return resolver.resolve(criterionId, completed, obtainedTime, advancementId);
				}
			} catch (Throwable ignored) {
				// Prevent faulty custom resolver from breaking the tracker
			}
		}
		return fallbackResolver.resolve(criterionId, completed, obtainedTime, advancementId);
	}
}
