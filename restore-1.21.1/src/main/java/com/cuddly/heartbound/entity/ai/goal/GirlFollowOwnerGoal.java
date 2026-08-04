package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal.Control;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import org.jetbrains.annotations.Nullable;

public class GirlFollowOwnerGoal extends Goal {
   private static final int REPATH_INTERVAL = 10;
   private final TameableGirlEntity girl;
   private final EntityNavigation nav;
   private final double followSpeed;
   private final float startFollowDist;
   private final float stopFollowDist;
   @Nullable
   private LivingEntity target;
   private int repathTimer;
   private float savedWaterPenalty;

   public GirlFollowOwnerGoal(TameableGirlEntity girl, double speed, float minDist, float maxDist) {
      this.girl = girl;
      this.followSpeed = speed;
      this.nav = girl.getNavigation();
      this.startFollowDist = minDist;
      this.stopFollowDist = maxDist;
      this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
      if (!(this.nav instanceof MobNavigation) && !(this.nav instanceof BirdNavigation)) {
         throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
      }
   }

   @Override
   public boolean canStart() {
      LivingEntity owner = this.girl.getOwner();
      if (owner != null && !this.girl.cannotFollowOwner()) {
         if (this.girl.squaredDistanceTo(owner) < (double)(this.startFollowDist * this.startFollowDist)) {
            return false;
         } else {
            this.target = owner;
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean shouldContinue() {
      return !this.nav.isIdle()
         && !this.girl.cannotFollowOwner()
         && this.girl.squaredDistanceTo(this.target) > (double)(this.stopFollowDist * this.stopFollowDist);
   }

   @Override
   public void start() {
      this.repathTimer = 0;
      this.savedWaterPenalty = this.girl.getPathfindingPenalty(PathNodeType.WATER);
      this.girl.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
   }

   @Override
   public void stop() {
      this.girl.setSprinting(false);
      this.target = null;
      this.nav.stop();
      this.girl.setPathfindingPenalty(PathNodeType.WATER, this.savedWaterPenalty);
   }

   @Override
   public void tick() {
      boolean shouldTeleport = this.girl.shouldTryTeleportToOwner();
      if (!shouldTeleport) {
         this.girl.getLookControl().lookAt(this.target, 10.0F, (float)this.girl.getMaxLookPitchChange());
      }

      if (--this.repathTimer <= 0) {
         this.repathTimer = this.getTickCount(10);
         if (shouldTeleport) {
            this.girl.setSprinting(false);
            this.girl.tryTeleportToOwner();
         } else {
            this.girl.setSprinting(true);
            this.nav.startMovingTo(this.target, this.followSpeed);
         }
      }
   }
}
