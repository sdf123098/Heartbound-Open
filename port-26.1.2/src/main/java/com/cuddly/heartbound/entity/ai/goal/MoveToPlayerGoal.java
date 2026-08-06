package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class MoveToPlayerGoal extends Goal {
   private final GirlSceneEntity girl;
   private boolean started = false;
   private final double speed;

   public MoveToPlayerGoal(GirlSceneEntity girl, double speed) {
      this.girl = girl;
      this.speed = speed;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
   }

   @Override
   public boolean canUse() {
      return this.girl.shouldMoveToPlayer();
   }

   @Override
   public void tick() {
      this.handleMovement();
      this.startOnContact();
   }

   @Override
   public boolean canContinueToUse() {
      return !this.started;
   }

   @Override
   public void stop() {
      this.started = false;
   }

   private void handleMovement() {
      if (!this.started && this.girl.getScenePlayer() != null) {
         this.girl.getNavigation().moveTo(this.girl.getScenePlayer(), this.speed);
      }
   }

   private void startOnContact() {
      if (this.girl.getScenePlayer() != null) {
         if (this.girl.distanceToSqr(this.girl.getScenePlayer()) <= 2.5) {
            this.girl.setDeltaMovement(Vec3.ZERO);
            this.girl.getNavigation().stop();
            this.girl.startRidingScene(this.girl.getScenePlayer());
            this.started = true;
         }
      }
   }
}
