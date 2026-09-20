package dev.rifan.advancementsprogresstracker.ui;

import dev.rifan.advancementsprogresstracker.cache.AdvancementProgressCache;
import dev.rifan.advancementsprogresstracker.data.AdvancementProgressInfo;
import dev.rifan.advancementsprogresstracker.data.CriterionDetail;
import dev.rifan.advancementsprogresstracker.data.ProgressStatus;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdvancementTrackerScreen extends Screen {
	public enum FilterMode {
		ALL("advancements_progress_tracker.filter.all", "All"),
		IN_PROGRESS("advancements_progress_tracker.filter.in_progress", "In Progress"),
		COMPLETED("advancements_progress_tracker.filter.completed", "Completed"),
		NOT_STARTED("advancements_progress_tracker.filter.not_started", "Not Started"),
		MULTI_ONLY("advancements_progress_tracker.filter.multi_only", "Multi-Criteria");

		private final String translationKey;
		private final String fallback;

		FilterMode(String translationKey, String fallback) {
			this.translationKey = translationKey;
			this.fallback = fallback;
		}

		public Component getLabel() {
			return Component.translatableWithFallback(translationKey, fallback);
		}
	}

	@Nullable
	private final Screen parentScreen;
	@Nullable
	private final ClientAdvancements clientAdvancements;

	private EditBox searchBox;
	private Button filterButton;
	private FilterMode filterMode = FilterMode.ALL;
	private String searchQuery = "";
	private long lastCacheVersion = -1;

	private List<AdvancementProgressInfo> allAdvancements = new ArrayList<>();
	private List<AdvancementProgressInfo> filteredAdvancements = new ArrayList<>();
	@Nullable
	private AdvancementProgressInfo selectedAdvancement;

	// Scroll offsets and drag state
	private double leftScroll = 0;
	private double maxLeftScroll = 0;
	private boolean isDraggingLeftScroll = false;
	private double leftDragClickOffsetY = 0;

	private double rightScroll = 0;
	private double maxRightScroll = 0;
	private boolean isDraggingRightScroll = false;
	private double rightDragClickOffsetY = 0;
	private int lastRightContentHeight = 0;

	// Dimensions
	private int leftPanelWidth;
	private int headerHeight = 40;
	private int footerHeight = 32;

	public AdvancementTrackerScreen(@Nullable Screen parentScreen, @Nullable ClientAdvancements clientAdvancements) {
		super(Component.translatableWithFallback("advancements_progress_tracker.title", "Advancements Progress Tracker"));
		this.parentScreen = parentScreen;
		this.clientAdvancements = clientAdvancements;
	}

	@Override
	protected void init() {
		super.init();

		this.leftPanelWidth = Math.max(180, Math.min(260, (int) (this.width * 0.35)));

		// Load advancements from cache
		if (this.clientAdvancements != null) {
			this.lastCacheVersion = AdvancementProgressCache.getInstance().getVersion();
			this.allAdvancements = AdvancementProgressCache.getInstance().getAll(this.clientAdvancements);
		} else {
			this.allAdvancements = List.of();
		}

		// Search Box
		int searchWidth = Math.min(180, leftPanelWidth - 20);
		this.searchBox = new EditBox(this.font, 10, 10, searchWidth, 18, Component.translatableWithFallback("advancements_progress_tracker.search", "Search..."));
		this.searchBox.setHint(Component.translatableWithFallback("advancements_progress_tracker.search.hint", "Search..."));
		this.searchBox.setValue(this.searchQuery);
		this.searchBox.setResponder(query -> {
			this.searchQuery = query;
			this.leftScroll = 0;
			this.rightScroll = 0;
			updateFilteredList();
		});
		this.addRenderableWidget(this.searchBox);

		// Filter button
		int filterX = 15 + searchWidth;
		int filterWidth = Math.min(110, Math.max(70, leftPanelWidth - searchWidth - 20));
		this.filterButton = Button.builder(this.filterMode.getLabel(), btn -> {
			cycleFilterMode();
			btn.setMessage(this.filterMode.getLabel());
		}).bounds(filterX, 10, filterWidth, 18).build();
		this.addRenderableWidget(this.filterButton);

		// Done/Close Button
		int doneWidth = 100;
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> this.onClose())
			.bounds(this.width - doneWidth - 10, this.height - 26, doneWidth, 20)
			.build());

		updateFilteredList();

		if (selectedAdvancement == null && !filteredAdvancements.isEmpty()) {
			selectedAdvancement = filteredAdvancements.get(0);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.clientAdvancements != null) {
			long currentVersion = AdvancementProgressCache.getInstance().getVersion();
			if (this.lastCacheVersion != -1 && currentVersion != this.lastCacheVersion) {
				this.lastCacheVersion = currentVersion;
				refreshData();
			}
		}
	}

	private void refreshData() {
		if (this.clientAdvancements != null) {
			Identifier selectedId = selectedAdvancement != null ? selectedAdvancement.id() : null;
			this.allAdvancements = AdvancementProgressCache.getInstance().getAll(this.clientAdvancements);
			updateFilteredList();

			if (selectedId != null) {
				for (AdvancementProgressInfo info : this.filteredAdvancements) {
					if (info.id().equals(selectedId)) {
						this.selectedAdvancement = info;
						break;
					}
				}
			}
		}
	}

	private void cycleFilterMode() {
		FilterMode[] modes = FilterMode.values();
		int nextIndex = (filterMode.ordinal() + 1) % modes.length;
		filterMode = modes[nextIndex];
		this.leftScroll = 0;
		this.rightScroll = 0;
		updateFilteredList();
	}

	private void updateFilteredList() {
		String query = searchQuery.trim().toLowerCase(Locale.ROOT);
		List<AdvancementProgressInfo> result = new ArrayList<>();

		for (AdvancementProgressInfo info : allAdvancements) {
			// Apply filter mode
			if (filterMode == FilterMode.IN_PROGRESS && info.status() != ProgressStatus.IN_PROGRESS) {
				continue;
			}
			if (filterMode == FilterMode.COMPLETED && info.status() != ProgressStatus.COMPLETED) {
				continue;
			}
			if (filterMode == FilterMode.NOT_STARTED && info.status() != ProgressStatus.NOT_STARTED) {
				continue;
			}
			if (filterMode == FilterMode.MULTI_ONLY && !info.isMultiCriteria()) {
				continue;
			}

			// Apply search query
			if (!query.isEmpty()) {
				boolean matchesTitle = info.title().getString().toLowerCase(Locale.ROOT).contains(query);
				boolean matchesDesc = info.description().getString().toLowerCase(Locale.ROOT).contains(query);
				boolean matchesId = info.id().toString().toLowerCase(Locale.ROOT).contains(query);
				boolean matchesCriteria = false;

				for (CriterionDetail cd : info.criteria()) {
					if (cd.displayName().getString().toLowerCase(Locale.ROOT).contains(query)
						|| cd.id().toLowerCase(Locale.ROOT).contains(query)) {
						matchesCriteria = true;
						break;
					}
				}

				if (!matchesTitle && !matchesDesc && !matchesId && !matchesCriteria) {
					continue;
				}
			}

			result.add(info);
		}

		this.filteredAdvancements = result;

		// Keep selected if still in list, else select first
		if (selectedAdvancement != null && !filteredAdvancements.contains(selectedAdvancement)) {
			selectedAdvancement = filteredAdvancements.isEmpty() ? null : filteredAdvancements.get(0);
		}
	}

	@Override
	public void onClose() {
		if (this.minecraft != null) {
			this.minecraft.setScreenAndShow(this.parentScreen);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (mouseX < leftPanelWidth) {
			// Scroll left panel
			this.leftScroll = Math.max(0, Math.min(maxLeftScroll, this.leftScroll - verticalAmount * 24));
			return true;
		} else {
			// Scroll right panel
			this.rightScroll = Math.max(0, Math.min(maxRightScroll, this.rightScroll - verticalAmount * 24));
			return true;
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();

		if (event.button() == 0) { // Left-click
			int visibleHeight = this.height - headerHeight - footerHeight;

			// Check left scrollbar thumb
			if (maxLeftScroll > 0) {
				int totalLeftContent = filteredAdvancements.size() * 36;
				int thumbHeight = Math.max(24, Math.min(visibleHeight, (int) ((double) visibleHeight * visibleHeight / (double) Math.max(1, totalLeftContent))));
				int availableTrackDistance = visibleHeight - thumbHeight;
				int thumbY = headerHeight + (availableTrackDistance > 0 ? (int) (availableTrackDistance * (leftScroll / maxLeftScroll)) : 0);
				int scrollbarX = leftPanelWidth - 6;

				if (mouseX >= scrollbarX - 2 && mouseX <= scrollbarX + 8 && mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
					this.isDraggingLeftScroll = true;
					this.leftDragClickOffsetY = mouseY - thumbY;
					return true;
				}
			}

			// Check right scrollbar thumb
			if (maxRightScroll > 0) {
				int thumbHeight = Math.max(24, Math.min(visibleHeight, (int) ((double) visibleHeight * visibleHeight / (double) Math.max(1, lastRightContentHeight))));
				int availableTrackDistance = visibleHeight - thumbHeight;
				int thumbY = headerHeight + (availableTrackDistance > 0 ? (int) (availableTrackDistance * (rightScroll / maxRightScroll)) : 0);
				int scrollbarX = this.width - 8;

				if (mouseX >= scrollbarX - 2 && mouseX <= scrollbarX + 8 && mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
					this.isDraggingRightScroll = true;
					this.rightDragClickOffsetY = mouseY - thumbY;
					return true;
				}
			}
		}

		if (mouseX >= 10 && mouseX <= leftPanelWidth - 8 && mouseY >= headerHeight && mouseY <= height - footerHeight) {
			// Click inside left panel list
			int listY = (int) (mouseY - headerHeight + leftScroll);
			int itemHeight = 36;
			int index = listY / itemHeight;

			if (index >= 0 && index < filteredAdvancements.size()) {
				this.selectedAdvancement = filteredAdvancements.get(index);
				this.rightScroll = 0;
				return true;
			}
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		int visibleHeight = this.height - headerHeight - footerHeight;

		if (this.isDraggingLeftScroll && maxLeftScroll > 0) {
			int totalLeftContent = filteredAdvancements.size() * 36;
			int thumbHeight = Math.max(24, Math.min(visibleHeight, (int) ((double) visibleHeight * visibleHeight / (double) Math.max(1, totalLeftContent))));
			int availableTrackDistance = visibleHeight - thumbHeight;
			if (availableTrackDistance > 0) {
				double newThumbY = event.y() - leftDragClickOffsetY;
				double offsetInTrack = newThumbY - headerHeight;
				double newScroll = (offsetInTrack / (double) availableTrackDistance) * maxLeftScroll;
				this.leftScroll = Math.max(0, Math.min(maxLeftScroll, newScroll));
			}
			return true;
		}

		if (this.isDraggingRightScroll && maxRightScroll > 0) {
			int thumbHeight = Math.max(24, Math.min(visibleHeight, (int) ((double) visibleHeight * visibleHeight / (double) Math.max(1, lastRightContentHeight))));
			int availableTrackDistance = visibleHeight - thumbHeight;
			if (availableTrackDistance > 0) {
				double newThumbY = event.y() - rightDragClickOffsetY;
				double offsetInTrack = newThumbY - headerHeight;
				double newScroll = (offsetInTrack / (double) availableTrackDistance) * maxRightScroll;
				this.rightScroll = Math.max(0, Math.min(maxRightScroll, newScroll));
			}
			return true;
		}

		return super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event.button() == 0) {
			this.isDraggingLeftScroll = false;
			this.isDraggingRightScroll = false;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == 256 && this.shouldCloseOnEsc()) { // GLFW_KEY_ESCAPE
			this.onClose();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		// 1. Render Screen Background
		graphics.fill(0, 0, this.width, this.height, 0xEE121214);

		// 2. Render Left Panel Background
		graphics.fill(0, 0, leftPanelWidth, this.height, 0x991A1A1E);
		graphics.fill(leftPanelWidth, 0, leftPanelWidth + 1, this.height, 0x44FFFFFF);

		// 3. Render Header & Footer Backgrounds
		graphics.fill(0, 0, this.width, headerHeight, 0xCC18181C);
		graphics.fill(0, headerHeight - 1, this.width, headerHeight, 0x33FFFFFF);
		graphics.fill(0, this.height - footerHeight, this.width, this.height, 0xCC18181C);
		graphics.fill(0, this.height - footerHeight, this.width, this.height - footerHeight + 1, 0x33FFFFFF);

		// 4. Render Left Panel: Advancement List
		renderLeftPanel(graphics, mouseX, mouseY);

		// 5. Render Right Panel: Detail View
		renderRightPanel(graphics, mouseX, mouseY);

		// 6. Render Footer Stats
		renderFooterStats(graphics);

		// 7. Render Widgets (Search bar, Buttons)
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	private void renderLeftPanel(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		int itemHeight = 36;
		int totalContentHeight = filteredAdvancements.size() * itemHeight;
		int visibleHeight = this.height - headerHeight - footerHeight;
		this.maxLeftScroll = Math.max(0, totalContentHeight - visibleHeight);

		int startY = headerHeight - (int) leftScroll;

		for (int i = 0; i < filteredAdvancements.size(); i++) {
			AdvancementProgressInfo info = filteredAdvancements.get(i);
			int itemY = startY + (i * itemHeight);

			if (itemY + itemHeight < headerHeight || itemY > this.height - footerHeight) {
				continue; // Clipped out
			}

			boolean isSelected = info == selectedAdvancement;
			boolean isHovered = mouseX >= 8 && mouseX <= leftPanelWidth - 8 && mouseY >= Math.max(headerHeight, itemY) && mouseY <= Math.min(this.height - footerHeight, itemY + itemHeight);

			// Item background
			int bgColor = isSelected ? 0x443B82F6 : (isHovered ? 0x22FFFFFF : 0x11FFFFFF);
			graphics.fill(8, itemY + 2, leftPanelWidth - 8, itemY + itemHeight - 2, bgColor);
			if (isSelected) {
				graphics.fill(8, itemY + 2, 10, itemY + itemHeight - 2, 0xFF3B82F6);
			}

			// Icon
			ItemStack icon = getAdvancementIcon(info);
			graphics.item(icon, 14, itemY + 8);

			// Title
			int titleColor = getAdvancementTitleColor(info.type());
			String titleStr = info.title().getString();
			int maxTitleWidth = leftPanelWidth - 75;
			String trimmedTitle = trimStringToWidth(this.font, titleStr, maxTitleWidth);
			graphics.text(this.font, trimmedTitle, 36, itemY + 6, titleColor, true);

			// Progress text
			String progressStr = info.getProgressFormatted();
			int progressColor = info.isCompleted() ? 0xFF2ECC71 : (info.completedRequirements() > 0 ? 0xFFF39C12 : 0xFF888888);
			graphics.text(this.font, progressStr, 36, itemY + 18, progressColor, false);

			// Mini Progress Bar
			int barX = leftPanelWidth - 45;
			int barWidth = 32;
			int barY = itemY + 22;
			graphics.fill(barX, barY, barX + barWidth, barY + 3, 0xFF222222);
			int fillWidth = Math.round(barWidth * info.percent());
			if (fillWidth > 0) {
				graphics.fill(barX, barY, barX + fillWidth, barY + 3, progressColor);
			}
		}

		// Scrollbar
		if (maxLeftScroll > 0) {
			renderScrollbar(graphics, leftPanelWidth - 6, headerHeight, visibleHeight, leftScroll, maxLeftScroll, totalContentHeight, isDraggingLeftScroll, mouseX, mouseY);
		}
	}

	private void renderRightPanel(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		int rightX = leftPanelWidth + 16;
		int rightWidth = this.width - rightX - 16;
		int visibleHeight = this.height - headerHeight - footerHeight;

		if (selectedAdvancement == null) {
			graphics.centeredText(this.font, Component.translatableWithFallback("advancements_progress_tracker.select_prompt", "Select an advancement from the list to view detailed progress."), leftPanelWidth + (this.width - leftPanelWidth) / 2, this.height / 2, 0xFF888888);
			return;
		}

		AdvancementProgressInfo info = selectedAdvancement;
		int currentY = headerHeight + 12 - (int) rightScroll;

		// 1. Header Card Background
		int cardHeight = 68;
		graphics.fill(rightX, currentY, rightX + rightWidth, currentY + cardHeight, 0x22FFFFFF);
		graphics.fill(rightX, currentY, rightX + 3, currentY + cardHeight, getAdvancementTitleColor(info.type()));

		// Big Icon
		ItemStack icon = getAdvancementIcon(info);
		graphics.item(icon, rightX + 10, currentY + 10);

		// Title & ID
		graphics.text(this.font, info.title(), rightX + 32, currentY + 10, getAdvancementTitleColor(info.type()), true);
		graphics.text(this.font, info.id().toString(), rightX + 32, currentY + 22, 0xFF666677, false);

		// Description
		List<FormattedCharSequence> descLines = this.font.split(info.description(), rightWidth - 42);
		int descY = currentY + 34;
		for (int i = 0; i < Math.min(2, descLines.size()); i++) {
			graphics.text(this.font, descLines.get(i), rightX + 32, descY + (i * 10), 0xFFAAAAAA, false);
		}

		currentY += cardHeight + 10;

		// 2. Main Progress Bar Card
		int barCardHeight = 46;
		graphics.fill(rightX, currentY, rightX + rightWidth, currentY + barCardHeight, 0x19FFFFFF);

		if (info.isOrRequirement()) {
			Component reqTitle = Component.translatableWithFallback("advancements_progress_tracker.requirements", "Requirements");
			String reqProgress = info.getProgressFormatted() + " (" + info.getPercentInt() + "%)";
			graphics.text(this.font, reqTitle, rightX + 8, currentY + 6, 0xFFFFFFFF, true);
			graphics.text(this.font, reqProgress, rightX + 8 + this.font.width(reqTitle) + 6, currentY + 6, info.isCompleted() ? 0xFF2ECC71 : 0xFFFFAA00, true);

			Component orNote = Component.translatable(
				"advancements_progress_tracker.criteria_fulfilled",
				info.completedCriteriaCount(),
				info.totalCriteria()
			);
			int noteW = this.font.width(orNote);
			graphics.text(this.font, orNote, rightX + rightWidth - noteW - 8, currentY + 6, 0xFFAAAAAA, false);
		} else {
			Component progressTitle = Component.translatableWithFallback("advancements_progress_tracker.progress", "Progress");
			String progressSummary = info.getProgressFormatted() + " (" + info.getPercentInt() + "%)";
			graphics.text(this.font, progressTitle, rightX + 8, currentY + 6, 0xFFFFFFFF, true);
			int summaryWidth = this.font.width(progressSummary);
			graphics.text(this.font, progressSummary, rightX + rightWidth - summaryWidth - 8, currentY + 6, info.isCompleted() ? 0xFF2ECC71 : 0xFFFFAA00, true);
		}

		// Large bar
		int pBarX = rightX + 8;
		int pBarY = currentY + 22;
		int pBarW = rightWidth - 16;
		int pBarH = 14;
		graphics.fill(pBarX, pBarY, pBarX + pBarW, pBarY + pBarH, 0xFF111115);
		int filledW = Math.round(pBarW * info.percent());
		if (filledW > 0) {
			int barColor = info.isCompleted() ? 0xFF2ECC71 : 0xFF3498DB;
			graphics.fill(pBarX, pBarY, pBarX + filledW, pBarY + pBarH, barColor);
		}

		currentY += barCardHeight + 12;

		// 3. Criteria Sections (The MAIN content!)
		List<CriterionDetail> completed = info.completedCriteria();
		List<CriterionDetail> remaining = info.remainingCriteria();

		int rowHeight = 22;

		// Completed Section Header
		graphics.fill(rightX, currentY, rightX + rightWidth, currentY + 22, 0x222ECC71);
		graphics.fill(rightX, currentY, rightX + 3, currentY + 22, 0xFF2ECC71);
		String completedHeader = "✓ " + Component.translatableWithFallback("advancements_progress_tracker.completed", "Completed").getString() + " (" + completed.size() + ")";
		graphics.text(this.font, completedHeader, rightX + 10, currentY + 7, 0xFF2ECC71, true);
		graphics.fill(rightX, currentY + 22, rightX + rightWidth, currentY + 23, 0x332ECC71);
		currentY += 26;

		if (completed.isEmpty()) {
			graphics.text(this.font, Component.translatableWithFallback("advancements_progress_tracker.none", "No criteria completed yet."), rightX + 16, currentY + 4, 0xFF888899, false);
			currentY += 20;
		} else {
			for (CriterionDetail cd : completed) {
				if (currentY >= headerHeight - rowHeight && currentY <= this.height - footerHeight) {
					boolean isHovered = mouseX >= rightX && mouseX <= rightX + rightWidth && mouseY >= currentY && mouseY <= currentY + rowHeight;
					int rowBg = isHovered ? 0x18FFFFFF : 0x08FFFFFF;
					graphics.fill(rightX + 4, currentY, rightX + rightWidth - 4, currentY + rowHeight - 2, rowBg);
					graphics.text(this.font, "✓", rightX + 8, currentY + 6, 0xFF2ECC71, false);

					if (!cd.icon().isEmpty()) {
						graphics.item(cd.icon(), rightX + 22, currentY + 2);
						graphics.text(this.font, cd.displayName(), rightX + 42, currentY + 6, 0xFFE0E0E0, false);
					} else {
						graphics.text(this.font, cd.displayName(), rightX + 22, currentY + 6, 0xFFE0E0E0, false);
					}
				}
				currentY += rowHeight;
			}
		}

		currentY += 12;

		// Remaining Section Header
		graphics.fill(rightX, currentY, rightX + rightWidth, currentY + 22, 0x22F39C12);
		graphics.fill(rightX, currentY, rightX + 3, currentY + 22, 0xFFF39C12);
		String remainingHeader = "○ " + Component.translatableWithFallback("advancements_progress_tracker.remaining", "Remaining").getString() + " (" + remaining.size() + ")";
		graphics.text(this.font, remainingHeader, rightX + 10, currentY + 7, 0xFFF39C12, true);
		graphics.fill(rightX, currentY + 22, rightX + rightWidth, currentY + 23, 0x33F39C12);
		currentY += 26;

		if (remaining.isEmpty()) {
			graphics.text(this.font, Component.translatableWithFallback("advancements_progress_tracker.all_completed", "All required criteria completed!"), rightX + 16, currentY + 4, 0xFF2ECC71, false);
			currentY += 20;
		} else {
			for (CriterionDetail cd : remaining) {
				if (currentY >= headerHeight - rowHeight && currentY <= this.height - footerHeight) {
					boolean isHovered = mouseX >= rightX && mouseX <= rightX + rightWidth && mouseY >= currentY && mouseY <= currentY + rowHeight;
					int rowBg = isHovered ? 0x18FFFFFF : 0x08FFFFFF;
					graphics.fill(rightX + 4, currentY, rightX + rightWidth - 4, currentY + rowHeight - 2, rowBg);
					graphics.text(this.font, "○", rightX + 8, currentY + 6, 0xFF888888, false);

					if (!cd.icon().isEmpty()) {
						graphics.item(cd.icon(), rightX + 22, currentY + 2);
						graphics.text(this.font, cd.displayName(), rightX + 42, currentY + 6, 0xFFAAAAAA, false);
					} else {
						graphics.text(this.font, cd.displayName(), rightX + 22, currentY + 6, 0xFFAAAAAA, false);
					}
				}
				currentY += rowHeight;
			}
		}

		int totalContentH = currentY + (int) rightScroll - headerHeight;
		this.lastRightContentHeight = totalContentH;
		this.maxRightScroll = Math.max(0, totalContentH - visibleHeight + 20);

		if (maxRightScroll > 0) {
			renderScrollbar(graphics, this.width - 8, headerHeight, visibleHeight, rightScroll, maxRightScroll, totalContentH, isDraggingRightScroll, mouseX, mouseY);
		}
	}

	private void renderFooterStats(GuiGraphicsExtractor graphics) {
		int completedCount = 0;
		for (AdvancementProgressInfo info : allAdvancements) {
			if (info.isCompleted()) {
				completedCount++;
			}
		}
		int total = allAdvancements.size();
		int percent = total > 0 ? Math.round((float) completedCount / (float) total * 100.0f) : 0;

		String stats = "Advancements: " + completedCount + " / " + total + " (" + percent + "%)";
		graphics.text(this.font, stats, 12, this.height - 18, 0xFF888899, false);
	}

	private void renderScrollbar(GuiGraphicsExtractor graphics, int x, int top, int height, double scroll, double maxScroll, int contentHeight, boolean isDragging, int mouseX, int mouseY) {
		int barWidth = 6;
		graphics.fill(x, top, x + barWidth, top + height, 0x33000000);

		int thumbHeight = Math.max(24, Math.min(height, (int) ((double) height * height / (double) Math.max(1, contentHeight))));
		int availableTrack = height - thumbHeight;
		int thumbY = top + (availableTrack > 0 && maxScroll > 0 ? (int) (availableTrack * (scroll / maxScroll)) : 0);

		boolean isHovered = mouseX >= x - 2 && mouseX <= x + barWidth + 2 && mouseY >= thumbY && mouseY <= thumbY + thumbHeight;
		int thumbColor = isDragging ? 0xFFFFFFFF : (isHovered ? 0xCCFFFFFF : 0x77FFFFFF);

		graphics.fill(x, thumbY, x + barWidth, thumbY + thumbHeight, thumbColor);
	}

	private ItemStack getAdvancementIcon(AdvancementProgressInfo info) {
		if (info.display() != null && info.display().getIcon() != null) {
			try {
				return info.display().getIcon().create();
			} catch (Throwable ignored) {
			}
		}
		return new ItemStack(Items.BOOK);
	}

	private int getAdvancementTitleColor(AdvancementType type) {
		return switch (type) {
			case CHALLENGE -> 0xFFFFAA00; // Gold
			case GOAL -> 0xFF55FF55;      // Green
			case TASK -> 0xFFFFFFFF;      // White
		};
	}

	private static String trimStringToWidth(Font font, String str, int maxWidth) {
		if (font.width(str) <= maxWidth) {
			return str;
		}
		String dots = "...";
		int dotsWidth = font.width(dots);
		StringBuilder sb = new StringBuilder();
		for (char c : str.toCharArray()) {
			if (font.width(sb.toString() + c) + dotsWidth > maxWidth) {
				break;
			}
			sb.append(c);
		}
		return sb.toString() + dots;
	}
}
