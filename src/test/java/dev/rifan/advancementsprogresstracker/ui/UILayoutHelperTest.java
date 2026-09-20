package dev.rifan.advancementsprogresstracker.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.*;

class UILayoutHelperTest {

	// A mock width measurer simulating proportional font (e.g. 6px per character, 8px for 'W', 4px for 'i', etc.)
	private static final ToIntFunction<String> MOCK_FONT = s -> {
		int width = 0;
		for (char c : s.toCharArray()) {
			if (c == 'i' || c == '.' || c == 'l' || c == ' ') {
				width += 3;
			} else if (c == 'W' || c == 'M') {
				width += 8;
			} else {
				width += 6;
			}
		}
		return width;
	};

	@Test
	@DisplayName("Left panel width adapts responsively to window width and respects bounds [160, 260]")
	void testLeftPanelWidthScaling() {
		// Large 1920x1080 display
		assertEquals(260, UILayoutHelper.computeLeftPanelWidth(1920));

		// 1280x720 window
		assertEquals(260, UILayoutHelper.computeLeftPanelWidth(1280));

		// 960x600 window (960 * 0.32 = 307 -> clamped to 260)
		assertEquals(260, UILayoutHelper.computeLeftPanelWidth(960));

		// 600x450 small window (600 * 0.32 = 192)
		assertEquals(192, UILayoutHelper.computeLeftPanelWidth(600));

		// 500x350 window (500 * 0.32 = 160)
		assertEquals(160, UILayoutHelper.computeLeftPanelWidth(500));

		// 400x300 tiny window (400 * 0.32 = 128 -> clamped to 160)
		assertEquals(160, UILayoutHelper.computeLeftPanelWidth(400));
	}

	@Test
	@DisplayName("Toolbar stacks search and filter vertically when panel width < 220")
	void testToolbarStacking() {
		// Wide left panel -> horizontal layout
		assertFalse(UILayoutHelper.shouldStackToolbar(260));
		assertEquals(40, UILayoutHelper.computeHeaderHeight(false));

		assertFalse(UILayoutHelper.shouldStackToolbar(220));

		// Narrow left panel -> stacked layout
		assertTrue(UILayoutHelper.shouldStackToolbar(219));
		assertTrue(UILayoutHelper.shouldStackToolbar(192));
		assertTrue(UILayoutHelper.shouldStackToolbar(160));
		assertEquals(58, UILayoutHelper.computeHeaderHeight(true));
	}

	@Test
	@DisplayName("Progress card avoids text collision by switching between single-line and stacked layout")
	void testProgressCardLayoutResponsiveness() {
		int leftTextWidth = 110;  // "Requirements: 0 / 1 (0%)"
		int rightTextWidth = 105; // "0 of 3 criteria fulfilled"

		// Wide right panel (e.g. 350px available)
		int wideContainer = 350;
		assertTrue(UILayoutHelper.fitsSingleLineProgress(leftTextWidth, rightTextWidth, wideContainer));
		assertEquals(44, UILayoutHelper.calculateProgressCardHeight(true));

		// Narrow right panel (e.g. 220px available, would collide on single line: 110 + 16 + 105 = 231 > 204)
		int narrowContainer = 220;
		assertFalse(UILayoutHelper.fitsSingleLineProgress(leftTextWidth, rightTextWidth, narrowContainer));
		assertEquals(56, UILayoutHelper.calculateProgressCardHeight(false));
	}

	@Test
	@DisplayName("Advancement detail header card dynamically scales height with wrapped text")
	void testHeaderCardDynamicHeight() {
		// Minimum height for short 1-line title and 1-line description
		assertEquals(48, UILayoutHelper.calculateHeaderCardHeight(1, 1));

		// Multiple lines of title and description (e.g. long custom datapack advancement)
		int multiLineHeight = UILayoutHelper.calculateHeaderCardHeight(2, 3);
		// 10 + (2 * 11) + 4 + (3 * 10) + 10 = 76
		assertEquals(76, multiLineHeight);
		assertTrue(multiLineHeight > 48);
	}

	@Test
	@DisplayName("Text truncation properly adds ellipsis and does not exceed maximum width")
	void testTextTruncation() {
		String shortText = "Heart";
		assertEquals("Heart", UILayoutHelper.trimStringToWidth(MOCK_FONT, shortText, 100));

		String longText = "Place a Creaking Heart with the correct alignment between two Pale Oak Logs";
		int maxWidth = 120;
		String truncated = UILayoutHelper.trimStringToWidth(MOCK_FONT, longText, maxWidth);

		assertTrue(truncated.endsWith("..."));
		assertTrue(MOCK_FONT.applyAsInt(truncated) <= maxWidth, "Truncated text width must not exceed maxWidth");
	}

	@Test
	@DisplayName("Empty state text width calculation respects right panel boundaries")
	void testEmptyStateAvailableWidth() {
		// Wide right panel (e.g. 600px wide) -> available 568px
		assertEquals(568, UILayoutHelper.computeAvailableEmptyStateWidth(600));

		// Narrow right panel (e.g. 200px wide) -> available 168px
		assertEquals(168, UILayoutHelper.computeAvailableEmptyStateWidth(200));

		// Extremely small right panel (e.g. 80px wide) -> safe minimum 60px
		assertEquals(60, UILayoutHelper.computeAvailableEmptyStateWidth(80));
	}

	@Test
	@DisplayName("Search hint is guaranteed to fit within EditBox inner bounds")
	void testSearchHintFitsEditBox() {
		String longHint = "Search advancements or criteria...";
		int smallEditBoxWidth = 80;
		int maxInnerWidth = smallEditBoxWidth - 8;

		String safeHint = UILayoutHelper.trimStringToWidth(MOCK_FONT, longHint, maxInnerWidth);
		assertTrue(MOCK_FONT.applyAsInt(safeHint) <= maxInnerWidth, "Hint must not overflow EditBox inner width");
	}
}
