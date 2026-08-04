package com.cuddly.heartbound.entity.ai.pathing;

import net.minecraft.block.FenceGateBlock;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;

public class GirlPathNodeMaker extends LandPathNodeMaker {
   @Override
   public PathNodeType getDefaultNodeType(PathContext context, int x, int y, int z) {
      PathNodeType type = super.getDefaultNodeType(context, x, y, z);
      return type == PathNodeType.FENCE && context.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof FenceGateBlock
         ? PathNodeType.DOOR_WOOD_CLOSED
         : type;
   }
}
