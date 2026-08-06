package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.TameableGirlEntity;
import java.util.Comparator;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.entity.EntityTypeTest;

public class GirlDefendBaseGoal extends TargetGoal {
   private final TameableGirlEntity girl;
   private LivingEntity target;

   public GirlDefendBaseGoal(TameableGirlEntity girl) {
      super(girl, false);
      this.girl = girl;
      this.setFlags(EnumSet.of(Flag.TARGET));
   }

   @Override
   public boolean canUse() {
      if (this.girl.isTamed()
         && this.girl.isRoaming()
         && !this.girl.isFollowing()
         && !this.girl.isSitting()
         && !this.girl.isHavingSex()
         && !this.girl.isPassenger()) {
         this.targetMob = this.girl
            .level()
            .getEntities(
               EntityTypeTest.forClass(Monster.class),
               this.girl.getBoundingBox().inflate(16.0, 8.0, 16.0),
               entity -> entity.isAlive() && (this.girl.getOwner() == null || entity != this.girl.getOwner().getLastHurtMob())
            )
            .stream()
            .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(this.girl)))
            .orElse(null);
         return this.targetMob != null;
      } else {
         return false;
      }
   }

   @Override
   public boolean canContinueToUse() {
      return this.girl.isRoaming()
         && !this.girl.isFollowing()
         && !this.girl.isSitting()
         && !this.girl.isHavingSex()
         && !this.girl.isPassenger()
         && this.targetMob != null
         && this.targetMob.isAlive()
         && this.girl.distanceToSqr(this.targetMob) <= 256.0;
   }

   @Override
   public void start() {
      this.girl.setTarget(this.targetMob);
      super.start();
   }

   @Override
   public void stop() {
      this.girl.setTarget(null);
      super.stop();
   }
}
