package com.cuddly.heartbound.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class MaxRelationshipCriterion extends AbstractCriterion<MaxRelationshipCriterion.Conditions> {
   public static final Identifier ID = Identifier.of("heartbound", "max_relationship");

   @Override
   public Codec<MaxRelationshipCriterion.Conditions> getConditionsCodec() {
      return MaxRelationshipCriterion.Conditions.CODEC;
   }

   public void trigger(ServerPlayerEntity player) {
      this.trigger(player, conditions -> true);
   }

   public static record Conditions(Optional<LootContextPredicate> player) implements net.minecraft.advancement.criterion.AbstractCriterion.Conditions {
      public static final Codec<MaxRelationshipCriterion.Conditions> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(MaxRelationshipCriterion.Conditions::player))
               .apply(instance, MaxRelationshipCriterion.Conditions::new)
      );

      public static AdvancementCriterion<MaxRelationshipCriterion.Conditions> any() {
         return HeartboundCriteria.MAX_RELATIONSHIP.create(new MaxRelationshipCriterion.Conditions(Optional.empty()));
      }
   }
}
