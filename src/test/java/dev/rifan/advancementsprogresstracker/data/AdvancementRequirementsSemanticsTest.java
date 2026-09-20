package dev.rifan.advancementsprogresstracker.data;

import dev.rifan.advancementsprogresstracker.reader.AdvancementProgressReader;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AdvancementRequirementsSemanticsTest {

	@BeforeAll
	static void setup() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	private AdvancementHolder createHolder(String path, AdvancementRequirements reqs) {
		Advancement adv = new Advancement(
			Optional.empty(),
			Optional.empty(),
			AdvancementRewards.EMPTY,
			Map.of(),
			reqs,
			false
		);
		return new AdvancementHolder(Identifier.fromNamespaceAndPath("minecraft", path), adv);
	}

	@Test
	void testStrictAndAdvancementSemantics() {
		// Strict AND: 3 requirements, each with 1 criterion
		AdvancementRequirements andReqs = AdvancementRequirements.allOf(List.of("c1", "c2", "c3"));
		assertEquals(3, andReqs.size());

		Set<String> completedCriteria = Set.of("c1");
		int completedCount = andReqs.count(completedCriteria::contains);
		assertEquals(1, completedCount);
		assertFalse(andReqs.test(completedCriteria::contains));

		// All 3 completed
		Set<String> allCompleted = Set.of("c1", "c2", "c3");
		assertEquals(3, andReqs.count(allCompleted::contains));
		assertTrue(andReqs.test(allCompleted::contains));
	}

	@Test
	void testStrictOrAdvancementSemantics() {
		// Strict OR: 1 requirement group containing 3 alternative criteria
		AdvancementRequirements orReqs = AdvancementRequirements.anyOf(List.of("mob_a", "mob_b", "mob_c"));
		assertEquals(1, orReqs.size());

		// When 1 alternative is completed, requirement is satisfied (1/1)
		Set<String> oneCompleted = Set.of("mob_a");
		assertEquals(1, orReqs.count(oneCompleted::contains));
		assertTrue(orReqs.test(oneCompleted::contains));

		// When 2 alternatives are completed, requirement count is STILL 1
		Set<String> twoCompleted = Set.of("mob_a", "mob_b");
		assertEquals(1, orReqs.count(twoCompleted::contains));
		assertTrue(orReqs.test(twoCompleted::contains));
	}

	@Test
	void testMixedCnfAdvancementSemantics() {
		// Mixed (CNF) like salvage_sherd:
		// Group 0 (Any 1 of 3 ruins): [desert_pyramid, desert_well, ocean_ruin]
		// Group 1 (Must have sherd): [has_sherd]
		AdvancementRequirements mixedReqs = new AdvancementRequirements(List.of(
			List.of("desert_pyramid", "desert_well", "ocean_ruin"),
			List.of("has_sherd")
		));

		assertEquals(2, mixedReqs.size());

		// Case 1: Visited only 1 ruin -> 1/2 requirements
		Set<String> ruinOnly = Set.of("desert_pyramid");
		assertEquals(1, mixedReqs.count(ruinOnly::contains));
		assertFalse(mixedReqs.test(ruinOnly::contains));

		// Case 2: Visited 2 ruins, but no sherd -> STILL 1/2 requirements!
		Set<String> twoRuins = Set.of("desert_pyramid", "desert_well");
		assertEquals(1, mixedReqs.count(twoRuins::contains));
		assertFalse(mixedReqs.test(twoRuins::contains));

		// Case 3: Visited 1 ruin AND obtained sherd -> 2/2 requirements (Done!)
		Set<String> ruinAndSherd = Set.of("desert_pyramid", "has_sherd");
		assertEquals(2, mixedReqs.count(ruinAndSherd::contains));
		assertTrue(mixedReqs.test(ruinAndSherd::contains));
	}

	@Test
	void testAdvancementProgressInfoOrSemanticsFlags() {
		AdvancementHolder holder = createHolder(
			"adventure/kill_a_mob",
			AdvancementRequirements.anyOf(List.of("zombie", "skeleton"))
		);

		List<CriterionDetail> criteria = List.of(
			new CriterionDetail("zombie", Component.literal("Zombie"), true, null),
			new CriterionDetail("skeleton", Component.literal("Skeleton"), false, null)
		);

		AdvancementProgressInfo info = new AdvancementProgressInfo(
			holder,
			1, // totalRequirements
			1, // completedRequirements
			0, // remainingRequirements
			1.0f,
			ProgressStatus.COMPLETED,
			criteria,
			List.of(criteria.get(0)),
			List.of(criteria.get(1))
		);

		assertFalse(info.isMultiRequirement());
		assertTrue(info.isMultiCriteria());
		assertTrue(info.isOrRequirement());
		assertEquals(2, info.totalCriteria());
		assertEquals(1, info.completedCriteriaCount());
		assertEquals("1 / 1", info.getProgressFormatted());
	}

	@Test
	void testCompletedRemainingCriteriaCountRegression() {
		// Test 1: 3 completed, 52 remaining, 55 total criteria
		List<String> criterionIds = new java.util.ArrayList<>();
		for (int i = 0; i < 55; i++) {
			criterionIds.add("c_" + i);
		}
		AdvancementRequirements reqs = AdvancementRequirements.allOf(criterionIds);
		AdvancementHolder holder = createHolder("test/strict_and_55", reqs);

		AdvancementProgress progress = new AdvancementProgress();
		progress.update(reqs);
		progress.grantProgress("c_0");
		progress.grantProgress("c_1");
		progress.grantProgress("c_2");

		AdvancementProgressReader reader = new AdvancementProgressReader();
		AdvancementProgressInfo info = reader.read(holder, progress);

		assertEquals(55, info.totalRequirements());
		assertEquals(3, info.completedRequirements());
		assertEquals(52, info.remainingRequirements());
		assertEquals(55, info.totalCriteria());
		assertEquals(3, info.completedCriteria().size());
		assertEquals(52, info.remainingCriteria().size());
		assertTrue(info.completedCriteria().stream().allMatch(CriterionDetail::completed));
		assertTrue(info.remainingCriteria().stream().noneMatch(CriterionDetail::completed));
	}

	@Test
	void testBalancedDietItemResolutionAndDifferentiation() {
		// Test 2: Balanced Diet item differentiation and icon resolution
		List<String> foods = List.of("rotten_flesh", "apple", "bread");
		AdvancementRequirements reqs = AdvancementRequirements.allOf(foods);
		AdvancementHolder holder = createHolder("husbandry/balanced_diet", reqs);

		AdvancementProgress progress = new AdvancementProgress();
		progress.update(reqs);
		progress.grantProgress("rotten_flesh");

		AdvancementProgressReader reader = new AdvancementProgressReader();
		AdvancementProgressInfo info = reader.read(holder, progress);

		assertEquals(1, info.completedCriteria().size());
		CriterionDetail completedItem = info.completedCriteria().get(0);
		assertEquals("rotten_flesh", completedItem.id());
		assertEquals("item", completedItem.category());
		assertTrue(completedItem.completed());

		assertEquals(2, info.remainingCriteria().size());
		assertTrue(info.remainingCriteria().stream().anyMatch(c -> c.id().equals("apple") && c.category().equals("item") && !c.completed()));
		assertTrue(info.remainingCriteria().stream().anyMatch(c -> c.id().equals("bread") && c.category().equals("item") && !c.completed()));
	}

	@Test
	void testAdventuringTimeBiomeResolutionRegression() {
		// Test 3: Adventuring Time 3 completed biomes, 52 remaining biomes
		List<String> biomes = new java.util.ArrayList<>();
		biomes.add("minecraft:plains");
		biomes.add("minecraft:desert");
		biomes.add("minecraft:forest");
		for (int i = 3; i < 55; i++) {
			biomes.add("minecraft:biome_" + i);
		}
		AdvancementRequirements reqs = AdvancementRequirements.allOf(biomes);
		AdvancementHolder holder = createHolder("adventure/adventuring_time", reqs);

		AdvancementProgress progress = new AdvancementProgress();
		progress.update(reqs);
		progress.grantProgress("minecraft:plains");
		progress.grantProgress("minecraft:desert");
		progress.grantProgress("minecraft:forest");

		AdvancementProgressReader reader = new AdvancementProgressReader();
		AdvancementProgressInfo info = reader.read(holder, progress);

		assertEquals(55, info.totalRequirements());
		assertEquals(3, info.completedRequirements());
		assertEquals(52, info.remainingRequirements());
		assertEquals(3, info.completedCriteria().size());
		assertEquals(52, info.remainingCriteria().size());
		assertEquals("3 / 55", info.getProgressFormatted());
		assertTrue(info.completedCriteria().stream().allMatch(c -> c.category().equals("biome")));
	}

	@Test
	void testOrRequirementSemanticsRegression() {
		// Test 4: OR requirement - 1/1 requirement, 1/41 criteria completed
		List<String> mobs = new java.util.ArrayList<>();
		mobs.add("zombie");
		for (int i = 1; i < 41; i++) {
			mobs.add("mob_" + i);
		}
		AdvancementRequirements reqs = AdvancementRequirements.anyOf(mobs);
		AdvancementHolder holder = createHolder("adventure/kill_a_mob", reqs);

		AdvancementProgress progress = new AdvancementProgress();
		progress.update(reqs);
		progress.grantProgress("zombie");

		AdvancementProgressReader reader = new AdvancementProgressReader();
		AdvancementProgressInfo info = reader.read(holder, progress);

		// Requirement progress must remain 1 / 1
		assertEquals(1, info.totalRequirements());
		assertEquals(1, info.completedRequirements());
		assertEquals(0, info.remainingRequirements());
		assertTrue(info.isCompleted());
		assertEquals("1 / 1", info.getProgressFormatted());

		// Criterion coverage must show 1 completed and 40 remaining
		assertEquals(41, info.totalCriteria());
		assertEquals(1, info.completedCriteriaCount());
		assertEquals(1, info.completedCriteria().size());
		assertEquals(40, info.remainingCriteria().size());
		assertEquals("zombie", info.completedCriteria().get(0).id());
	}

	@Test
	void testMixedRequirementProgressVsCoverageRegression() {
		// Test 5: Mixed requirement progress vs criterion coverage
		List<String> ruins = List.of("desert_pyramid", "desert_well", "ocean_ruin", "trail_ruins", "cold_ocean_ruin", "warm_ocean_ruin");
		AdvancementRequirements mixedReqs = new AdvancementRequirements(List.of(
			ruins,
			List.of("has_sherd")
		));
		AdvancementHolder holder = createHolder("adventure/salvage_sherd", mixedReqs);

		AdvancementProgress progress = new AdvancementProgress();
		progress.update(mixedReqs);
		progress.grantProgress("desert_pyramid");

		AdvancementProgressReader reader = new AdvancementProgressReader();
		AdvancementProgressInfo info = reader.read(holder, progress);

		// 1 of 2 requirements met (50%)
		assertEquals(2, info.totalRequirements());
		assertEquals(1, info.completedRequirements());
		assertEquals(1, info.remainingRequirements());
		assertEquals(0.5f, info.percent(), 0.001f);
		assertEquals("1 / 2", info.getProgressFormatted());

		// 1 of 7 criteria met
		assertEquals(7, info.totalCriteria());
		assertEquals(1, info.completedCriteriaCount());
		assertEquals(1, info.completedCriteria().size());
		assertEquals(6, info.remainingCriteria().size());
		assertEquals("desert_pyramid", info.completedCriteria().get(0).id());
	}
}
