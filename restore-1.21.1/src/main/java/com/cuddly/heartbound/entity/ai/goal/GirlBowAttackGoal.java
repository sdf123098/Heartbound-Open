package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.BaseGirlEntityAI;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal.Control;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public class GirlBowAttackGoal extends Goal {
   private final BaseGirlEntityAI girl;
   private final double moveSpeed;
   private final double minRangeSq;
   private final double maxRangeSq;
   private int shootCooldown = 0;
   private int strafeTime = 0;
   private boolean strafingLeft = false;
   private boolean strafingBack = false;
   private static final int MIN_DRAW_TICKS = 8;
   private static final int MAX_DRAW_TICKS = 20;

   public GirlBowAttackGoal(BaseGirlEntityAI girl, double speed, float minRange, float maxRange, int cooldownTicks) {
      this.girl = girl;
      this.moveSpeed = speed;
      this.minRangeSq = (double)(minRange * minRange);
      this.maxRangeSq = (double)(maxRange * maxRange);
      this.shootCooldown = cooldownTicks;
      this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
   }

   private boolean hasBow() {
      return this.girl.isHolding(Items.BOW);
   }

   @Override
   public boolean canStart() {
      return this.girl.getTarget() != null && this.hasBow();
   }

   @Override
   public boolean shouldContinue() {
      return this.girl.getTarget() != null && this.hasBow();
   }

   @Override
   public boolean shouldRunEveryTick() {
      return true;
   }

   @Override
   public void start() {
      this.girl.setAttacking(true);
      this.strafeTime = 0;
   }

   @Override
   public void stop() {
      this.girl.setAttacking(false);
      this.girl.clearActiveItem();
      this.girl.setSprinting(false);
      this.girl.setWalkingBackward(false);
      if (this.girl.getNavigation().isFollowingPath() && this.girl.getNavigation().getTargetPos() != null) {
         BlockPos target = this.girl.getNavigation().getTargetPos();
         this.girl
            .getLookControl()
            .lookAt((double)target.getX() + 0.5, (double)((float)target.getY() + this.girl.getStandingEyeHeight()), (double)target.getZ() + 0.5);
      } else {
         this.girl
            .getLookControl()
            .lookAt(this.girl.getX() + this.girl.getRotationVector().x, this.girl.getEyeY(), this.girl.getZ() + this.girl.getRotationVector().z);
      }

      this.strafeTime = 0;
   }

   @Override
   public void tick() {
      LivingEntity target = this.girl.getTarget();
      if (target != null && target.isAlive()) {
         double distSq = this.girl.squaredDistanceTo(target);
         boolean canSee = this.girl.canSee(target);
         double dist = Math.sqrt(distSq);
         double travelTime = dist / 1.6;
         double leadX = target.getX() + target.getVelocity().x * travelTime;
         double leadZ = target.getZ() + target.getVelocity().z * travelTime;
         double leadDx = leadX - this.girl.getX();
         double leadDz = leadZ - this.girl.getZ();
         float targetYaw = (float)(Math.atan2(leadDz, leadDx) * (180.0 / Math.PI)) - 90.0F;
         this.girl.setYaw(targetYaw);
         this.girl.setBodyYaw(targetYaw);
         this.girl.setHeadYaw(targetYaw);
         double leadY = target.getEyeY() + target.getVelocity().y * travelTime;
         this.girl.getLookControl().lookAt(leadX, leadY, leadZ, 90.0F, 90.0F);
         this.girl.setSprinting(false);
         this.handleMovement(target, distSq, canSee);
         this.handleShooting(target, distSq, canSee);
         if (this.shootCooldown > 0) {
            this.shootCooldown--;
         }
      }
   }

   private void handleMovement(LivingEntity target, double distSq, boolean canSee) {
      if (!canSee) {
         this.girl.getNavigation().startMovingTo(target, this.moveSpeed);
         this.girl.setWalkingBackward(false);
         this.strafeTime = 0;
      } else {
         if (distSq < this.minRangeSq) {
            this.backOff();
            this.girl.setWalkingBackward(true);
         } else if (distSq > this.maxRangeSq) {
            this.girl.getNavigation().startMovingTo(target, this.moveSpeed);
            this.girl.setWalkingBackward(false);
         } else {
            this.strafeInRange();
            this.girl.setWalkingBackward(this.strafingBack);
         }
      }
   }

   private void backOff() {
      this.girl.getNavigation().stop();
      this.girl.getMoveControl().strafeTo(-0.5F, 0.0F);
   }

   private void strafeInRange() {
      this.girl.getNavigation().stop();
      this.strafeTime++;
      if (this.strafeTime >= 20) {
         if (this.girl.getRandom().nextFloat() < 0.3F) {
            this.strafingLeft = !this.strafingLeft;
         }

         if (this.girl.getRandom().nextFloat() < 0.3F) {
            this.strafingBack = !this.strafingBack;
         }

         this.strafeTime = 0;
      }

      this.girl.getMoveControl().strafeTo(this.strafingBack ? -0.5F : 0.5F, this.strafingLeft ? 0.5F : -0.5F);
   }

   private void handleShooting(LivingEntity target, double distSq, boolean canSee) {
      if (this.girl.isUsingItem()) {
         this.handleBowDraw(target, distSq, canSee);
      } else if (this.shootCooldown <= 0 && canSee) {
         this.girl.setCurrentHand(ProjectileUtil.getHandPossiblyHolding(this.girl, Items.BOW));
      }
   }

   private void handleBowDraw(LivingEntity target, double distSq, boolean canSee) {
      int useTime = this.girl.getItemUseTime();
      if (!canSee && useTime < 5) {
         this.girl.clearActiveItem();
      } else {
         double range = Math.sqrt(distSq);
         double maxRange = Math.sqrt(this.maxRangeSq);
         double rangeFraction = Math.min(range / maxRange, 1.0);
         int requiredDraw = (int)(8.0 + 12.0 * rangeFraction);
         if (useTime >= requiredDraw) {
            if (canSee) {
               this.girl.getMoveControl().strafeTo(0.0F, 0.0F);
               float pull = BowItem.getPullProgress(useTime);
               this.girl.shootAt(target, pull);
            }

            this.girl.clearActiveItem();
            this.shootCooldown = (int)(10.0 + 15.0 * rangeFraction);
         }
      }
   }
}
