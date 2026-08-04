package com.cuddly.heartbound.mixins.transformation;

import com.cuddly.heartbound.client.rendering.TransformedPlayerRenderManager;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PlayerEntityRenderer.class})
public abstract class PlayerEntityRendererMixin {
   @Inject(
      method = {"<init>(Lnet/minecraft/client/render/entity/EntityRendererFactory$Context;Z)V"},
      at = {@At("TAIL")}
   )
   private void heartbound$captureContext(Context ctx, boolean slim, CallbackInfo ci) {
      TransformedPlayerRenderManager.captureContext(ctx);
   }

   @Inject(
      method = {"render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void heartbound$onRender(
      AbstractClientPlayerEntity player, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci
   ) {
      if (player.getVehicle() instanceof PlayerEntity ridden) {
         TransformablePlayer riddenTp = (TransformablePlayer)ridden;
         if (riddenTp.heartbound$isTransformSceneActive()) {
            ci.cancel();
            return;
         }
      }

      if (player.getVehicle() instanceof GirlSceneEntity girl && girl.isSceneActive()) {
         ci.cancel();
         return;
      }

      if (player instanceof TransformablePlayer tp && tp.heartbound$isTransformed()) {
         ci.cancel();
         String girlId = tp.heartbound$getTransformGirlId();
         TransformedPlayerRenderManager.render(player, girlId, yaw, tickDelta, matrices, vertexConsumers, light);
         return;
      }
   }
}
