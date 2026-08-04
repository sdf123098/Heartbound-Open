package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.freecam.Freecam;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayNetworkHandler.class})
public class ClientPacketListenerMixin {
   @Inject(
      method = {"onPlayerRespawn(Lnet/minecraft/network/packet/s2c/play/PlayerRespawnS2CPacket;)V"},
      at = {@At("HEAD")}
   )
   private void onPlayerRespawn(CallbackInfo ci) {
      if (Freecam.isEnabled()) {
         Freecam.toggle();
      }
   }
}
