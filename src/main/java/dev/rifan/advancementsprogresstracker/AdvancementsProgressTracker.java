package dev.rifan.advancementsprogresstracker;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancementsProgressTracker implements ModInitializer {
	public static final String MOD_ID = "advancements_progress_tracker";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[Advancements Progress Tracker] Initialized on Minecraft 26.2");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
