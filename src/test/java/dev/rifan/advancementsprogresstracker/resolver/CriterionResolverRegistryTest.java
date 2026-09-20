package dev.rifan.advancementsprogresstracker.resolver;

import dev.rifan.advancementsprogresstracker.data.CriterionDetail;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CriterionResolverRegistryTest {

	@Test
	void testAdventuringTimeResolvesToBiome() {
		CriterionResolverRegistry registry = CriterionResolverRegistry.getInstance();
		CriterionDetail detail = registry.resolve("minecraft:plains", true, null, "minecraft:adventure/adventuring_time");

		assertNotNull(detail);
		assertEquals("minecraft:plains", detail.id());
		assertTrue(detail.completed());
		assertEquals("biome", detail.category());
	}

	@Test
	void testUnknownCriteriaFallsBackToGenericGracefully() {
		CriterionResolverRegistry registry = CriterionResolverRegistry.getInstance();
		CriterionDetail detail = registry.resolve("custom_mod:mysterious_task", false, null, "custom:mod_advancement");

		assertNotNull(detail);
		assertEquals("custom_mod:mysterious_task", detail.id());
		assertFalse(detail.completed());
		assertEquals("generic", detail.category());
		assertEquals("Mysterious Task", detail.displayName().getString());
	}
}
