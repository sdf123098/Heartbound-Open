package com.cuddly.heartbound.mixins.transformation;

import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public abstract class PlayerRidingMixin {
   @Inject(
      method = {"canAddPassenger(Lnet/minecraft/world/entity/Entity;)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void heartbound$canAddPassenger(Entity passenger, CallbackInfoReturnable<Boolean> cir) {
      if ((Object)this instanceof Player self) {
         TransformablePlayer tp = (TransformablePlayer)self;
         if (tp.heartbound$isTransformSceneActive() || tp.heartbound$isWaitingForTarget()) {
            cir.setReturnValue(!self.isVehicle());
         }
      }
   }

   @Inject(
      method = {"getPassengerRidingPosition(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/Vec3;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void heartbound$getPassengerRidingPos(Entity passenger, CallbackInfoReturnable<Vec3> cir) {
      if ((Object)this instanceof Player self) {
         TransformablePlayer tp = (TransformablePlayer)self;
         if (tp.heartbound$isTransformSceneActive()) {
            Vec3 bonePos = tp.heartbound$getPassengerBonePosition();
            if (bonePos != null && !bonePos.closerThan(Vec3.ZERO, 0.1)) {
               cir.setReturnValue(self.position().add(bonePos));
            } else {
               cir.setReturnValue(self.position().add(0.0, 1.0, 0.0));
            }
         }
      }
   }

   @Inject(
      method = {"turn(DD)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void heartbound$lockLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
   }
}
