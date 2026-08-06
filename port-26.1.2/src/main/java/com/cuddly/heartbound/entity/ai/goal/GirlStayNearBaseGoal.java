package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;

public class GirlStayNearBaseGoal extends Goal {
   private static final int REPATH_TICKS = 40;
   private final TameableGirlEntity girl;
   private final PathNavigation nav;
   private final double walkSpeed;
   private final float triggerDistSq;
   private final float arrivedDistSq;
   private final float leashDistSq;
   private Path activePath;
   private int repathCountdown;
   private float cachedWaterPenalty;

   public GirlStayNearBaseGoal(TameableGirlEntity girl, double speed, float minDistance, float maxDistance, float breakOffPoint) {
      this.girl = girl;
      this.walkSpeed = speed;
      this.nav = girl.getNavigation();
      this.triggerDistSq = maxDistance * maxDistance;
      this.arrivedDistSq = minDistance * minDistance;
      this.leashDistSq = breakOffPoint * breakOffPoint;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   private double distSqToBase(BlockPos base) {
      return this.girl.distanceToSqr((double)base.getX() + 0.5, (double)base.getY(), (double)base.getZ() + 0.5);
   }

   @Override
   public boolean canUse() {
      if (!this.girl.isTamed()) {
         return false;
      } else {
         BlockPos base = this.girl.getBasePos();
         if (base != null && !this.girl.isFollowing() && !this.girl.isSitting() && !this.girl.isHavingSex() && !this.girl.isPassenger()) {
            double dist = this.distSqToBase(base);
            if (!(dist > (double)this.leashDistSq) && !(dist <= (double)this.triggerDistSq)) {
               this.activePath = this.nav.createPath((double)base.getX() + 0.5, (double)base.getY(), (double)base.getZ() + 0.5, 0);
               return this.activePath != null;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean canContinueToUse() {
      BlockPos base = this.girl.getBasePos();
      return base != null && !this.girl.isFollowing() && !this.nav.isDone() ? this.distSqToBase(base) > (double)this.arrivedDistSq : false;
   }

   @Override
   public void start() {
      this.repathCountdown = 0;
      this.cachedWaterPenalty = this.girl.getPathfindingMalus(PathType.WATER);
      this.girl.setPathfindingMalus(PathType.WATER, 0.0F);
      if (this.activePath != null) {
         this.nav.moveTo(this.activePath, this.walkSpeed);
      }
   }

   @Override
   public void stop() {
      this.nav.stop();
      this.girl.setPathfindingMalus(PathType.WATER, this.cachedWaterPenalty);
      this.activePath = null;
   }

   @Override
   public void tick() {
      BlockPos base = this.girl.getBasePos();
      if (base != null) {
         this.girl
            .getLookControl()
            .setLookAt((double)base.getX() + 0.5, (double)base.getY(), (double)base.getZ() + 0.5, 10.0F, (float)this.girl.getMaxHeadXRot());
         if (--this.repathCountdown <= 0) {
            this.repathCountdown = this.adjustedTickDelay(40);
            if (this.nav.isDone() || this.activePath != null && this.activePath.isDone()) {
               this.activePath = this.nav.createPath((double)base.getX() + 0.5, (double)base.getY(), (double)base.getZ() + 0.5, 0);
               if (this.activePath != null) {
                  this.nav.moveTo(this.activePath, this.walkSpeed);
               }
            }
         }
      }
   }
}
