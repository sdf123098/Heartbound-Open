package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.client.rendering.renderers.AbstractGirlRenderer;
import com.cuddly.heartbound.client.rendering.renderers.TransformedPlayerRenderer;
import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.freecam.FreeCamera;
import com.cuddly.heartbound.freecam.Freecam;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
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
   private Entity entity;
   @Shadow
   private float eyeHeightOld;
   @Shadow
   private float eyeHeight;
   @Shadow
   private Vec3 position;
   @Shadow
   private MutableBlockPos blockPosition;
   @Unique
   private boolean heartbound$wasTransformed = false;

   @Inject(
      method = {"setEntity(Lnet/minecraft/world/entity/Entity;)V"},
      at = {@At("HEAD")}
   )
   public void onUpdate(Entity newFocusedEntity, CallbackInfo ci) {
      if (newFocusedEntity != null && this.entity != null && !newFocusedEntity.equals(this.entity)) {
         if (newFocusedEntity instanceof FreeCamera || this.entity instanceof FreeCamera) {
            this.eyeHeightOld = this.eyeHeight = newFocusedEntity.getEyeHeight();
         }
      }
   }

   @Inject(
      method = {"update(Lnet/minecraft/client/DeltaTracker;)V"},
      at = {@At("TAIL")}
   )
   private void heartbound$snapCameraOnTransformChange(DeltaTracker tickCounter, CallbackInfo ci) {
      Entity focusedEntity = this.entity;
      if (focusedEntity instanceof TransformablePlayer tp) {
         boolean isTransformed = tp.heartbound$isTransformed();
         if (this.heartbound$wasTransformed != isTransformed) {
            this.heartbound$wasTransformed = isTransformed;
            this.eyeHeightOld = this.eyeHeight = focusedEntity.getEyeHeight();
         }
      }
   }

   @Unique
   private void heartbound$setCameraPos(Vec3 camPos) {
      this.position = camPos;
      this.blockPosition.set(camPos.x, camPos.y, camPos.z);
   }

   @Inject(
      method = {"update(Lnet/minecraft/client/DeltaTracker;)V"},
      at = {@At("TAIL")}
   )
   private void heartbound$overrideSceneCamera(DeltaTracker tickCounter, CallbackInfo ci) {
      Entity focusedEntity = this.entity;
      if (focusedEntity != null && Freecam.MC.options.getCameraType() == CameraType.FIRST_PERSON) {
         if (focusedEntity instanceof Player player) {
            TransformablePlayer tp = (TransformablePlayer)player;
            if (tp.heartbound$isTransformSceneActive()) {
               Vec3 girlCamPos = TransformedPlayerRenderer.getGirlCamPos();
               if (girlCamPos != null && TransformedPlayerRenderer.getTrackedSceneEntityId() == player.getId()) {
                  this.heartbound$setCameraPos(girlCamPos);
               }
            } else {
               if (player.getVehicle() instanceof Player ridden && ((TransformablePlayer)ridden).heartbound$isTransformSceneActive()) {
                  Vec3 boyCamPos = TransformedPlayerRenderer.getBoyCamPos();
                  if (boyCamPos != null) {
                     this.heartbound$setCameraPos(boyCamPos);
                  }

                  return;
               }

               if (player.getVehicle() instanceof GirlSceneEntity girl && girl.isHavingSex()) {
                  Vec3 boyCamPos = AbstractGirlRenderer.getBoyCamPos();
                  if (boyCamPos != null && AbstractGirlRenderer.getTrackedGirlEntityId() == girl.getId()) {
                     this.heartbound$setCameraPos(boyCamPos);
                  }
               }
            }
         }
      }
   }

   @Inject(
      method = {"getFluidInCamera()Lnet/minecraft/world/level/material/FogType;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onGetSubmersionType(CallbackInfoReturnable<FogType> cir) {
      if (Freecam.isEnabled() && !ModConfig.INSTANCE.visual.showSubmersion) {
         cir.setReturnValue(FogType.NONE);
      }
   }
}
