package com.cuddly.heartbound.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class MaxRelationshipCriterion extends SimpleCriterionTrigger<MaxRelationshipCriterion.Conditions> {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("heartbound", "max_relationship");

   @Override
   public Codec<MaxRelationshipCriterion.Conditions> codec() {
      return MaxRelationshipCriterion.Conditions.CODEC;
   }

   public void trigger(ServerPlayer player) {
      this.trigger(player, conditions -> true);
   }

   public static record Conditions(Optional<ContextAwarePredicate> player) implements net.minecraft.advancements.criterion.SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<MaxRelationshipCriterion.Conditions> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(MaxRelationshipCriterion.Conditions::player))
               .apply(instance, MaxRelationshipCriterion.Conditions::new)
      );

      public static Criterion<MaxRelationshipCriterion.Conditions> any() {
         return HeartboundCriteria.MAX_RELATIONSHIP.createCriterion(new MaxRelationshipCriterion.Conditions(Optional.empty()));
      }
   }
}
