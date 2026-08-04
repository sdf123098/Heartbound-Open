package com.cuddly.heartbound.entity.ai.goal;

import com.cuddly.heartbound.entity.base.tamable.BaseGirlEntityAI;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

public class GirlMeleeAttackGoal extends MeleeAttackGoal {
   private final BaseGirlEntityAI girl;

   public GirlMeleeAttackGoal(BaseGirlEntityAI girl, double speed, boolean pauseWhenMobIdle) {
      super(girl, speed, pauseWhenMobIdle);
      this.girl = girl;
   }

   @Override
   public void start() {
      this.girl.setSprinting(true);
      this.girl.setAttacking(true);
      super.start();
   }

   @Override
   public void stop() {
      this.girl.setSprinting(false);
      this.girl.setAttacking(false);
      super.stop();
   }

   @Override
   public boolean shouldContinue() {
      LivingEntity target = this.girl.getTarget();
      return target != null && target.isAlive() ? super.shouldContinue() : false;
   }
}
