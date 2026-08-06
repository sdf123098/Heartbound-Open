package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

public class GirlFollowOwnerGoal extends Goal {
   private static final int REPATH_INTERVAL = 10;
   private final TameableGirlEntity girl;
   private final PathNavigation nav;
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
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      if (!(this.nav instanceof GroundPathNavigation) && !(this.nav instanceof FlyingPathNavigation)) {
         throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
      }
   }

   @Override
   public boolean canUse() {
      LivingEntity owner = this.girl.getOwner();
      if (owner != null && !this.girl.cannotFollowOwner()) {
         if (this.girl.distanceToSqr(owner) < (double)(this.startFollowDist * this.startFollowDist)) {
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
   public boolean canContinueToUse() {
      return !this.nav.isDone()
         && !this.girl.cannotFollowOwner()
         && this.girl.distanceToSqr(this.target) > (double)(this.stopFollowDist * this.stopFollowDist);
   }

   @Override
   public void start() {
      this.repathTimer = 0;
      this.savedWaterPenalty = this.girl.getPathfindingMalus(PathType.WATER);
      this.girl.setPathfindingMalus(PathType.WATER, 0.0F);
   }

   @Override
   public void stop() {
      this.girl.setSprinting(false);
      this.target = null;
      this.nav.stop();
      this.girl.setPathfindingMalus(PathType.WATER, this.savedWaterPenalty);
   }

   @Override
   public void tick() {
      boolean shouldTeleport = this.girl.shouldTryTeleportToOwner();
      if (!shouldTeleport) {
         this.girl.getLookControl().setLookAt(this.target, 10.0F, (float)this.girl.getMaxHeadXRot());
      }

      if (--this.repathTimer <= 0) {
         this.repathTimer = this.adjustedTickDelay(10);
         if (shouldTeleport) {
            this.girl.setSprinting(false);
            this.girl.tryTeleportToOwner();
         } else {
            this.girl.setSprinting(true);
            this.nav.moveTo(this.target, this.followSpeed);
         }
      }
   }
}
