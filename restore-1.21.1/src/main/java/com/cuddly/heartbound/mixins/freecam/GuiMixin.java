package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.freecam.Freecam;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({InGameHud.class})
public class GuiMixin {
   @Inject(
      method = {"getCameraPlayer()Lnet/minecraft/entity/player/PlayerEntity;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetCameraPlayer(CallbackInfoReturnable<PlayerEntity> cir) {
      if (Freecam.isEnabled()) {
         cir.setReturnValue(Freecam.MC.player);
      }
   }

   @Inject(
      method = {"renderOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/Identifier;F)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderTextureOverlay(DrawContext guiGraphics, Identifier shaderLocation, float alpha, CallbackInfo ci) {
      if (Freecam.isEnabled()) {
         ci.cancel();
      }
   }
}
