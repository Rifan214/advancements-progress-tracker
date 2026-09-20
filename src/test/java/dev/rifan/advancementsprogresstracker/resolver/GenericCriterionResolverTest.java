package dev.rifan.advancementsprogresstracker.resolver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenericCriterionResolverTest {

	@Test
	void testFormatCriterionId() {
		assertEquals("Plains", GenericCriterionResolver.formatCriterionId("minecraft:plains"));
		assertEquals("Kill Mob", GenericCriterionResolver.formatCriterionId("adventure/kill_mob"));
		assertEquals("Entered Nether", GenericCriterionResolver.formatCriterionId("entered_nether"));
		assertEquals("Has Silk Touch", GenericCriterionResolver.formatCriterionId("has_silk_touch"));
		assertEquals("Unknown Criterion", GenericCriterionResolver.formatCriterionId(""));
		assertEquals("Unknown Criterion", GenericCriterionResolver.formatCriterionId(null));
	}
}
