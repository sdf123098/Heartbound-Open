package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.freecam.FreeCamera;
import com.cuddly.heartbound.freecam.Freecam;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LocalPlayer.class})
public class LocalPlayerMixin {
   @Unique
   private Vec3 freecam$frozenPos = null;

   @Inject(
      method = {"isControlledCamera()Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onIsCamera(CallbackInfoReturnable<Boolean> cir) {
      if (Freecam.isEnabled() && this.equals(Freecam.MC.player)) {
         cir.setReturnValue(true);
      }
   }

   @Inject(
      method = {"sendPosition()V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void freecam$cancelFreeCameraPackets(CallbackInfo ci) {
      if ((Object)this instanceof FreeCamera) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"tick()V"},
      at = {@At("TAIL")}
   )
   private void freecam$enforceFreeze(CallbackInfo ci) {
      if (this.freecam$shouldFreeze()) {
         Entity self = (Entity)(Object)this;
         if (this.freecam$frozenPos == null) {
            this.freecam$frozenPos = self.position();
         }

         self.setPos(this.freecam$frozenPos.x, this.freecam$frozenPos.y, this.freecam$frozenPos.z);
         self.setDeltaMovement(Vec3.ZERO);
      } else {
         this.freecam$frozenPos = null;
      }
   }

   @Unique
   private boolean freecam$shouldFreeze() {
      return Freecam.isEnabled() && this.equals(Freecam.MC.player) && ModConfig.INSTANCE.utility.freezePlayer && !Freecam.isPlayerControlEnabled();
   }
}
