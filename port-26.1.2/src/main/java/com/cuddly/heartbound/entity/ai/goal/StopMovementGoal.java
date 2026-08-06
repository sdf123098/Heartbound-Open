package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.GirlEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;

public class StopMovementGoal extends Goal {
   private final GirlEntity entity;
   private float frozenBodyYaw;

   public StopMovementGoal(GirlEntity entity) {
      this.entity = entity;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
   }

   @Override
   public boolean canUse() {
      return this.entity.isMovementLocked();
   }

   @Override
   public boolean canContinueToUse() {
      return this.entity.isMovementLocked();
   }

   @Override
   public void start() {
      this.frozenBodyYaw = this.entity.getVisualRotationYInDegrees();
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
      double vy = this.entity.getDeltaMovement().y;
      this.entity.setDeltaMovement(0.0, Math.min(vy, 0.0), 0.0);
      this.entity.setJumping(false);
      this.entity.yBodyRot = this.frozenBodyYaw;
      MoveControl mc = this.entity.getMoveControl();
      if (mc != null) {
         mc.setWantedPosition(this.entity.getX(), this.entity.getY(), this.entity.getZ(), 0.0);
      }
   }
}
