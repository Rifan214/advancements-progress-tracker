package dev.rifan.advancementsprogresstracker.resolver;

import dev.rifan.advancementsprogresstracker.data.CriterionDetail;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CriterionResolverRegistryTest {

	@BeforeAll
	static void setup() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

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
	void testAdventuringTimeResolvesDappledForestBiome() {
		CriterionResolverRegistry registry = CriterionResolverRegistry.getInstance();
		CriterionDetail detail = registry.resolve("minecraft:dappled_forest", true, null, "minecraft:adventure/adventuring_time");

		assertNotNull(detail);
		assertEquals("minecraft:dappled_forest", detail.id());
		assertTrue(detail.completed());
		assertEquals("biome", detail.category());
		assertEquals("Dappled Forest", detail.displayName().getString());
	}

	@Test
	void testNetherExploreResolvesToBiome() {
		CriterionResolverRegistry registry = CriterionResolverRegistry.getInstance();
		CriterionDetail detail = registry.resolve("minecraft:nether_wastes", true, null, "minecraft:nether/explore_nether");

		assertNotNull(detail);
		assertEquals("minecraft:nether_wastes", detail.id());
		assertTrue(detail.completed());
		assertEquals("biome", detail.category());
	}

	@Test
	void testArmorTrimResolvesToTrimPattern() {
		CriterionResolverRegistry registry = CriterionResolverRegistry.getInstance();
		CriterionDetail detail = registry.resolve(
			"armor_trimmed_minecraft:silence_armor_trim_smithing_template_smithing_trim",
			true,
			null,
			"minecraft:adventure/trim_with_all_exclusive_armor_patterns"
		);

		assertNotNull(detail);
		assertEquals("trim", detail.category());
	}

	@Test
	void testCatVariantResolvesProperly() {
		CriterionResolverRegistry registry = CriterionResolverRegistry.getInstance();
		CriterionDetail detail = registry.resolve(
			"minecraft:british_shorthair",
			true,
			null,
			"minecraft:husbandry/complete_catalogue"
		);

		assertNotNull(detail);
		assertEquals("cat_variant", detail.category());
		assertTrue(detail.displayName().getString().contains("British Shorthair"));
	}

	@Test
	void testWolfVariantResolvesProperly() {
		CriterionResolverRegistry registry = CriterionResolverRegistry.getInstance();
		CriterionDetail detail = registry.resolve(
			"minecraft:ashen",
			false,
			null,
			"minecraft:husbandry/whole_pack"
		);

		assertNotNull(detail);
		assertEquals("wolf_variant", detail.category());
		assertTrue(detail.displayName().getString().contains("Ashen"));
	}

	@Test
	void testBalancedDietResolvesToItem() {
		CriterionResolverRegistry registry = CriterionResolverRegistry.getInstance();
		CriterionDetail detail = registry.resolve(
			"baked_potato",
			true,
			null,
			"minecraft:husbandry/balanced_diet"
		);

		assertNotNull(detail);
		assertEquals("item", detail.category());
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
