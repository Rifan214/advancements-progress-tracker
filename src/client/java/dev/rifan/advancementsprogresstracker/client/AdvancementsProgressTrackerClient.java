package dev.rifan.advancementsprogresstracker.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancementsProgressTrackerClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("advancements_progress_tracker/client");

	@Override
	public void onInitializeClient() {
		LOGGER.info("[Advancements Progress Tracker] Client initialized");
	}
}
