package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.ai.goal.Goal.Control;
import net.minecraft.entity.mob.HostileEntity;

public class GirlDefendBaseGoal extends TrackTargetGoal {
   private final TameableGirlEntity girl;
   private LivingEntity target;
   private final TargetPredicate targetPredicate;

   public GirlDefendBaseGoal(TameableGirlEntity girl) {
      super(girl, false);
      this.girl = girl;
      this.targetPredicate = TargetPredicate.createAttackable()
         .setBaseMaxDistance(16.0)
         .setPredicate(entity -> !(entity instanceof HostileEntity) ? false : girl.getOwner() == null || entity != girl.getOwner().getAttacking());
      this.setControls(EnumSet.of(Control.TARGET));
   }

   @Override
   public boolean canStart() {
      if (this.girl.isTamed()
         && this.girl.isRoaming()
         && !this.girl.isFollowing()
         && !this.girl.isSitting()
         && !this.girl.isHavingSex()
         && !this.girl.hasVehicle()) {
         this.target = this.girl
            .getWorld()
            .getClosestEntity(
               HostileEntity.class,
               this.targetPredicate,
               this.girl,
               this.girl.getX(),
               this.girl.getY(),
               this.girl.getZ(),
               this.girl.getBoundingBox().expand(16.0, 8.0, 16.0)
            );
         return this.target != null;
      } else {
         return false;
      }
   }

   @Override
   public boolean shouldContinue() {
      return this.girl.isRoaming()
         && !this.girl.isFollowing()
         && !this.girl.isSitting()
         && !this.girl.isHavingSex()
         && !this.girl.hasVehicle()
         && this.target != null
         && this.target.isAlive()
         && this.girl.squaredDistanceTo(this.target) <= 256.0;
   }

   @Override
   public void start() {
      this.girl.setTarget(this.target);
      super.start();
   }

   @Override
   public void stop() {
      this.girl.setTarget(null);
      super.stop();
   }
}
