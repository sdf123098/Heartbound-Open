package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.freecam.Freecam;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({GameRenderer.class})
public class GameRendererMixin {
   @Inject(
      method = {"renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderItemInHand(CallbackInfo ci) {
      if (Freecam.isEnabled() && !ModConfig.INSTANCE.visual.showHand) {
         ci.cancel();
      } else {
         if (Freecam.MC.player instanceof TransformablePlayer tp && tp.heartbound$isTransformSceneActive()) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"shouldRenderBlockOutline()Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onShouldRenderBlockOutline(CallbackInfoReturnable<Boolean> cir) {
      if (Freecam.isEnabled() && !Freecam.isPlayerControlEnabled() && !freecam$allowInteract()) {
         cir.setReturnValue(false);
      }
   }

   @Unique
   private static boolean freecam$allowInteract() {
      return ModConfig.INSTANCE.utility.allowInteract && ModConfig.INSTANCE.utility.interactionMode.equals(ModConfig.InteractionMode.PLAYER);
   }
}
