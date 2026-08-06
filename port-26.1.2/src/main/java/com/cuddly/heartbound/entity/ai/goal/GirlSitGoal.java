package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.GirlEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;

public class GirlSitGoal extends Goal {
   private final GirlEntity tameable;

   public GirlSitGoal(GirlEntity tameable) {
      this.tameable = tameable;
      this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
   }

   @Override
   public boolean canContinueToUse() {
      return this.tameable.isSitting();
   }

   @Override
   public boolean canUse() {
      if (this.tameable.isInWater()) {
         return false;
      } else {
         return !this.tameable.onGround() ? false : this.tameable.isSitting();
      }
   }

   @Override
   public void start() {
      this.tameable.getNavigation().stop();
      this.tameable.setSitting(true);
   }

   @Override
   public void stop() {
      this.tameable.setSitting(false);
   }
}
