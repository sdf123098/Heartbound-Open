package com.cuddly.heartbound.mixins.transformation;

import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.1.2: Player.jumpFromGround() 已删除，跳跃逻辑上移 LivingEntity.jumpFromGround()。
 * 场景演出期间禁止玩家跳跃（原 PlayerTransformationMixin#heartbound$blockJump）。
 */
@Mixin(LivingEntity.class)
public abstract class PlayerJumpBlockMixin {
   @Inject(
      method = {"jumpFromGround()V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void heartbound$blockJumpDuringScene(CallbackInfo ci) {
      if ((Object)this instanceof TransformablePlayer tp && tp.heartbound$isTransformSceneActive()) {
         ci.cancel();
      }
   }
}
