package com.cuddly.heartbound.entity.ai.pathing;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;

public class GirlNavigation extends GroundPathNavigation {
   public GirlNavigation(Mob entity, Level world) {
      super(entity, world);
   }

   @Override
   protected PathFinder createPathFinder(int range) {
      this.nodeEvaluator = new GirlPathNodeMaker();
      this.nodeEvaluator.setCanPassDoors(true);
      this.nodeEvaluator.setCanOpenDoors(true);
      return new PathFinder(this.nodeEvaluator, range);
   }
}
