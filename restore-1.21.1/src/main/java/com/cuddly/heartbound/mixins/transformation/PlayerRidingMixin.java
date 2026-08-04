package com.cuddly.heartbound.mixins.transformation;

import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public abstract class PlayerRidingMixin {
   @Inject(
      method = {"canAddPassenger(Lnet/minecraft/entity/Entity;)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void heartbound$canAddPassenger(Entity passenger, CallbackInfoReturnable<Boolean> cir) {
      if (this instanceof PlayerEntity self) {
         TransformablePlayer tp = (TransformablePlayer)self;
         if (tp.heartbound$isTransformSceneActive() || tp.heartbound$isWaitingForTarget()) {
            cir.setReturnValue(!self.hasPassengers());
         }
      }
   }

   @Inject(
      method = {"getPassengerRidingPos(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void heartbound$getPassengerRidingPos(Entity passenger, CallbackInfoReturnable<Vec3d> cir) {
      if (this instanceof PlayerEntity self) {
         TransformablePlayer tp = (TransformablePlayer)self;
         if (tp.heartbound$isTransformSceneActive()) {
            Vec3d bonePos = tp.heartbound$getPassengerBonePosition();
            if (bonePos != null && !bonePos.isInRange(Vec3d.ZERO, 0.1)) {
               cir.setReturnValue(self.getPos().add(bonePos));
            } else {
               cir.setReturnValue(self.getPos().add(0.0, 1.0, 0.0));
            }
         }
      }
   }

   @Inject(
      method = {"changeLookDirection(DD)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void heartbound$lockLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
   }
}
