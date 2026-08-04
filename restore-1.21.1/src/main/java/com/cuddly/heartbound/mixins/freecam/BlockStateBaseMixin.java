package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.freecam.FreeCamera;
import com.cuddly.heartbound.freecam.Freecam;
import net.minecraft.block.Block;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AbstractBlockState.class})
public abstract class BlockStateBaseMixin {
   @Shadow
   public abstract Block getBlock();

   @Inject(
      method = {"getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetCollisionShape(BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
      if (context instanceof EntityShapeContext entityShapeContext && entityShapeContext.getEntity() instanceof FreeCamera) {
         if (!Freecam.isEnabled()) {
            return;
         }

         cir.setReturnValue(VoxelShapes.empty());
      }
   }
}
