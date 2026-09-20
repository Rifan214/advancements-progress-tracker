package dev.rifan.advancementsprogresstracker.client;

import dev.rifan.advancementsprogresstracker.ui.AdvancementTrackerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancementsProgressTrackerClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("advancements_progress_tracker/client");

	public static KeyMapping OPEN_TRACKER_KEY;

	@Override
	public void onInitializeClient() {
		LOGGER.info("[Advancements Progress Tracker] Client initializing...");

		// 1. Register Keybinding
		OPEN_TRACKER_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.advancements_progress_tracker.open",
			GLFW.GLFW_KEY_H,
			KeyMapping.Category.MISC
		));

		// 2. Client Tick Handler for Keybinding
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_TRACKER_KEY.consumeClick()) {
				if (client.player != null && client.getConnection() != null) {
					client.setScreenAndShow(new AdvancementTrackerScreen(client.gui.screen(), client.getConnection().getAdvancements()));
				}
			}
		});

		// 3. Vanilla AdvancementsScreen Integration Button
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof AdvancementsScreen) {
				int buttonWidth = 84;
				int buttonHeight = 18;
				int buttonX = screen.width - buttonWidth - 10;
				int buttonY = 8;

				Screens.getWidgets(screen).add(
					Button.builder(
						Component.translatableWithFallback("advancements_progress_tracker.button", "Tracker"),
						btn -> {
							if (client.getConnection() != null) {
								client.setScreenAndShow(new AdvancementTrackerScreen(screen, client.getConnection().getAdvancements()));
							}
						}
					).bounds(buttonX, buttonY, buttonWidth, buttonHeight).build()
				);
			}
		});

		LOGGER.info("[Advancements Progress Tracker] Client initialized successfully");
	}
}
