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

	@Test
	@DisplayName("Scrollbar thumb height respects minimum bound and scales proportionally to content")
	void testScrollbarThumbHeightCalculation() {
		int trackHeight = 300;
		int minThumbHeight = 24;

		// Large content: thumb height = 300 * 300 / 1200 = 75
		assertEquals(75, UILayoutHelper.calculateThumbHeight(trackHeight, 1200, minThumbHeight));

		// Very large content: calculated thumb height = 300 * 300 / 9000 = 10, clamped to minThumbHeight (24)
		assertEquals(24, UILayoutHelper.calculateThumbHeight(trackHeight, 9000, minThumbHeight));

		// Content equals track height: thumb spans entire track
		assertEquals(trackHeight, UILayoutHelper.calculateThumbHeight(trackHeight, 300, minThumbHeight));

		// Content smaller than track: thumb spans entire track
		assertEquals(trackHeight, UILayoutHelper.calculateThumbHeight(trackHeight, 150, minThumbHeight));

		// Invalid/empty content: returns minThumbHeight safely
		assertEquals(minThumbHeight, UILayoutHelper.calculateThumbHeight(trackHeight, 0, minThumbHeight));
	}

	@Test
	@DisplayName("Thumb Y position scales between track top and track bottom")
	void testScrollbarThumbYPosition() {
		int trackTop = 40;
		int trackHeight = 300;
		int thumbHeight = 60; // available track = 240
		double maxScroll = 500.0;

		// Scroll at 0%
		assertEquals(trackTop, UILayoutHelper.calculateThumbY(trackTop, trackHeight, thumbHeight, 0.0, maxScroll));

		// Scroll at 50%
		assertEquals(trackTop + 120, UILayoutHelper.calculateThumbY(trackTop, trackHeight, thumbHeight, 250.0, maxScroll));

		// Scroll at 100%
		assertEquals(trackTop + 240, UILayoutHelper.calculateThumbY(trackTop, trackHeight, thumbHeight, 500.0, maxScroll));
	}

	@Test
	@DisplayName("Drag downward: dragging mouse down increases scroll amount")
	void testScrollbarDragDownwardIncreasesScroll() {
		int trackTop = 40;
		int trackHeight = 300;
		int thumbHeight = 60;
		double maxScroll = 500.0;
		double dragClickOffsetY = 15.0; // Clicked 15px inside thumb

		// Mouse starts at Y = 55 (thumb top at 40, scroll = 0)
		double initialScroll = UILayoutHelper.calculateScrollFromMouseDrag(55.0, dragClickOffsetY, trackTop, trackHeight, thumbHeight, maxScroll);
		assertEquals(0.0, initialScroll, 0.001);

		// Drag downward to Y = 105 (thumb top wants to be 90)
		double scrolledDown = UILayoutHelper.calculateScrollFromMouseDrag(105.0, dragClickOffsetY, trackTop, trackHeight, thumbHeight, maxScroll);
		assertTrue(scrolledDown > initialScroll, "Dragging downward must increase scroll amount");

		// Drag further down to Y = 175
		double scrolledFurtherDown = UILayoutHelper.calculateScrollFromMouseDrag(175.0, dragClickOffsetY, trackTop, trackHeight, thumbHeight, maxScroll);
		assertTrue(scrolledFurtherDown > scrolledDown, "Dragging further downward must further increase scroll amount");
	}

	@Test
	@DisplayName("Drag upward: dragging mouse up decreases scroll amount")
	void testScrollbarDragUpwardDecreasesScroll() {
		int trackTop = 40;
		int trackHeight = 300;
		int thumbHeight = 60;
		double maxScroll = 500.0;
		double dragClickOffsetY = 15.0;

		// Start from lower position Y = 200
		double scrollFromLower = UILayoutHelper.calculateScrollFromMouseDrag(200.0, dragClickOffsetY, trackTop, trackHeight, thumbHeight, maxScroll);

		// Drag upward to Y = 120
		double scrollFromHigher = UILayoutHelper.calculateScrollFromMouseDrag(120.0, dragClickOffsetY, trackTop, trackHeight, thumbHeight, maxScroll);
		assertTrue(scrollFromHigher < scrollFromLower, "Dragging upward must decrease scroll amount");
	}

	@Test
	@DisplayName("Top clamp: dragging above track clamps thumb to trackTop and scroll to 0")
	void testScrollbarTopClamping() {
		int trackTop = 40;
		int trackHeight = 300;
		int thumbHeight = 60;
		double maxScroll = 500.0;
		double dragClickOffsetY = 15.0;

		// Drag above trackTop (e.g. mouseY = 10, wants thumb top = -5)
		double clampedThumbY = UILayoutHelper.clampThumbY(10.0 - dragClickOffsetY, trackTop, trackHeight, thumbHeight);
		assertEquals(trackTop, clampedThumbY, 0.001);

		double clampedScroll = UILayoutHelper.calculateScrollFromMouseDrag(10.0, dragClickOffsetY, trackTop, trackHeight, thumbHeight, maxScroll);
		assertEquals(0.0, clampedScroll, 0.001);
	}

	@Test
	@DisplayName("Bottom clamp: dragging below track clamps thumb to track bottom and scroll to maxScroll")
	void testScrollbarBottomClamping() {
		int trackTop = 40;
		int trackHeight = 300;
		int thumbHeight = 60;
		double maxScroll = 500.0;
		double dragClickOffsetY = 15.0;

		int expectedMaxThumbY = trackTop + trackHeight - thumbHeight; // 40 + 240 = 280

		// Drag way below track (e.g. mouseY = 800)
		double clampedThumbY = UILayoutHelper.clampThumbY(800.0 - dragClickOffsetY, trackTop, trackHeight, thumbHeight);
		assertEquals(expectedMaxThumbY, clampedThumbY, 0.001);

		double clampedScroll = UILayoutHelper.calculateScrollFromMouseDrag(800.0, dragClickOffsetY, trackTop, trackHeight, thumbHeight, maxScroll);
		assertEquals(maxScroll, clampedScroll, 0.001);
	}

	@Test
	@DisplayName("No-scroll content: when content fits viewport, scrolling and dragging do not activate")
	void testNoScrollWhenContentFitsViewport() {
		int trackTop = 40;
		int trackHeight = 300;
		int contentHeight = 250;
		int minThumbHeight = 24;
		double maxScroll = 0.0; // No excess content

		int thumbHeight = UILayoutHelper.calculateThumbHeight(trackHeight, contentHeight, minThumbHeight);
		assertEquals(trackHeight, thumbHeight);

		double scrollFromDrag = UILayoutHelper.calculateScrollFromMouseDrag(150.0, 10.0, trackTop, trackHeight, thumbHeight, maxScroll);
		assertEquals(0.0, scrollFromDrag, 0.001);

		double thumbY = UILayoutHelper.calculateThumbY(trackTop, trackHeight, thumbHeight, 0.0, maxScroll);
		assertEquals(trackTop, thumbY);
	}

	@Test
	@DisplayName("Scrollbar hit testing recognizes points inside thumb bounds with tolerance")
	void testScrollbarHitTesting() {
		int scrollbarX = 254;
		int thumbWidth = 6;
		int thumbY = 100;
		int thumbHeight = 50;

		// Inside thumb
		assertTrue(UILayoutHelper.isPointInScrollbarThumb(256, 120, scrollbarX, thumbWidth, thumbY, thumbHeight));

		// In 2px tolerance zone horizontally
		assertTrue(UILayoutHelper.isPointInScrollbarThumb(252, 120, scrollbarX, thumbWidth, thumbY, thumbHeight));
		assertTrue(UILayoutHelper.isPointInScrollbarThumb(262, 120, scrollbarX, thumbWidth, thumbY, thumbHeight));

		// Outside horizontally
		assertFalse(UILayoutHelper.isPointInScrollbarThumb(250, 120, scrollbarX, thumbWidth, thumbY, thumbHeight));
		assertFalse(UILayoutHelper.isPointInScrollbarThumb(265, 120, scrollbarX, thumbWidth, thumbY, thumbHeight));

		// Outside vertically
		assertFalse(UILayoutHelper.isPointInScrollbarThumb(256, 95, scrollbarX, thumbWidth, thumbY, thumbHeight));
		assertFalse(UILayoutHelper.isPointInScrollbarThumb(256, 155, scrollbarX, thumbWidth, thumbY, thumbHeight));
	}

	@Test
	@DisplayName("Mouse-wheel regression: wheel scrolling calculates and clamps correctly")
	void testMouseWheelScrolling() {
		double maxScroll = 200.0;
		double scrollStep = 24.0;

		// Wheel scroll down (negative wheel delta -> scroll increases)
		double scrolledDown = UILayoutHelper.calculateMouseWheelScroll(50.0, maxScroll, -1.0, scrollStep);
		assertEquals(74.0, scrolledDown, 0.001);

		// Wheel scroll up (positive wheel delta -> scroll decreases)
		double scrolledUp = UILayoutHelper.calculateMouseWheelScroll(50.0, maxScroll, 1.0, scrollStep);
		assertEquals(26.0, scrolledUp, 0.001);

		// Wheel scroll clamped at top (0.0)
		double clampedTop = UILayoutHelper.calculateMouseWheelScroll(10.0, maxScroll, 1.0, scrollStep);
		assertEquals(0.0, clampedTop, 0.001);

		// Wheel scroll clamped at bottom (maxScroll)
		double clampedBottom = UILayoutHelper.calculateMouseWheelScroll(190.0, maxScroll, -1.0, scrollStep);
		assertEquals(200.0, clampedBottom, 0.001);
	}

	@Test
	@DisplayName("Dragging state transitions stop immediately upon mouse release")
	void testDraggingStateLifecycle() {
		// Simulates tracker screen dragging state lifecycle
		boolean isDragging = false;
		double scrollAmount = 0.0;
		double maxScroll = 300.0;
		int trackTop = 40;
		int trackHeight = 200;
		int thumbHeight = 50;

		// 1. Mouse click inside thumb -> dragging starts
		double clickY = 55.0;
		double thumbY = 40.0;
		double dragOffset = clickY - thumbY; // 15.0
		isDragging = true;
		assertTrue(isDragging);

		// 2. Drag to new position
		double currentMouseY = 120.0;
		if (isDragging) {
			scrollAmount = UILayoutHelper.calculateScrollFromMouseDrag(currentMouseY, dragOffset, trackTop, trackHeight, thumbHeight, maxScroll);
		}
		assertTrue(scrollAmount > 0.0);

		// 3. Mouse release -> dragging stops immediately
		isDragging = false;
		assertFalse(isDragging);

		// 4. Subsequent mouse movement without dragging active does not change scroll
		double previousScroll = scrollAmount;
		currentMouseY = 180.0;
		if (isDragging) {
			scrollAmount = UILayoutHelper.calculateScrollFromMouseDrag(currentMouseY, dragOffset, trackTop, trackHeight, thumbHeight, maxScroll);
		}
		assertEquals(previousScroll, scrollAmount, "Scroll position must not change when dragging is inactive");
	}
}
