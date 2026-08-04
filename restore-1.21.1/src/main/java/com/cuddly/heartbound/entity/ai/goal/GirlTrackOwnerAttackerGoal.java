package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.ai.goal.Goal.Control;

public class GirlTrackOwnerAttackerGoal extends TrackTargetGoal {
   private final TameableGirlEntity girl;
   private LivingEntity threat;
   private int cachedDamageTime;

   public GirlTrackOwnerAttackerGoal(TameableGirlEntity girl) {
      super(girl, false);
      this.girl = girl;
      this.setControls(EnumSet.of(Control.TARGET));
   }

   @Override
   public boolean canStart() {
      if (this.girl.isTamed() && !this.girl.isSitting()) {
         LivingEntity owner = this.girl.getOwner();
         if (owner == null) {
            return false;
         } else {
            LivingEntity attacker = owner.getAttacker();
            int damageTime = owner.getLastAttackedTime();
            if (damageTime == this.cachedDamageTime) {
               return false;
            } else if (!this.canTrack(attacker, TargetPredicate.DEFAULT)) {
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
         this.cachedDamageTime = owner.getLastAttackedTime();
      }

      super.start();
   }
}
