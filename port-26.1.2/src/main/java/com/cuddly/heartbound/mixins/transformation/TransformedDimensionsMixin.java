package com.cuddly.heartbound.mixins.transformation;

import com.cuddly.heartbound.transformation.GirlTransformationInfo;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.1.2: Entity.getDefaultDimensions(Pose) 改名为 getDimensions(Pose)（声明在 Entity）。
 * 变换玩家按 GirlTransformationInfo 提供缩放尺寸；未变换玩家宽度钳制 0.5
 * （原 PlayerTransformationMixin#heartbound$getBaseDimensions，经 TransformablePlayer 接口调用）。
 */
@Mixin(Entity.class)
public abstract class TransformedDimensionsMixin {
   @Inject(
      method = {"getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void heartbound$getBaseDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
      if ((Object)this instanceof TransformablePlayer tp) {
         String girlId = tp.heartbound$getTransformGirlId();
         if (girlId != null && !girlId.isEmpty()) {
            GirlTransformationInfo info = GirlTransformationInfo.REGISTRY.get(girlId);
            if (info != null) {
               cir.setReturnValue(EntityDimensions.scalable(info.width(), info.height()).withEyeHeight(info.eyeHeight()));
               return;
            }
         }

         EntityDimensions original = (EntityDimensions)cir.getReturnValue();
         if (original.width() > 0.5F) {
            cir.setReturnValue(EntityDimensions.scalable(0.5F, original.height()).withEyeHeight(original.eyeHeight()));
         }
      }
   }
}
