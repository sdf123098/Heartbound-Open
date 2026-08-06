package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.freecam.Freecam;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Gui.class})
public class GuiMixin {
   @Inject(
      method = {"getCameraPlayer()Lnet/minecraft/world/entity/player/Player;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetCameraPlayer(CallbackInfoReturnable<Player> cir) {
      if (Freecam.isEnabled()) {
         cir.setReturnValue(Freecam.MC.player);
      }
   }

   @Inject(
      method = {"extractTextureOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;F)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderTextureOverlay(GuiGraphicsExtractor guiGraphicsExtractor, Identifier shaderLocation, float alpha, CallbackInfo ci) {
      if (Freecam.isEnabled()) {
         ci.cancel();
      }
   }
}
