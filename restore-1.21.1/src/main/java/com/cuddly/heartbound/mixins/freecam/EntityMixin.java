package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.freecam.Freecam;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Entity.class})
public class EntityMixin {
   @Inject(
      method = {"changeLookDirection(DD)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onChangeLookDirection(double x, double y, CallbackInfo ci) {
      if (Freecam.isEnabled() && this.equals(Freecam.MC.player) && !Freecam.isPlayerControlEnabled()) {
         Freecam.getFreeCamera().changeLookDirection(x, y);
         ci.cancel();
      }
   }

   @Inject(
      method = {"pushAwayFrom(Lnet/minecraft/entity/Entity;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onPushAwayFrom(Entity entity, CallbackInfo ci) {
      if (Freecam.isEnabled() && (entity.equals(Freecam.getFreeCamera()) || this.equals(Freecam.getFreeCamera()))) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"setVelocity(DDD)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSetVelocity(CallbackInfo ci) {
      if (this.freecam$shouldFreeze()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"updateVelocity(FLnet/minecraft/util/math/Vec3d;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onUpdateVelocity(CallbackInfo ci) {
      if (this.freecam$shouldFreeze()) {
         ci.cancel();
      }
   }

   @Unique
   private boolean freecam$shouldFreeze() {
      return Freecam.isEnabled() && this.equals(Freecam.MC.player) && this.freecam$allowFreeze();
   }

   @Unique
   private boolean freecam$allowFreeze() {
      return ModConfig.INSTANCE.utility.freezePlayer && !Freecam.isPlayerControlEnabled();
   }
}
