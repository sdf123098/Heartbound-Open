package com.cuddly.heartbound.entity.ai.pathing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class GirlPathNodeMaker extends WalkNodeEvaluator {
   @Override
   public PathType getPathType(PathfindingContext context, int x, int y, int z) {
      PathType type = super.getPathType(context, x, y, z);
      return type == PathType.FENCE && context.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof FenceGateBlock
         ? PathType.DOOR_WOOD_CLOSED
         : type;
   }
}
