package com.cuddly.heartbound.entity.ai.pathing;

import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

public class GirlNavigation extends MobNavigation {
   public GirlNavigation(MobEntity entity, World world) {
      super(entity, world);
   }

   @Override
   protected PathNodeNavigator createPathNodeNavigator(int range) {
      this.nodeMaker = new GirlPathNodeMaker();
      this.nodeMaker.setCanEnterOpenDoors(true);
      this.nodeMaker.setCanOpenDoors(true);
      return new PathNodeNavigator(this.nodeMaker, range);
   }
}
