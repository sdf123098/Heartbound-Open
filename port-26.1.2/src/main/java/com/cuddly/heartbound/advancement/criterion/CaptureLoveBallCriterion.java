package com.cuddly.heartbound.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class CaptureLoveBallCriterion extends SimpleCriterionTrigger<CaptureLoveBallCriterion.Conditions> {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("heartbound", "capture_love_ball");

   @Override
   public Codec<CaptureLoveBallCriterion.Conditions> codec() {
      return CaptureLoveBallCriterion.Conditions.CODEC;
   }

   public void trigger(ServerPlayer player) {
      this.trigger(player, conditions -> true);
   }

   public static record Conditions(Optional<ContextAwarePredicate> player) implements net.minecraft.advancements.criterion.SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<CaptureLoveBallCriterion.Conditions> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(CaptureLoveBallCriterion.Conditions::player))
               .apply(instance, CaptureLoveBallCriterion.Conditions::new)
      );

      public static Criterion<CaptureLoveBallCriterion.Conditions> any() {
         return HeartboundCriteria.CAPTURE_LOVE_BALL.createCriterion(new CaptureLoveBallCriterion.Conditions(Optional.empty()));
      }
   }
}
