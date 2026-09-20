package dev.rifan.advancementsprogresstracker.mixin.client;

import dev.rifan.advancementsprogresstracker.cache.AdvancementProgressCache;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Automatically invalidates the advancement progress cache whenever the server sends advancement updates.
 */
@Mixin(ClientAdvancements.class)
public class ClientAdvancementsMixin {

	@Inject(method = "update", at = @At("RETURN"))
	private void onUpdateAdvancements(ClientboundUpdateAdvancementsPacket packet, CallbackInfo ci) {
		AdvancementProgressCache.getInstance().invalidate();
	}
}
