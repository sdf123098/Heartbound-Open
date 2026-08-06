package com.cuddly.heartbound.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class TransformCriterion extends SimpleCriterionTrigger<TransformCriterion.Conditions> {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("heartbound", "transform");

   @Override
   public Codec<TransformCriterion.Conditions> codec() {
      return TransformCriterion.Conditions.CODEC;
   }

   public void trigger(ServerPlayer player) {
      this.trigger(player, conditions -> true);
   }

   public static record Conditions(Optional<ContextAwarePredicate> player) implements net.minecraft.advancements.criterion.SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<TransformCriterion.Conditions> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(TransformCriterion.Conditions::player))
               .apply(instance, TransformCriterion.Conditions::new)
      );

      public static Criterion<TransformCriterion.Conditions> any() {
         return HeartboundCriteria.TRANSFORM.createCriterion(new TransformCriterion.Conditions(Optional.empty()));
      }
   }
}
