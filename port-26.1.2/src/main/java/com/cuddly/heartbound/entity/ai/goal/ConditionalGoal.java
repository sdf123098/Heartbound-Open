package com.cuddly.heartbound.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;
import net.minecraft.world.entity.ai.goal.Goal;

public class ConditionalGoal extends Goal {
   private final Goal delegate;
   private final BooleanSupplier predicate;

   public ConditionalGoal(Goal delegate, BooleanSupplier predicate) {
      this.delegate = delegate;
      this.predicate = predicate;
   }

   @Override
   public boolean canUse() {
      return this.predicate.getAsBoolean() && this.delegate.canUse();
   }

   @Override
   public boolean canContinueToUse() {
      return this.predicate.getAsBoolean() && this.delegate.canContinueToUse();
   }

   @Override
   public void start() {
      this.delegate.start();
   }

   @Override
   public void stop() {
      this.delegate.stop();
   }

   @Override
   public void tick() {
      this.delegate.tick();
   }

   @Override
   public boolean isInterruptable() {
      return this.delegate.isInterruptable();
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return this.delegate.requiresUpdateEveryTick();
   }

   @Override
   public EnumSet<Flag> getFlags() {
      return this.delegate.getFlags();
   }
}
