package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class GirlTrackOwnerAttackerGoal extends TargetGoal {
   private final TameableGirlEntity girl;
   private LivingEntity threat;
   private int cachedDamageTime;

   public GirlTrackOwnerAttackerGoal(TameableGirlEntity girl) {
      super(girl, false);
      this.girl = girl;
      this.setFlags(EnumSet.of(Flag.TARGET));
   }

   @Override
   public boolean canUse() {
      if (this.girl.isTamed() && !this.girl.isSitting()) {
         LivingEntity owner = this.girl.getOwner();
         if (owner == null) {
            return false;
         } else {
            LivingEntity attacker = owner.getLastHurtByMob();
            int damageTime = owner.getLastHurtByMobTimestamp();
            if (damageTime == this.cachedDamageTime) {
               return false;
            } else if (!this.canAttack(attacker, TargetingConditions.DEFAULT)) {
               return false;
            } else {
               this.threat = attacker;
               return this.girl.canAttackWithOwner(this.threat, owner);
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public void start() {
      this.mob.setTarget(this.threat);
      LivingEntity owner = this.girl.getOwner();
      if (owner != null) {
         this.cachedDamageTime = owner.getLastHurtByMobTimestamp();
      }

      super.start();
   }
}
