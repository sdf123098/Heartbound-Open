package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.BaseGirlEntityAI;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Items;

public class GirlAttackSwitchGoal extends Goal {
   private final BaseGirlEntityAI girl;
   private final GirlMeleeAttackGoal meleeGoal;
   private final GirlBowAttackGoal bowGoal;
   private final double switchDistanceSq;
   private final double switchBackDistanceSq;
   private static final int RETARGET_INTERVAL = 30;
   private Goal activeGoal = null;
   private int retargetCooldown = 0;

   public GirlAttackSwitchGoal(BaseGirlEntityAI girl, double speed, float switchDistance, float minBowRange, float maxBowRange) {
      this.girl = girl;
      this.meleeGoal = new GirlMeleeAttackGoal(girl, speed, false);
      this.bowGoal = new GirlBowAttackGoal(girl, speed, minBowRange, maxBowRange, 5);
      this.switchDistanceSq = (double)(switchDistance * switchDistance);
      this.switchBackDistanceSq = this.switchDistanceSq * 0.5;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   private boolean hasBow() {
      return this.girl.isHolding(Items.BOW);
   }

   @Override
   public boolean canUse() {
      LivingEntity target = this.girl.getTarget();
      return target != null && target.isAlive();
   }

   @Override
   public boolean canContinueToUse() {
      LivingEntity target = this.girl.getTarget();
      if (target != null && target.isAlive()) {
         return true;
      } else {
         LivingEntity next = this.findNearestHostile();
         if (next != null) {
            this.girl.setTarget(next);
            this.retargetCooldown = 30;
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   private void swapTo(Goal newGoal) {
      if (this.activeGoal != newGoal) {
         if (this.activeGoal != null) {
            this.activeGoal.stop();
         }

         this.activeGoal = newGoal;
         this.activeGoal.start();
      }
   }

   @Override
   public void stop() {
      if (this.activeGoal != null) {
         this.activeGoal.stop();
         this.activeGoal = null;
      }

      this.girl.setTarget(null);
   }

   @Override
   public void tick() {
      LivingEntity target = this.girl.getTarget();
      if (target == null || !target.isAlive()) {
         target = this.findNearestHostile();
         if (target == null) {
            this.stop();
            return;
         }

         this.girl.setTarget(target);
         this.retargetCooldown = 30;
      }

      double distSq = this.girl.distanceToSqr(target);
      if (--this.retargetCooldown <= 0) {
         this.retargetCooldown = 30;
         LivingEntity betterTarget = this.findBetterTarget(target, distSq);
         if (betterTarget != null) {
            this.girl.setTarget(betterTarget);
            distSq = this.girl.distanceToSqr(betterTarget);
         }
      }

      boolean canUseBow = this.hasBow();
      boolean mainHandEmpty = this.girl.getMainHandItem().isEmpty();
      double healthRatio = (double)(this.girl.getHealth() / this.girl.getMaxHealth());
      if (canUseBow) {
         if (mainHandEmpty) {
            this.swapTo(this.bowGoal);
         } else if (healthRatio <= 0.5 && distSq >= this.switchBackDistanceSq) {
            this.swapTo(this.bowGoal);
         } else {
            this.chooseByDistance(distSq);
         }
      } else {
         this.swapTo(this.meleeGoal);
      }

      if (this.activeGoal != null) {
         this.activeGoal.tick();
      }
   }

   private LivingEntity findNearestHostile() {
      LivingEntity attacker = this.girl.getLastHurtByMob();
      if (attacker != null && attacker.isAlive() && !this.girl.isOwner(attacker)) {
         return attacker;
      } else {
         LivingEntity nearest = null;
         double nearestDistSq = Double.MAX_VALUE;

         for (Monster hostile : this.girl
            .level()
            .getEntitiesOfClass(Monster.class, this.girl.getBoundingBox().inflate(16.0, 8.0, 16.0), LivingEntity::isAlive)) {
            double dSq = this.girl.distanceToSqr(hostile);
            if (dSq < nearestDistSq) {
               nearestDistSq = dSq;
               nearest = hostile;
            }
         }

         return nearest;
      }
   }

   private LivingEntity findBetterTarget(LivingEntity currentTarget, double currentDistSq) {
      if (currentDistSq < this.switchBackDistanceSq) {
         return null;
      } else {
         LivingEntity attacker = this.girl.getLastHurtByMob();
         if (attacker != null
            && attacker.isAlive()
            && attacker != currentTarget
            && !this.girl.isOwner(attacker)
            && this.girl.distanceToSqr(attacker) < currentDistSq) {
            return attacker;
         } else {
            double threshold = currentDistSq * 0.4;
            LivingEntity closest = null;
            double closestDistSq = threshold;

            for (Monster hostile : this.girl
               .level()
               .getEntitiesOfClass(
                  Monster.class, this.girl.getBoundingBox().inflate(16.0, 8.0, 16.0), e -> e.isAlive() && e != currentTarget && this.girl.hasLineOfSight(e)
               )) {
               double dSq = this.girl.distanceToSqr(hostile);
               if (dSq < closestDistSq) {
                  closestDistSq = dSq;
                  closest = hostile;
               }
            }

            return closest;
         }
      }
   }

   private void chooseByDistance(double distSq) {
      if (this.activeGoal == this.meleeGoal) {
         if (distSq > this.switchDistanceSq) {
            this.swapTo(this.bowGoal);
         }
      } else if (distSq < this.switchBackDistanceSq) {
         this.swapTo(this.meleeGoal);
      } else {
         this.swapTo(this.bowGoal);
      }
   }
}
