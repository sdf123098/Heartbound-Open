package com.cuddly.heartbound.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class TransformCriterion extends AbstractCriterion<TransformCriterion.Conditions> {
   public static final Identifier ID = Identifier.of("heartbound", "transform");

   @Override
   public Codec<TransformCriterion.Conditions> getConditionsCodec() {
      return TransformCriterion.Conditions.CODEC;
   }

   public void trigger(ServerPlayerEntity player) {
      this.trigger(player, conditions -> true);
   }

   public static record Conditions(Optional<LootContextPredicate> player) implements net.minecraft.advancement.criterion.AbstractCriterion.Conditions {
      public static final Codec<TransformCriterion.Conditions> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(TransformCriterion.Conditions::player))
               .apply(instance, TransformCriterion.Conditions::new)
      );

      public static AdvancementCriterion<TransformCriterion.Conditions> any() {
         return HeartboundCriteria.TRANSFORM.create(new TransformCriterion.Conditions(Optional.empty()));
      }
   }
}
