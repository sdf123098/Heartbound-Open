package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.ai.goal.Goal.Control;

public class GirlAttackWithOwnerGoal extends TrackTargetGoal {
   private final TameableGirlEntity girl;
   private final Class<?>[] exclusions;
   private LivingEntity ownerTarget;
   private int cachedAttackTime;

   public GirlAttackWithOwnerGoal(TameableGirlEntity girl, Class<?>... doNotTarget) {
      super(girl, false);
      this.girl = girl;
      this.exclusions = doNotTarget;
      this.setControls(EnumSet.of(Control.TARGET));
   }

   @Override
   public boolean canStart() {
      if (this.girl.isTamed() && !this.girl.isSitting()) {
         LivingEntity owner = this.girl.getOwner();
         if (owner == null) {
            return false;
         } else {
            LivingEntity attacked = owner.getAttacking();
            int attackTime = owner.getLastAttackTime();
            if (attackTime != this.cachedAttackTime && attacked != null) {
               for (Class<?> excluded : this.exclusions) {
                  if (excluded.isAssignableFrom(attacked.getClass())) {
                     return false;
                  }
               }

               this.ownerTarget = attacked;
               return this.canTrack(this.ownerTarget, TargetPredicate.DEFAULT) && this.girl.canAttackWithOwner(this.ownerTarget, owner);
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
         this.cachedAttackTime = owner.getLastAttackTime();
      }

      super.start();
   }
}
