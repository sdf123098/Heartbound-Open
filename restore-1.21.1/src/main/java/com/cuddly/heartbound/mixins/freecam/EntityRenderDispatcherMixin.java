package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.freecam.FreeCamera;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EntityRenderDispatcher.class})
public class EntityRenderDispatcherMixin {
   @Inject(
      method = {"shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onShouldRender(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
      if (entity instanceof FreeCamera) {
         cir.setReturnValue(false);
      }

      if (entity instanceof TransformablePlayer tp && tp.heartbound$isTransformed()) {
         cir.setReturnValue(true);
      }
   }
}
