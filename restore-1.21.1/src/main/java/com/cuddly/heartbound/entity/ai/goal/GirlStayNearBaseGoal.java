package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal.Control;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;

public class GirlStayNearBaseGoal extends Goal {
   private static final int REPATH_TICKS = 40;
   private final TameableGirlEntity girl;
   private final EntityNavigation nav;
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
      this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
   }

   private double distSqToBase(BlockPos base) {
      return this.girl.squaredDistanceTo((double)base.getX() + 0.5, (double)base.getY(), (double)base.getZ() + 0.5);
   }

   @Override
   public boolean canStart() {
      if (!this.girl.isTamed()) {
         return false;
      } else {
         BlockPos base = this.girl.getBasePos();
         if (base != null && !this.girl.isFollowing() && !this.girl.isSitting() && !this.girl.isHavingSex() && !this.girl.hasVehicle()) {
            double dist = this.distSqToBase(base);
            if (!(dist > (double)this.leashDistSq) && !(dist <= (double)this.triggerDistSq)) {
               this.activePath = this.nav.findPathTo((double)base.getX() + 0.5, (double)base.getY(), (double)base.getZ() + 0.5, 0);
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
   public boolean shouldContinue() {
      BlockPos base = this.girl.getBasePos();
      return base != null && !this.girl.isFollowing() && !this.nav.isIdle() ? this.distSqToBase(base) > (double)this.arrivedDistSq : false;
   }

   @Override
   public void start() {
      this.repathCountdown = 0;
      this.cachedWaterPenalty = this.girl.getPathfindingPenalty(PathNodeType.WATER);
      this.girl.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
      if (this.activePath != null) {
         this.nav.startMovingAlong(this.activePath, this.walkSpeed);
      }
   }

   @Override
   public void stop() {
      this.nav.stop();
      this.girl.setPathfindingPenalty(PathNodeType.WATER, this.cachedWaterPenalty);
      this.activePath = null;
   }

   @Override
   public void tick() {
      BlockPos base = this.girl.getBasePos();
      if (base != null) {
         this.girl
            .getLookControl()
            .lookAt((double)base.getX() + 0.5, (double)base.getY(), (double)base.getZ() + 0.5, 10.0F, (float)this.girl.getMaxLookPitchChange());
         if (--this.repathCountdown <= 0) {
            this.repathCountdown = this.getTickCount(40);
            if (this.nav.isIdle() || this.activePath != null && this.activePath.isFinished()) {
               this.activePath = this.nav.findPathTo((double)base.getX() + 0.5, (double)base.getY(), (double)base.getZ() + 0.5, 0);
               if (this.activePath != null) {
                  this.nav.startMovingAlong(this.activePath, this.walkSpeed);
               }
            }
         }
      }
   }
}
