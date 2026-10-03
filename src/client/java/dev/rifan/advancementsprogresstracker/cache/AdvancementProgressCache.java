package dev.rifan.advancementsprogresstracker.cache;

import dev.rifan.advancementsprogresstracker.data.AdvancementProgressInfo;
import dev.rifan.advancementsprogresstracker.mixin.client.ClientAdvancementsAccessor;
import dev.rifan.advancementsprogresstracker.reader.AdvancementProgressReader;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Event-driven, thread-safe cache for parsed advancement progress.
 * Prevents heavy re-scanning every frame.
 */
public class AdvancementProgressCache {
	private static final AdvancementProgressCache INSTANCE = new AdvancementProgressCache();

	private final AdvancementProgressReader reader = new AdvancementProgressReader();
	private final Map<Identifier, AdvancementProgressInfo> cacheById = new HashMap<>();
	private List<AdvancementProgressInfo> cachedList = Collections.emptyList();
	private volatile boolean dirty = true;
	private long version = 0;

	private AdvancementProgressCache() {
	}

	public static AdvancementProgressCache getInstance() {
		return INSTANCE;
	}

	/**
	 * Marks the cache as dirty so it will be recomputed upon next request.
	 */
	public synchronized void invalidate() {
		this.dirty = true;
		this.version++;
	}

	/**
	 * Clears all cached data (e.g. when leaving a world or disconnecting).
	 */
	public synchronized void clear() {
		this.cacheById.clear();
		this.cachedList = Collections.emptyList();
		this.dirty = true;
		this.version++;
	}

	/**
	 * Returns the current version counter of the cache.
	 * Increments whenever progress updates or cache is invalidated.
	 */
	public synchronized long getVersion() {
		return this.version;
	}

	/**
	 * Returns all advancements with resolved progress.
	 * Rebuilds only if cache is invalidated (dirty).
	 */
	public synchronized List<AdvancementProgressInfo> getAll(ClientAdvancements clientAdvancements) {
		if (dirty || cachedList.isEmpty()) {
			rebuildCache(clientAdvancements);
		}
		return cachedList;
	}

	/**
	 * Retrieves an advancement by ID from cache.
	 */
	@Nullable
	public synchronized AdvancementProgressInfo get(Identifier id, ClientAdvancements clientAdvancements) {
		if (dirty || !cacheById.containsKey(id)) {
			rebuildCache(clientAdvancements);
		}
		return cacheById.get(id);
	}

	private void rebuildCache(ClientAdvancements clientAdvancements) {
		if (clientAdvancements == null) {
			return;
		}

		Map<AdvancementHolder, AdvancementProgress> progressMap;
		if (clientAdvancements instanceof ClientAdvancementsAccessor accessor) {
			progressMap = accessor.getProgressMap();
		} else {
			progressMap = clientAdvancements.progress() != null ? clientAdvancements.progress() : Collections.emptyMap();
		}

		Map<Identifier, AdvancementProgressInfo> newCacheById = new HashMap<>();
		List<AdvancementProgressInfo> newList = new ArrayList<>();

		// 1. Process all advancement nodes in the advancement tree with player display info
		if (clientAdvancements.tree() != null) {
			for (AdvancementNode node : clientAdvancements.tree().nodes()) {
				AdvancementHolder holder = node.holder();
				if (holder.value().display().isPresent()) {
					AdvancementProgress progress = progressMap.get(holder);
					AdvancementProgressInfo info = reader.read(holder, progress);
					newCacheById.put(holder.id(), info);
					newList.add(info);
				}
			}
		}

		// 2. Also process any player-facing advancements in progress map that might not be in the tree
		for (Map.Entry<AdvancementHolder, AdvancementProgress> entry : progressMap.entrySet()) {
			AdvancementHolder holder = entry.getKey();
			if (!newCacheById.containsKey(holder.id()) && holder.value().display().isPresent()) {
				AdvancementProgressInfo info = reader.read(holder, entry.getValue());
				newCacheById.put(holder.id(), info);
				newList.add(info);
			}
		}

		// Sort by title alphabetically
		newList.sort((a, b) -> a.title().getString().compareToIgnoreCase(b.title().getString()));

		this.cacheById.clear();
		this.cacheById.putAll(newCacheById);
		this.cachedList = Collections.unmodifiableList(newList);
		this.dirty = false;
	}
}
