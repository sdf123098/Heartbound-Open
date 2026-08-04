package com.cuddly.heartbound.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal.Control;

public class ConditionalGoal extends Goal {
   private final Goal delegate;
   private final BooleanSupplier predicate;

   public ConditionalGoal(Goal delegate, BooleanSupplier predicate) {
      this.delegate = delegate;
      this.predicate = predicate;
   }

   @Override
   public boolean canStart() {
      return this.predicate.getAsBoolean() && this.delegate.canStart();
   }

   @Override
   public boolean shouldContinue() {
      return this.predicate.getAsBoolean() && this.delegate.shouldContinue();
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
   public boolean canStop() {
      return this.delegate.canStop();
   }

   @Override
   public boolean shouldRunEveryTick() {
      return this.delegate.shouldRunEveryTick();
   }

   @Override
   public EnumSet<Control> getControls() {
      return this.delegate.getControls();
   }
}
