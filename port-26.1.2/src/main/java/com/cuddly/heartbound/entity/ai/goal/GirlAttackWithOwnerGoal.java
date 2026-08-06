package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class GirlAttackWithOwnerGoal extends TargetGoal {
   private final TameableGirlEntity girl;
   private final Class<?>[] exclusions;
   private LivingEntity ownerTarget;
   private int cachedAttackTime;

   public GirlAttackWithOwnerGoal(TameableGirlEntity girl, Class<?>... doNotTarget) {
      super(girl, false);
      this.girl = girl;
      this.exclusions = doNotTarget;
      this.setFlags(EnumSet.of(Flag.TARGET));
   }

   @Override
   public boolean canUse() {
      if (this.girl.isTamed() && !this.girl.isSitting()) {
         LivingEntity owner = this.girl.getOwner();
         if (owner == null) {
            return false;
         } else {
            LivingEntity attacked = owner.getLastHurtMob();
            int attackTime = owner.getLastHurtMobTimestamp();
            if (attackTime != this.cachedAttackTime && attacked != null) {
               for (Class<?> excluded : this.exclusions) {
                  if (excluded.isAssignableFrom(attacked.getClass())) {
                     return false;
                  }
               }

               this.ownerTarget = attacked;
               return this.canAttack(this.ownerTarget, TargetingConditions.DEFAULT) && this.girl.canAttackWithOwner(this.ownerTarget, owner);
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public void start() {
      this.mob.setTarget(this.ownerTarget);
      LivingEntity owner = this.girl.getOwner();
      if (owner != null) {
         this.cachedAttackTime = owner.getLastHurtMobTimestamp();
      }

      super.start();
   }
}
