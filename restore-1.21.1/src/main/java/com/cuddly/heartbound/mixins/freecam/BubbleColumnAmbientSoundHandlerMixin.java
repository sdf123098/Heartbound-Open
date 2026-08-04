package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.freecam.FreeCamera;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.BubbleColumnSoundPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BubbleColumnSoundPlayer.class})
public class BubbleColumnAmbientSoundHandlerMixin {
   @Shadow
   @Final
   private ClientPlayerEntity player;

   @Inject(
      method = {"tick()V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onTick(CallbackInfo ci) {
      if (this.player instanceof FreeCamera) {
         ci.cancel();
      }
   }
}
