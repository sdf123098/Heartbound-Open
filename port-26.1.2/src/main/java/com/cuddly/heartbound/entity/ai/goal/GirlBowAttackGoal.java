package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.BaseGirlEntityAI;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;

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
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   private boolean hasBow() {
      return this.girl.isHolding(Items.BOW);
   }

   @Override
   public boolean canUse() {
      return this.girl.getTarget() != null && this.hasBow();
   }

   @Override
   public boolean canContinueToUse() {
      return this.girl.getTarget() != null && this.hasBow();
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void start() {
      this.girl.setAggressive(true);
      this.strafeTime = 0;
   }

   @Override
   public void stop() {
      this.girl.setAggressive(false);
      this.girl.stopUsingItem();
      this.girl.setSprinting(false);
      this.girl.setWalkingBackward(false);
      if (this.girl.getNavigation().isInProgress() && this.girl.getNavigation().getTargetPos() != null) {
         BlockPos target = this.girl.getNavigation().getTargetPos();
         this.girl
            .getLookControl()
            .setLookAt((double)target.getX() + 0.5, (double)((float)target.getY() + this.girl.getEyeHeight()), (double)target.getZ() + 0.5);
      } else {
         this.girl
            .getLookControl()
            .setLookAt(this.girl.getX() + this.girl.getLookAngle().x, this.girl.getEyeY(), this.girl.getZ() + this.girl.getLookAngle().z);
      }

      this.strafeTime = 0;
   }

   @Override
   public void tick() {
      LivingEntity target = this.girl.getTarget();
      if (target != null && target.isAlive()) {
         double distSq = this.girl.distanceToSqr(target);
         boolean canSee = this.girl.hasLineOfSight(target);
         double dist = Math.sqrt(distSq);
         double travelTime = dist / 1.6;
         double leadX = target.getX() + target.getDeltaMovement().x * travelTime;
         double leadZ = target.getZ() + target.getDeltaMovement().z * travelTime;
         double leadDx = leadX - this.girl.getX();
         double leadDz = leadZ - this.girl.getZ();
         float targetYaw = (float)(Math.atan2(leadDz, leadDx) * (180.0 / Math.PI)) - 90.0F;
         this.girl.setYRot(targetYaw);
         this.girl.setYBodyRot(targetYaw);
         this.girl.setYHeadRot(targetYaw);
         double leadY = target.getEyeY() + target.getDeltaMovement().y * travelTime;
         this.girl.getLookControl().setLookAt(leadX, leadY, leadZ, 90.0F, 90.0F);
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
         this.girl.getNavigation().moveTo(target, this.moveSpeed);
         this.girl.setWalkingBackward(false);
         this.strafeTime = 0;
      } else {
         if (distSq < this.minRangeSq) {
            this.backOff();
            this.girl.setWalkingBackward(true);
         } else if (distSq > this.maxRangeSq) {
            this.girl.getNavigation().moveTo(target, this.moveSpeed);
            this.girl.setWalkingBackward(false);
         } else {
            this.strafeInRange();
            this.girl.setWalkingBackward(this.strafingBack);
         }
      }
   }

   private void backOff() {
      this.girl.getNavigation().stop();
      this.girl.getMoveControl().strafe(-0.5F, 0.0F);
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

      this.girl.getMoveControl().strafe(this.strafingBack ? -0.5F : 0.5F, this.strafingLeft ? 0.5F : -0.5F);
   }

   private void handleShooting(LivingEntity target, double distSq, boolean canSee) {
      if (this.girl.isUsingItem()) {
         this.handleBowDraw(target, distSq, canSee);
      } else if (this.shootCooldown <= 0 && canSee) {
         this.girl.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.girl, Items.BOW));
      }
   }

   private void handleBowDraw(LivingEntity target, double distSq, boolean canSee) {
      int useTime = this.girl.getTicksUsingItem();
      if (!canSee && useTime < 5) {
         this.girl.stopUsingItem();
      } else {
         double range = Math.sqrt(distSq);
         double maxRange = Math.sqrt(this.maxRangeSq);
         double rangeFraction = Math.min(range / maxRange, 1.0);
         int requiredDraw = (int)(8.0 + 12.0 * rangeFraction);
         if (useTime >= requiredDraw) {
            if (canSee) {
               this.girl.getMoveControl().strafe(0.0F, 0.0F);
               float pull = BowItem.getPowerForTime(useTime);
               this.girl.performRangedAttack(target, pull);
            }

            this.girl.stopUsingItem();
            this.shootCooldown = (int)(10.0 + 15.0 * rangeFraction);
         }
      }
   }
}
