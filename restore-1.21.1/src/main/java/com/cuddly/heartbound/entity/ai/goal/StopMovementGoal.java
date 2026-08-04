package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.GirlEntity;
import java.util.EnumSet;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal.Control;

public class StopMovementGoal extends Goal {
   private final GirlEntity entity;
   private float frozenBodyYaw;

   public StopMovementGoal(GirlEntity entity) {
      this.entity = entity;
      this.setControls(EnumSet.of(Control.MOVE, Control.JUMP));
   }

   @Override
   public boolean canStart() {
      return this.entity.isMovementLocked();
   }

   @Override
   public boolean shouldContinue() {
      return this.entity.isMovementLocked();
   }

   @Override
   public void start() {
      this.frozenBodyYaw = this.entity.getBodyYaw();
      this.freeze();
   }

   @Override
   public void tick() {
      this.freeze();
   }

   @Override
   public void stop() {
      this.entity.getNavigation().stop();
   }

   private void freeze() {
      this.entity.getNavigation().stop();
      double vy = this.entity.getVelocity().y;
      this.entity.setVelocity(0.0, Math.min(vy, 0.0), 0.0);
      this.entity.setJumping(false);
      this.entity.bodyYaw = this.frozenBodyYaw;
      MoveControl mc = this.entity.getMoveControl();
      if (mc != null) {
         mc.moveTo(this.entity.getX(), this.entity.getY(), this.entity.getZ(), 0.0);
      }
   }
}
