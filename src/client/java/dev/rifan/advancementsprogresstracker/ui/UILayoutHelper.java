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
		return stackToolbar ? 56 : 40;
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
}
