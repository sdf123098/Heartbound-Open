package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.freecam.Freecam;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LightmapRenderStateExtractor.class})
public class LightTextureMixin {
   @Inject(
      method = {"extract(Lnet/minecraft/client/renderer/state/LightmapRenderState;F)V"},
      at = {@At("TAIL")}
   )
   private void onSetBrightnessFactor(LightmapRenderState state, float partialTick, CallbackInfo ci) {
      if (Freecam.isEnabled() && ModConfig.INSTANCE.visual.fullBright) {
         state.blockFactor = 1.0F;
         state.skyFactor = 1.0F;
         state.brightness = 1.0F;
      }
   }
}
