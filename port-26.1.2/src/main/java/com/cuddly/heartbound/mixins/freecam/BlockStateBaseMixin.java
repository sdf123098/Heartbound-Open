package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.freecam.FreeCamera;
import com.cuddly.heartbound.freecam.Freecam;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({BlockStateBase.class})
public abstract class BlockStateBaseMixin {
   @Shadow
   public abstract Block getBlock();

   @Inject(
      method = {"getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetCollisionShape(BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
      if (context instanceof EntityCollisionContext entityShapeContext && entityShapeContext.getEntity() instanceof FreeCamera) {
         if (!Freecam.isEnabled()) {
            return;
         }

         cir.setReturnValue(Shapes.empty());
      }
   }
}
