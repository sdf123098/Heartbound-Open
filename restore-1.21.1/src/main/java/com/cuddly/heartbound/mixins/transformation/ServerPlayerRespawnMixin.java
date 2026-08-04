package com.cuddly.heartbound.mixins.transformation;

import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ServerPlayerEntity.class})
public abstract class ServerPlayerRespawnMixin {
   @Inject(
      method = {"copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V"},
      at = {@At("TAIL")}
   )
   private void heartbound$copyTransformData(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
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
