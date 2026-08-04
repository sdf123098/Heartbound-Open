package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.GirlEntity;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal.Control;

public class GirlSitGoal extends Goal {
   private final GirlEntity tameable;

   public GirlSitGoal(GirlEntity tameable) {
      this.tameable = tameable;
      this.setControls(EnumSet.of(Control.JUMP, Control.MOVE));
   }

   @Override
   public boolean shouldContinue() {
      return this.tameable.isSitting();
   }

   @Override
   public boolean canStart() {
      if (this.tameable.isTouchingWater()) {
         return false;
      } else {
         return !this.tameable.isOnGround() ? false : this.tameable.isSitting();
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
