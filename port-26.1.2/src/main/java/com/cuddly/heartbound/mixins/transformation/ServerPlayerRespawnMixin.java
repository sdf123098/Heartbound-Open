package com.cuddly.heartbound.mixins.transformation;

import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ServerPlayer.class})
public abstract class ServerPlayerRespawnMixin {
   @Inject(
      method = {"restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V"},
      at = {@At("TAIL")}
   )
   private void heartbound$copyTransformData(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
      TransformablePlayer oldTp = (TransformablePlayer)oldPlayer;
      TransformablePlayer newTp = (TransformablePlayer)this;
      if (!alive) {
         newTp.heartbound$setTransformGirlId("");
         newTp.heartbound$setStripping(false);
         newTp.heartbound$setAnimLocked(false);
         newTp.heartbound$stopTransformScene();
      } else {
         String girlId = oldTp.heartbound$getTransformGirlId();
         if (girlId != null && !girlId.isEmpty()) {
            newTp.heartbound$setTransformGirlId(girlId);
            newTp.heartbound$setStripped(oldTp.heartbound$isStripped());
         }
      }
   }
}
