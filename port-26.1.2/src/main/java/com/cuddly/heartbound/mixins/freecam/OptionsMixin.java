package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.freecam.Freecam;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Options.class})
public class OptionsMixin {
   @Inject(
      method = {"setCameraType(Lnet/minecraft/client/CameraType;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSetPerspective(CallbackInfo ci) {
      if (Freecam.isEnabled()) {
         ci.cancel();
      }
   }
}
