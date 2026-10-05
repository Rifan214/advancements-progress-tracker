package dev.rifan.advancementsprogresstracker.ui;

import java.util.function.ToIntFunction;

/**
 * Pure layout calculation and text truncation helpers for responsive UI scaling.
 */
public final class UILayoutHelper {
	private UILayoutHelper() {
	}

	public static int computeLeftPanelWidth(int screenWidth) {
		return Math.max(160, Math.min(260, (int) (screenWidth * 0.32)));
	}

	public static boolean shouldStackToolbar(int leftPanelWidth) {
		return leftPanelWidth < 220;
	}

	public static int computeHeaderHeight(boolean stackToolbar) {
		return stackToolbar ? 58 : 40;
	}

	public static int computeAvailableEmptyStateWidth(int rightWidth) {
		return Math.max(60, rightWidth - 32);
	}

	public static int computeAvailableDetailTextWidth(int rightWidth) {
		return Math.max(60, rightWidth - 44);
	}

	public static int calculateHeaderCardHeight(int titleLinesCount, int descLinesCount) {
		int titleHeight = Math.max(1, titleLinesCount) * 11;
		int descHeight = Math.max(0, descLinesCount) * 10;
		return Math.max(48, 10 + titleHeight + 4 + descHeight + 10);
	}

	public static boolean fitsSingleLineProgress(int leftWidth, int rightWidth, int availableContainerWidth) {
		return (leftWidth + 16 + rightWidth) <= (availableContainerWidth - 16);
	}

	public static int calculateProgressCardHeight(boolean fitsSingleLine) {
		return fitsSingleLine ? 44 : 56;
	}

	public static String trimStringToWidth(ToIntFunction<String> widthMeasurer, String str, int maxWidth) {
		if (str == null || str.isEmpty() || maxWidth <= 0) {
			return "";
		}
		if (widthMeasurer.applyAsInt(str) <= maxWidth) {
			return str;
		}
		String dots = "...";
		int dotsWidth = widthMeasurer.applyAsInt(dots);
		if (dotsWidth >= maxWidth) {
			return dots;
		}
		StringBuilder sb = new StringBuilder();
		for (char c : str.toCharArray()) {
			if (widthMeasurer.applyAsInt(sb.toString() + c) + dotsWidth > maxWidth) {
				break;
			}
			sb.append(c);
		}
		return sb.toString() + dots;
	}

	public static int calculateThumbHeight(int trackHeight, int contentHeight, int minThumbHeight) {
		if (contentHeight <= 0 || trackHeight <= 0) {
			return minThumbHeight;
		}
		if (contentHeight <= trackHeight) {
			return trackHeight;
		}
		return Math.max(minThumbHeight, Math.min(trackHeight, (int) ((double) trackHeight * trackHeight / (double) contentHeight)));
	}

	public static int calculateThumbY(int trackTop, int trackHeight, int thumbHeight, double scrollAmount, double maxScroll) {
		int availableTrack = trackHeight - thumbHeight;
		if (availableTrack <= 0 || maxScroll <= 0) {
			return trackTop;
		}
		double clampedScroll = Math.max(0.0, Math.min(maxScroll, scrollAmount));
		return trackTop + (int) (availableTrack * (clampedScroll / maxScroll));
	}

	public static boolean isPointInScrollbarThumb(double mouseX, double mouseY, int scrollbarX, int thumbWidth, int thumbY, int thumbHeight) {
		return mouseX >= scrollbarX - 2 && mouseX <= scrollbarX + thumbWidth + 2 && mouseY >= thumbY && mouseY <= thumbY + thumbHeight;
	}

	public static double clampThumbY(double thumbY, int trackTop, int trackHeight, int thumbHeight) {
		int availableTrack = Math.max(0, trackHeight - thumbHeight);
		return Math.max(trackTop, Math.min(trackTop + availableTrack, thumbY));
	}

	public static double calculateScrollFromThumbY(double thumbY, int trackTop, int trackHeight, int thumbHeight, double maxScroll) {
		if (maxScroll <= 0) {
			return 0.0;
		}
		int availableTrack = trackHeight - thumbHeight;
		if (availableTrack <= 0) {
			return 0.0;
		}
		double clampedY = clampThumbY(thumbY, trackTop, trackHeight, thumbHeight);
		double normalized = (clampedY - trackTop) / (double) availableTrack;
		return Math.max(0.0, Math.min(maxScroll, normalized * maxScroll));
	}

	public static double calculateScrollFromMouseDrag(double mouseY, double dragClickOffsetY, int trackTop, int trackHeight, int thumbHeight, double maxScroll) {
		double thumbY = mouseY - dragClickOffsetY;
		return calculateScrollFromThumbY(thumbY, trackTop, trackHeight, thumbHeight, maxScroll);
	}

	public static double calculateMouseWheelScroll(double currentScroll, double maxScroll, double verticalWheelAmount, double scrollStep) {
		if (maxScroll <= 0) {
			return 0.0;
		}
		return Math.max(0.0, Math.min(maxScroll, currentScroll - verticalWheelAmount * scrollStep));
	}
}
