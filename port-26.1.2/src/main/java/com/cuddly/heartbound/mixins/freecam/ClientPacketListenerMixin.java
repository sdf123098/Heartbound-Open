package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.freecam.Freecam;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class ClientPacketListenerMixin {
   @Inject(
      method = {"handleRespawn(Lnet/minecraft/network/protocol/game/ClientboundRespawnPacket;)V"},
      at = {@At("HEAD")}
   )
   private void onPlayerRespawn(CallbackInfo ci) {
      if (Freecam.isEnabled()) {
         Freecam.toggle();
      }
   }
}
