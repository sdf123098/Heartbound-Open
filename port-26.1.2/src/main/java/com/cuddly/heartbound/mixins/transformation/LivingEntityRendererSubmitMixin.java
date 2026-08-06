package com.cuddly.heartbound.mixins.transformation;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.client.rendering.TransformedPlayerRenderManager;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.1.2: submit(EntityRenderState, ...) 桥接方法声明在 LivingEntityRenderer（@Inject 不匹配继承方法，
 * 原 PlayerEntityRendererMixin 目标 AvatarRenderer 找不到）。处理器内已用 AvatarRenderState 守卫，
 * 对非玩家 LivingEntityRenderer 子类无副作用。
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererSubmitMixin {
   @Inject(
      method = {"submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void heartbound$onSubmit(
      EntityRenderState state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState cameraRenderState, CallbackInfo ci
   ) {
      if (!(state instanceof AvatarRenderState avatarState)) {
         return;
      }

      Minecraft client = Minecraft.getInstance();
      Entity entity = client.level != null ? client.level.getEntity(avatarState.id) : null;
      if (!(entity instanceof AbstractClientPlayer player)) {
         return;
      }

      if (player.getVehicle() instanceof Player ridden) {
         TransformablePlayer riddenTp = (TransformablePlayer)ridden;
         if (riddenTp.heartbound$isTransformSceneActive()) {
            Heartbound.LOGGER.info("[HB-DBG] playerRender {} cancelled: riding scene-active player {}", player.getScoreboardName(), ridden.getScoreboardName());
            ci.cancel();
            return;
         }
      }

      if (player.getVehicle() instanceof GirlSceneEntity girl && girl.isSceneActive()) {
         Heartbound.LOGGER.info("[HB-DBG] playerRender {} cancelled: riding scene-active girl {} id={}", player.getScoreboardName(), girl.getGirlID(), girl.getId());
         ci.cancel();
         return;
      }

      if (player instanceof TransformablePlayer tp && tp.heartbound$isTransformed()) {
         Heartbound.LOGGER.info("[HB-DBG] playerRender {} replaced as girl '{}'", player.getScoreboardName(), tp.heartbound$getTransformGirlId());
         ci.cancel();
         String girlId = tp.heartbound$getTransformGirlId();
         TransformedPlayerRenderManager.render(player, girlId, matrices, collector, cameraRenderState);
      }
   }
}
