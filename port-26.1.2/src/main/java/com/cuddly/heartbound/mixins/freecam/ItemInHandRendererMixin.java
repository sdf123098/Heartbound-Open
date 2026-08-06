package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.freecam.Freecam;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemInHandRenderer.class})
public class ItemInHandRendererMixin {
   private static final String RENDER_HANDS_DESC = "renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V";
   @Unique
   private float freecam$tickDelta;

   @ModifyVariable(
      method = RENDER_HANDS_DESC,
      at = @At("HEAD"),
      argsOnly = true
   )
   private LocalPlayer onRenderItem(LocalPlayer player) {
      return (LocalPlayer)(Freecam.isEnabled() ? Freecam.getFreeCamera() : player);
   }

   @Inject(
      method = RENDER_HANDS_DESC,
      at = {@At("HEAD")}
   )
   private void storeTickDelta(float tickDelta, PoseStack matrices, SubmitNodeCollector vertexConsumers, LocalPlayer player, int light, CallbackInfo ci) {
      this.freecam$tickDelta = tickDelta;
   }

   @ModifyVariable(
      method = RENDER_HANDS_DESC,
      at = @At("HEAD"),
      argsOnly = true
   )
   private int onRenderItem2(int light) {
      return Freecam.isEnabled() ? Freecam.MC.getEntityRenderDispatcher().getPackedLightCoords(Freecam.getFreeCamera(), this.freecam$tickDelta) : light;
   }
}
