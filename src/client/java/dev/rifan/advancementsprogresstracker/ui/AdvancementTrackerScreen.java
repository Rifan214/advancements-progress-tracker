package dev.rifan.advancementsprogresstracker.ui;

import dev.rifan.advancementsprogresstracker.cache.AdvancementProgressCache;
import dev.rifan.advancementsprogresstracker.data.AdvancementProgressInfo;
import dev.rifan.advancementsprogresstracker.data.CriterionDetail;
import dev.rifan.advancementsprogresstracker.data.ProgressStatus;
import com.mojang.blaze3d.platform.InputConstants;
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

	@Nullable
	private Component hoveredTooltip = null;

	public AdvancementTrackerScreen(@Nullable Screen parentScreen, @Nullable ClientAdvancements clientAdvancements) {
		super(Component.translatableWithFallback("advancements_progress_tracker.title", "Advancements Progress Tracker"));
		this.parentScreen = parentScreen;
		this.clientAdvancements = clientAdvancements;
	}

	@Override
	protected void init() {
		super.init();

		this.leftPanelWidth = UILayoutHelper.computeLeftPanelWidth(this.width);
		boolean stackToolbar = UILayoutHelper.shouldStackToolbar(this.leftPanelWidth);
		this.headerHeight = UILayoutHelper.computeHeaderHeight(stackToolbar);

		// Load advancements from cache
		if (this.clientAdvancements != null) {
			this.lastCacheVersion = AdvancementProgressCache.getInstance().getVersion();
			this.allAdvancements = AdvancementProgressCache.getInstance().getAll(this.clientAdvancements);
		} else {
			this.allAdvancements = List.of();
		}

		// Search Box & Filter button
		if (stackToolbar) {
			int toolbarW = this.leftPanelWidth - 20;
			this.searchBox = new EditBox(this.font, 10, 7, toolbarW, 20, Component.translatableWithFallback("advancements_progress_tracker.search", "Search..."));
			String rawHint = Component.translatableWithFallback("advancements_progress_tracker.search.hint", "Search...").getString();
			String safeHint = trimStringToWidth(this.font, rawHint, toolbarW - 8);
			this.searchBox.setHint(Component.literal(safeHint));
			this.searchBox.setValue(this.searchQuery);
			this.searchBox.setResponder(query -> {
				this.searchQuery = query;
				this.leftScroll = 0;
				this.rightScroll = 0;
				updateFilteredList();
			});
			this.addRenderableWidget(this.searchBox);

			this.filterButton = Button.builder(this.filterMode.getLabel(), btn -> {
				cycleFilterMode();
				btn.setMessage(this.filterMode.getLabel());
			}).bounds(10, 31, toolbarW, 20).build();
			this.addRenderableWidget(this.filterButton);
		} else {
			int filterWidth = 85;
			int searchWidth = this.leftPanelWidth - filterWidth - 25;
			this.searchBox = new EditBox(this.font, 10, 10, searchWidth, 20, Component.translatableWithFallback("advancements_progress_tracker.search", "Search..."));
			String rawHint = Component.translatableWithFallback("advancements_progress_tracker.search.hint", "Search...").getString();
			String safeHint = trimStringToWidth(this.font, rawHint, searchWidth - 8);
			this.searchBox.setHint(Component.literal(safeHint));
			this.searchBox.setValue(this.searchQuery);
			this.searchBox.setResponder(query -> {
				this.searchQuery = query;
				this.leftScroll = 0;
				this.rightScroll = 0;
				updateFilteredList();
			});
			this.addRenderableWidget(this.searchBox);

			this.filterButton = Button.builder(this.filterMode.getLabel(), btn -> {
				cycleFilterMode();
				btn.setMessage(this.filterMode.getLabel());
			}).bounds(15 + searchWidth, 10, filterWidth, 20).build();
			this.addRenderableWidget(this.filterButton);
		}

		// Done/Close Button
		int doneWidth = Math.min(100, Math.max(70, this.width - leftPanelWidth - 20));
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
		this.isDraggingLeftScroll = false;
		this.isDraggingRightScroll = false;
		this.setDragging(false);
		if (this.minecraft != null) {
			this.minecraft.setScreenAndShow(this.parentScreen);
		}
	}

	private static boolean isLeftMouseButton(MouseButtonEvent event) {
		return event.button() == InputConstants.MOUSE_BUTTON_LEFT || event.button() == 0;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (mouseX < leftPanelWidth) {
			// Scroll left panel
			this.leftScroll = UILayoutHelper.calculateMouseWheelScroll(this.leftScroll, this.maxLeftScroll, verticalAmount, 24);
			return true;
		} else {
			// Scroll right panel
			this.rightScroll = UILayoutHelper.calculateMouseWheelScroll(this.rightScroll, this.maxRightScroll, verticalAmount, 24);
			return true;
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();

		if (isLeftMouseButton(event)) {
			int visibleHeight = this.height - headerHeight - footerHeight;

			// Check left scrollbar thumb
			if (maxLeftScroll > 0) {
				int totalLeftContent = filteredAdvancements.size() * 36;
				int thumbHeight = UILayoutHelper.calculateThumbHeight(visibleHeight, totalLeftContent, 24);
				int thumbY = UILayoutHelper.calculateThumbY(headerHeight, visibleHeight, thumbHeight, leftScroll, maxLeftScroll);
				int scrollbarX = leftPanelWidth - 6;
				int barWidth = 6;

				if (UILayoutHelper.isPointInScrollbarThumb(mouseX, mouseY, scrollbarX, barWidth, thumbY, thumbHeight)) {
					this.isDraggingLeftScroll = true;
					this.leftDragClickOffsetY = mouseY - thumbY;
					this.setDragging(true);
					return true;
				}
			}

			// Check right scrollbar thumb
			if (maxRightScroll > 0) {
				int thumbHeight = UILayoutHelper.calculateThumbHeight(visibleHeight, lastRightContentHeight, 24);
				int thumbY = UILayoutHelper.calculateThumbY(headerHeight, visibleHeight, thumbHeight, rightScroll, maxRightScroll);
				int scrollbarX = this.width - 8;
				int barWidth = 6;

				if (UILayoutHelper.isPointInScrollbarThumb(mouseX, mouseY, scrollbarX, barWidth, thumbY, thumbHeight)) {
					this.isDraggingRightScroll = true;
					this.rightDragClickOffsetY = mouseY - thumbY;
					this.setDragging(true);
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
			int thumbHeight = UILayoutHelper.calculateThumbHeight(visibleHeight, totalLeftContent, 24);
			this.leftScroll = UILayoutHelper.calculateScrollFromMouseDrag(event.y(), leftDragClickOffsetY, headerHeight, visibleHeight, thumbHeight, maxLeftScroll);
			return true;
		}

		if (this.isDraggingRightScroll && maxRightScroll > 0) {
			int thumbHeight = UILayoutHelper.calculateThumbHeight(visibleHeight, lastRightContentHeight, 24);
			this.rightScroll = UILayoutHelper.calculateScrollFromMouseDrag(event.y(), rightDragClickOffsetY, headerHeight, visibleHeight, thumbHeight, maxRightScroll);
			return true;
		}

		return super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (isLeftMouseButton(event)) {
			this.isDraggingLeftScroll = false;
			this.isDraggingRightScroll = false;
			this.setDragging(false);
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
		this.hoveredTooltip = null;

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

		// 8. Tooltip for truncated text (if hovered)
		if (this.hoveredTooltip != null) {
			graphics.setTooltipForNextFrame(this.font, this.hoveredTooltip, mouseX, mouseY);
		}
	}

	private void renderLeftPanel(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		int itemHeight = 36;
		int totalContentHeight = filteredAdvancements.size() * itemHeight;
		int visibleHeight = this.height - headerHeight - footerHeight;
		this.maxLeftScroll = Math.max(0, totalContentHeight - visibleHeight);
		this.leftScroll = Math.max(0, Math.min(this.leftScroll, this.maxLeftScroll));

		graphics.enableScissor(0, headerHeight, leftPanelWidth, this.height - footerHeight);

		if (filteredAdvancements.isEmpty()) {
			Component emptyListMsg = Component.translatableWithFallback("advancements_progress_tracker.none", "No advancements found.");
			List<FormattedCharSequence> lines = this.font.split(emptyListMsg, Math.max(20, leftPanelWidth - 24));
			int totalH = lines.size() * 11;
			int startEmptyY = headerHeight + Math.max(10, (visibleHeight - totalH) / 2);
			for (int i = 0; i < lines.size(); i++) {
				graphics.centeredText(this.font, lines.get(i), leftPanelWidth / 2, startEmptyY + (i * 11), 0xFF888888);
			}
		} else {
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
				int maxTitleWidth = Math.max(20, leftPanelWidth - 48);
				if (this.font.width(titleStr) > maxTitleWidth) {
					String trimmedTitle = trimStringToWidth(this.font, titleStr, maxTitleWidth);
					graphics.text(this.font, trimmedTitle, 36, itemY + 6, titleColor, true);
					if (isHovered) {
						this.hoveredTooltip = info.title();
					}
				} else {
					graphics.text(this.font, titleStr, 36, itemY + 6, titleColor, true);
				}

				// Progress text
				String progressStr = info.getProgressFormatted();
				int progressColor = info.isCompleted() ? 0xFF2ECC71 : (info.completedRequirements() > 0 ? 0xFFF39C12 : 0xFF888888);
				graphics.text(this.font, progressStr, 36, itemY + 18, progressColor, false);

				// Mini Progress Bar
				int barWidth = Math.max(16, Math.min(32, leftPanelWidth - 110));
				int barX = leftPanelWidth - barWidth - 14;
				int barY = itemY + 22;
				graphics.fill(barX, barY, barX + barWidth, barY + 3, 0xFF222222);
				int fillWidth = Math.round(barWidth * info.percent());
				if (fillWidth > 0) {
					graphics.fill(barX, barY, barX + fillWidth, barY + 3, progressColor);
				}
			}
		}

		graphics.disableScissor();

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
			int availableW = UILayoutHelper.computeAvailableEmptyStateWidth(rightWidth);
			Component prompt = Component.translatableWithFallback("advancements_progress_tracker.select_prompt", "Select an advancement from the list to view detailed progress.");
			List<FormattedCharSequence> promptLines = this.font.split(prompt, availableW);
			int lineHeight = 12;
			int totalH = promptLines.size() * lineHeight;
			int centerY = headerHeight + Math.max(0, (visibleHeight - totalH) / 2);
			int centerX = rightX + rightWidth / 2;

			for (int i = 0; i < promptLines.size(); i++) {
				graphics.centeredText(this.font, promptLines.get(i), centerX, centerY + (i * lineHeight), 0xFF888888);
			}
			return;
		}

		graphics.enableScissor(leftPanelWidth + 1, headerHeight, this.width, this.height - footerHeight);

		AdvancementProgressInfo info = selectedAdvancement;
		int currentY = headerHeight + 12 - (int) rightScroll;

		// 1. Header Card (Dynamic height, wrapped title & description, internal ID removed)
		int availableTextW = UILayoutHelper.computeAvailableDetailTextWidth(rightWidth);
		List<FormattedCharSequence> titleLines = this.font.split(info.title(), availableTextW);
		List<FormattedCharSequence> descLines = this.font.split(info.description(), availableTextW);

		int cardHeight = UILayoutHelper.calculateHeaderCardHeight(titleLines.size(), descLines.size());

		graphics.fill(rightX, currentY, rightX + rightWidth, currentY + cardHeight, 0x22FFFFFF);
		graphics.fill(rightX, currentY, rightX + 3, currentY + cardHeight, getAdvancementTitleColor(info.type()));

		// Big Icon
		ItemStack icon = getAdvancementIcon(info);
		graphics.item(icon, rightX + 10, currentY + 10);

		// Render Title lines (wrapped)
		int textY = currentY + 10;
		for (FormattedCharSequence tLine : titleLines) {
			graphics.text(this.font, tLine, rightX + 34, textY, getAdvancementTitleColor(info.type()), true);
			textY += 11;
		}

		// Render Description lines (wrapped)
		textY += 2;
		for (FormattedCharSequence dLine : descLines) {
			graphics.text(this.font, dLine, rightX + 34, textY, 0xFFAAAAAA, false);
			textY += 10;
		}

		currentY += cardHeight + 10;

		// 2. Main Progress Bar Card (Responsive single-line or stacked layout)
		int pBarY;
		int barCardHeight;

		if (info.isOrRequirement()) {
			Component reqTitle = Component.translatableWithFallback("advancements_progress_tracker.requirements", "Requirements");
			String reqProgress = info.getProgressFormatted() + " (" + info.getPercentInt() + "%)";
			Component orNote = Component.translatable(
				"advancements_progress_tracker.criteria_fulfilled",
				info.completedCriteriaCount(),
				info.totalCriteria()
			);

			int leftW = this.font.width(reqTitle) + 6 + this.font.width(reqProgress);
			int noteW = this.font.width(orNote);
			boolean fitsSingleLine = UILayoutHelper.fitsSingleLineProgress(leftW, noteW, rightWidth);
			barCardHeight = UILayoutHelper.calculateProgressCardHeight(fitsSingleLine);

			if (fitsSingleLine) {
				graphics.fill(rightX, currentY, rightX + rightWidth, currentY + barCardHeight, 0x19FFFFFF);

				graphics.text(this.font, reqTitle, rightX + 8, currentY + 6, 0xFFFFFFFF, true);
				graphics.text(this.font, reqProgress, rightX + 8 + this.font.width(reqTitle) + 6, currentY + 6, info.isCompleted() ? 0xFF2ECC71 : 0xFFFFAA00, true);
				graphics.text(this.font, orNote, rightX + rightWidth - noteW - 8, currentY + 6, 0xFFAAAAAA, false);
				pBarY = currentY + 22;
			} else {
				graphics.fill(rightX, currentY, rightX + rightWidth, currentY + barCardHeight, 0x19FFFFFF);

				graphics.text(this.font, reqTitle, rightX + 8, currentY + 6, 0xFFFFFFFF, true);
				graphics.text(this.font, reqProgress, rightX + 8 + this.font.width(reqTitle) + 6, currentY + 6, info.isCompleted() ? 0xFF2ECC71 : 0xFFFFAA00, true);
				graphics.text(this.font, orNote, rightX + 8, currentY + 18, 0xFFAAAAAA, false);
				pBarY = currentY + 34;
			}
		} else {
			Component progressTitle = Component.translatableWithFallback("advancements_progress_tracker.progress", "Progress");
			String progressSummary = info.getProgressFormatted() + " (" + info.getPercentInt() + "%)";

			int titleW = this.font.width(progressTitle);
			int summaryW = this.font.width(progressSummary);
			boolean fitsSingleLine = UILayoutHelper.fitsSingleLineProgress(titleW, summaryW, rightWidth);
			barCardHeight = UILayoutHelper.calculateProgressCardHeight(fitsSingleLine);

			if (fitsSingleLine) {
				graphics.fill(rightX, currentY, rightX + rightWidth, currentY + barCardHeight, 0x19FFFFFF);

				graphics.text(this.font, progressTitle, rightX + 8, currentY + 6, 0xFFFFFFFF, true);
				graphics.text(this.font, progressSummary, rightX + rightWidth - summaryW - 8, currentY + 6, info.isCompleted() ? 0xFF2ECC71 : 0xFFFFAA00, true);
				pBarY = currentY + 22;
			} else {
				barCardHeight = 56;
				graphics.fill(rightX, currentY, rightX + rightWidth, currentY + barCardHeight, 0x19FFFFFF);

				graphics.text(this.font, progressTitle, rightX + 8, currentY + 6, 0xFFFFFFFF, true);
				graphics.text(this.font, progressSummary, rightX + 8, currentY + 18, info.isCompleted() ? 0xFF2ECC71 : 0xFFFFAA00, true);
				pBarY = currentY + 34;
			}
		}

		// Large Progress Bar
		int pBarX = rightX + 8;
		int pBarW = Math.max(10, rightWidth - 16);
		int pBarH = 14;
		graphics.fill(pBarX, pBarY, pBarX + pBarW, pBarY + pBarH, 0xFF111115);
		int filledW = Math.round(pBarW * info.percent());
		if (filledW > 0) {
			int barColor = info.isCompleted() ? 0xFF2ECC71 : 0xFF3498DB;
			graphics.fill(pBarX, pBarY, pBarX + Math.min(filledW, pBarW), pBarY + pBarH, barColor);
		}

		currentY += barCardHeight + 12;

		// 3. Criteria Sections
		List<CriterionDetail> completed = info.completedCriteria();
		List<CriterionDetail> remaining = info.remainingCriteria();

		int rowHeight = 22;

		// Completed Section Header
		graphics.fill(rightX, currentY, rightX + rightWidth, currentY + 22, 0x222ECC71);
		graphics.fill(rightX, currentY, rightX + 3, currentY + 22, 0xFF2ECC71);
		String completedLabel = Component.translatableWithFallback("advancements_progress_tracker.completed", "Completed").getString();
		String completedHeader = "✓ " + completedLabel + " (" + completed.size() + ")";
		int maxHeaderW = Math.max(20, rightWidth - 16);
		String trimmedCompHeader = trimStringToWidth(this.font, completedHeader, maxHeaderW);
		graphics.text(this.font, trimmedCompHeader, rightX + 10, currentY + 7, 0xFF2ECC71, true);
		graphics.fill(rightX, currentY + 22, rightX + rightWidth, currentY + 23, 0x332ECC71);
		currentY += 26;

		if (completed.isEmpty()) {
			String noneStr = Component.translatableWithFallback("advancements_progress_tracker.none", "No criteria completed yet.").getString();
			String trimmedNone = trimStringToWidth(this.font, noneStr, Math.max(20, rightWidth - 32));
			graphics.text(this.font, trimmedNone, rightX + 16, currentY + 4, 0xFF888899, false);
			currentY += 20;
		} else {
			for (CriterionDetail cd : completed) {
				if (currentY >= headerHeight - rowHeight && currentY <= this.height - footerHeight) {
					boolean isHovered = mouseX >= rightX && mouseX <= rightX + rightWidth && mouseY >= currentY && mouseY <= currentY + rowHeight;
					int rowBg = isHovered ? 0x18FFFFFF : 0x08FFFFFF;
					graphics.fill(rightX + 4, currentY, rightX + rightWidth - 4, currentY + rowHeight - 2, rowBg);
					graphics.text(this.font, "✓", rightX + 8, currentY + 6, 0xFF2ECC71, false);

					int textStartX = cd.icon().isEmpty() ? rightX + 22 : rightX + 42;
					int maxTextW = Math.max(20, rightX + rightWidth - textStartX - 10);
					String dispName = cd.displayName().getString();

					if (!cd.icon().isEmpty()) {
						graphics.item(cd.icon(), rightX + 22, currentY + 2);
					}

					if (this.font.width(dispName) > maxTextW) {
						String trimmed = trimStringToWidth(this.font, dispName, maxTextW);
						graphics.text(this.font, trimmed, textStartX, currentY + 6, 0xFFE0E0E0, false);
						if (isHovered) {
							this.hoveredTooltip = cd.displayName();
						}
					} else {
						graphics.text(this.font, cd.displayName(), textStartX, currentY + 6, 0xFFE0E0E0, false);
					}
				}
				currentY += rowHeight;
			}
		}

		currentY += 12;

		// Remaining Section Header
		graphics.fill(rightX, currentY, rightX + rightWidth, currentY + 22, 0x22F39C12);
		graphics.fill(rightX, currentY, rightX + 3, currentY + 22, 0xFFF39C12);
		String remainingLabel = Component.translatableWithFallback("advancements_progress_tracker.remaining", "Remaining").getString();
		String remainingHeader = "○ " + remainingLabel + " (" + remaining.size() + ")";
		String trimmedRemHeader = trimStringToWidth(this.font, remainingHeader, maxHeaderW);
		graphics.text(this.font, trimmedRemHeader, rightX + 10, currentY + 7, 0xFFF39C12, true);
		graphics.fill(rightX, currentY + 22, rightX + rightWidth, currentY + 23, 0x33F39C12);
		currentY += 26;

		if (remaining.isEmpty()) {
			String allCompStr = Component.translatableWithFallback("advancements_progress_tracker.all_completed", "All required criteria completed!").getString();
			String trimmedAllComp = trimStringToWidth(this.font, allCompStr, Math.max(20, rightWidth - 32));
			graphics.text(this.font, trimmedAllComp, rightX + 16, currentY + 4, 0xFF2ECC71, false);
			currentY += 20;
		} else {
			for (CriterionDetail cd : remaining) {
				if (currentY >= headerHeight - rowHeight && currentY <= this.height - footerHeight) {
					boolean isHovered = mouseX >= rightX && mouseX <= rightX + rightWidth && mouseY >= currentY && mouseY <= currentY + rowHeight;
					int rowBg = isHovered ? 0x18FFFFFF : 0x08FFFFFF;
					graphics.fill(rightX + 4, currentY, rightX + rightWidth - 4, currentY + rowHeight - 2, rowBg);
					graphics.text(this.font, "○", rightX + 8, currentY + 6, 0xFF888888, false);

					int textStartX = cd.icon().isEmpty() ? rightX + 22 : rightX + 42;
					int maxTextW = Math.max(20, rightX + rightWidth - textStartX - 10);
					String dispName = cd.displayName().getString();

					if (!cd.icon().isEmpty()) {
						graphics.item(cd.icon(), rightX + 22, currentY + 2);
					}

					if (this.font.width(dispName) > maxTextW) {
						String trimmed = trimStringToWidth(this.font, dispName, maxTextW);
						graphics.text(this.font, trimmed, textStartX, currentY + 6, 0xFFAAAAAA, false);
						if (isHovered) {
							this.hoveredTooltip = cd.displayName();
						}
					} else {
						graphics.text(this.font, cd.displayName(), textStartX, currentY + 6, 0xFFAAAAAA, false);
					}
				}
				currentY += rowHeight;
			}
		}

		graphics.disableScissor();

		int totalContentH = currentY + (int) rightScroll - headerHeight;
		this.lastRightContentHeight = totalContentH;
		this.maxRightScroll = Math.max(0, totalContentH - visibleHeight + 20);
		this.rightScroll = Math.max(0, Math.min(this.rightScroll, this.maxRightScroll));

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
		int doneWidth = Math.min(100, Math.max(70, this.width - leftPanelWidth - 20));
		int maxStatsW = Math.max(30, this.width - doneWidth - 25);
		String trimmedStats = trimStringToWidth(this.font, stats, maxStatsW);
		graphics.text(this.font, trimmedStats, 12, this.height - 18, 0xFF888899, false);
	}

	private void renderScrollbar(GuiGraphicsExtractor graphics, int x, int top, int height, double scroll, double maxScroll, int contentHeight, boolean isDragging, int mouseX, int mouseY) {
		int barWidth = 6;
		graphics.fill(x, top, x + barWidth, top + height, 0x33000000);

		int thumbHeight = UILayoutHelper.calculateThumbHeight(height, contentHeight, 24);
		int thumbY = UILayoutHelper.calculateThumbY(top, height, thumbHeight, scroll, maxScroll);

		boolean isHovered = UILayoutHelper.isPointInScrollbarThumb(mouseX, mouseY, x, barWidth, thumbY, thumbHeight);
		int thumbColor = isDragging ? 0xFFFFFFFF : (isHovered ? 0xCCFFFFFF : 0x77FFFFFF);

		graphics.fill(x, thumbY, x + barWidth, thumbY + thumbHeight, thumbColor);
	}

	private ItemStack getAdvancementIcon(AdvancementProgressInfo info) {
		if (info.display() != null && info.display().icon() != null) {
			try {
				return info.display().icon().create();
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
		return UILayoutHelper.trimStringToWidth(font::width, str, maxWidth);
	}
}
