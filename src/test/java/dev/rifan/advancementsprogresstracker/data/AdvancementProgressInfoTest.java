package dev.rifan.advancementsprogresstracker.data;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AdvancementProgressInfoTest {

	@BeforeAll
	public static void setup() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	private AdvancementHolder createDummyHolder(String path) {
		Advancement dummyAdvancement = new Advancement(
			Optional.empty(),
			Optional.empty(),
			net.minecraft.advancements.AdvancementRewards.EMPTY,
			Map.of(),
			AdvancementRequirements.EMPTY,
			false
		);
		return new AdvancementHolder(Identifier.fromNamespaceAndPath("minecraft", path), dummyAdvancement);
	}

	@Test
	public void testProgressCalculationsForMultiCriteria() {
		AdvancementHolder holder = createDummyHolder("adventure/adventuring_time");
		List<CriterionDetail> all = new ArrayList<>();
		List<CriterionDetail> completed = new ArrayList<>();
		List<CriterionDetail> remaining = new ArrayList<>();

		for (int i = 0; i < 35; i++) {
			CriterionDetail c = new CriterionDetail("biome_" + i, Component.literal("Biome " + i), true, Instant.now(), "biome");
			all.add(c);
			completed.add(c);
		}
		for (int i = 35; i < 53; i++) {
			CriterionDetail c = new CriterionDetail("biome_" + i, Component.literal("Biome " + i), false, null, "biome");
			all.add(c);
			remaining.add(c);
		}

		float percent = 35.0f / 53.0f;
		AdvancementProgressInfo info = new AdvancementProgressInfo(
			holder,
			53,
			35,
			18,
			percent,
			ProgressStatus.IN_PROGRESS,
			all,
			completed,
			remaining
		);

		assertEquals(53, info.totalRequirements());
		assertEquals(35, info.completedRequirements());
		assertEquals(18, info.remainingRequirements());
		assertEquals("35 / 53", info.getProgressFormatted());
		assertEquals(66, info.getPercentInt());
		assertEquals(ProgressStatus.IN_PROGRESS, info.status());
		assertFalse(info.isCompleted());
		assertTrue(info.isMultiCriteria());
		assertEquals(35, info.completedCriteria().size());
		assertEquals(18, info.remainingCriteria().size());
		assertEquals(53, info.criteria().size());
	}

	@Test
	public void testFullyCompletedAdvancement() {
		AdvancementHolder holder = createDummyHolder("adventure/kill_a_mob");
		CriterionDetail detail = new CriterionDetail("killed_zombie", Component.literal("Kill Zombie"), true, Instant.now());

		AdvancementProgressInfo info = new AdvancementProgressInfo(
			holder,
			1,
			1,
			0,
			1.0f,
			ProgressStatus.COMPLETED,
			List.of(detail),
			List.of(detail),
			List.of()
		);

		assertEquals("1 / 1", info.getProgressFormatted());
		assertEquals(100, info.getPercentInt());
		assertTrue(info.isCompleted());
		assertFalse(info.isMultiCriteria());
		assertEquals(1, info.completedCriteria().size());
		assertEquals(0, info.remainingCriteria().size());
	}

	@Test
	public void testNotStartedAdvancement() {
		AdvancementHolder holder = createDummyHolder("adventure/arbalistic");
		CriterionDetail detail = new CriterionDetail("arbalistic", Component.literal("Arbalistic"), false, null);

		AdvancementProgressInfo info = new AdvancementProgressInfo(
			holder,
			1,
			0,
			1,
			0.0f,
			ProgressStatus.NOT_STARTED,
			List.of(detail),
			List.of(),
			List.of(detail)
		);

		assertEquals("0 / 1", info.getProgressFormatted());
		assertEquals(0, info.getPercentInt());
		assertFalse(info.isCompleted());
		assertEquals(ProgressStatus.NOT_STARTED, info.status());
	}

	@Test
	public void testListImmutability() {
		AdvancementHolder holder = createDummyHolder("test");
		List<CriterionDetail> modifiableList = new ArrayList<>();
		modifiableList.add(new CriterionDetail("c1", Component.literal("C1"), true, null));

		AdvancementProgressInfo info = new AdvancementProgressInfo(
			holder,
			1,
			1,
			0,
			1.0f,
			ProgressStatus.COMPLETED,
			modifiableList,
			modifiableList,
			List.of()
		);

		assertThrows(UnsupportedOperationException.class, () -> {
			info.criteria().add(new CriterionDetail("c2", Component.literal("C2"), false, null));
		});
	}
}
