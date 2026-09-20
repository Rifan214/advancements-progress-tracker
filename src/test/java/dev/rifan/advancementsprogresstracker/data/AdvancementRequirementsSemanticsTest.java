package dev.rifan.advancementsprogresstracker.data;

import net.minecraft.SharedConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
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
}
