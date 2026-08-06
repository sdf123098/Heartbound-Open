package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.freecam.Freecam;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public abstract class LivingEntityMixin {
   @Shadow
   public abstract float getHealth();

   @Inject(
      method = {"getFrictionInfluencedSpeed(F)F"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetMovementSpeed(CallbackInfoReturnable<Float> cir) {
      if (Freecam.isEnabled() && ModConfig.INSTANCE.movement.flightMode.equals(ModConfig.FlightMode.CREATIVE) && this.equals(Freecam.getFreeCamera())) {
         cir.setReturnValue((float)(ModConfig.INSTANCE.movement.horizontalSpeed / 10.0) * (float)(Freecam.getFreeCamera().isSprinting() ? 2 : 1));
      }
   }

   @Inject(
      method = {"setHealth(F)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSetHealth(float health, CallbackInfo ci) {
      if (Freecam.isEnabled() && this.equals(Freecam.MC.player) && !Freecam.MC.player.isCreative() && this.getHealth() > health) {
         ci.cancel();
      }
   }
}
