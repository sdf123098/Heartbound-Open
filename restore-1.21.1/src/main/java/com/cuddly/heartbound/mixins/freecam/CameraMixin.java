package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.client.rendering.renderers.AbstractGirlRenderer;
import com.cuddly.heartbound.client.rendering.renderers.TransformedPlayerRenderer;
import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.freecam.FreeCamera;
import com.cuddly.heartbound.freecam.Freecam;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Camera.class})
public class CameraMixin {
   @Shadow
   private Entity focusedEntity;
   @Shadow
   private float lastCameraY;
   @Shadow
   private float cameraY;
   @Shadow
   private Vec3d pos;
   @Shadow
   private Mutable blockPos;
   @Unique
   private boolean heartbound$wasTransformed = false;

   @Inject(
      method = {"update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V"},
      at = {@At("HEAD")}
   )
   public void onUpdate(BlockView area, Entity newFocusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
      if (newFocusedEntity != null && this.focusedEntity != null && !newFocusedEntity.equals(this.focusedEntity)) {
         if (newFocusedEntity instanceof FreeCamera || this.focusedEntity instanceof FreeCamera) {
            this.lastCameraY = this.cameraY = newFocusedEntity.getStandingEyeHeight();
         }
      }
   }

   @Inject(
      method = {"update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V"},
      at = {@At("TAIL")}
   )
   private void heartbound$snapCameraOnTransformChange(
      BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci
   ) {
      if (focusedEntity instanceof TransformablePlayer tp) {
         boolean isTransformed = tp.heartbound$isTransformed();
         if (this.heartbound$wasTransformed != isTransformed) {
            this.heartbound$wasTransformed = isTransformed;
            this.lastCameraY = this.cameraY = focusedEntity.getStandingEyeHeight();
         }
      }
   }

   @Unique
   private void heartbound$setCameraPos(Vec3d camPos) {
      this.pos = camPos;
      this.blockPos.set(camPos.x, camPos.y, camPos.z);
   }

   @Inject(
      method = {"update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V"},
      at = {@At("TAIL")}
   )
   private void heartbound$overrideSceneCamera(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
      if (focusedEntity != null && !thirdPerson) {
         if (focusedEntity instanceof PlayerEntity player) {
            TransformablePlayer tp = (TransformablePlayer)player;
            if (tp.heartbound$isTransformSceneActive()) {
               Vec3d girlCamPos = TransformedPlayerRenderer.getGirlCamPos();
               if (girlCamPos != null && TransformedPlayerRenderer.getTrackedSceneEntityId() == player.getId()) {
                  this.heartbound$setCameraPos(girlCamPos);
               }
            } else {
               if (player.getVehicle() instanceof PlayerEntity ridden && ((TransformablePlayer)ridden).heartbound$isTransformSceneActive()) {
                  Vec3d boyCamPos = TransformedPlayerRenderer.getBoyCamPos();
                  if (boyCamPos != null) {
                     this.heartbound$setCameraPos(boyCamPos);
                  }

                  return;
               }

               if (player.getVehicle() instanceof GirlSceneEntity girl && girl.isHavingSex()) {
                  Vec3d boyCamPos = AbstractGirlRenderer.getBoyCamPos();
                  if (boyCamPos != null && AbstractGirlRenderer.getTrackedGirlEntityId() == girl.getId()) {
                     this.heartbound$setCameraPos(boyCamPos);
                  }
               }
            }
         }
      }
   }

   @Inject(
      method = {"getSubmersionType()Lnet/minecraft/block/enums/CameraSubmersionType;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onGetSubmersionType(CallbackInfoReturnable<CameraSubmersionType> cir) {
      if (Freecam.isEnabled() && !ModConfig.INSTANCE.visual.showSubmersion) {
         cir.setReturnValue(CameraSubmersionType.NONE);
      }
   }
}
