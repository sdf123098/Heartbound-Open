package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.freecam.Freecam;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({LightmapTextureManager.class})
public class LightTextureMixin {
   @WrapOperation(
      method = {"update(F)V"},
      at = {@At(
         value = "INVOKE",
         target = "Ljava/lang/Double;floatValue()F",
         ordinal = 1
      )}
   )
   private float onSetBrightnessFactor(Double instance, Operation<Float> original) {
      return Freecam.isEnabled() && ModConfig.INSTANCE.visual.fullBright ? Float.MAX_VALUE : (Float)original.call(new Object[]{instance});
   }
}
