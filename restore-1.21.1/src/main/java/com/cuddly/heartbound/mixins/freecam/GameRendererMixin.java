package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.freecam.Freecam;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({GameRenderer.class})
public class GameRendererMixin {
   @Inject(
      method = {"renderHand(Lnet/minecraft/client/render/Camera;FLorg/joml/Matrix4f;)V"},
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

   @ModifyVariable(
      method = {"updateCrosshairTarget(F)V"},
      at = @At(
         value = "INVOKE_ASSIGN",
         target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;"
      )
   )
   private Entity onUpdateTargetedEntity(Entity entity) {
      return (Entity)(!Freecam.isEnabled()
            || !Freecam.isPlayerControlEnabled() && !ModConfig.INSTANCE.utility.interactionMode.equals(ModConfig.InteractionMode.PLAYER)
         ? entity
         : Freecam.MC.player);
   }

   @Unique
   private static boolean freecam$allowInteract() {
      return ModConfig.INSTANCE.utility.allowInteract && ModConfig.INSTANCE.utility.interactionMode.equals(ModConfig.InteractionMode.PLAYER);
   }
}
