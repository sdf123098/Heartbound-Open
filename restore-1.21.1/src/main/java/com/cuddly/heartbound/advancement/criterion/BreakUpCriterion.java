package com.cuddly.heartbound.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class BreakUpCriterion extends AbstractCriterion<BreakUpCriterion.Conditions> {
   public static final Identifier ID = Identifier.of("heartbound", "break_up");

   @Override
   public Codec<BreakUpCriterion.Conditions> getConditionsCodec() {
      return BreakUpCriterion.Conditions.CODEC;
   }

   public void trigger(ServerPlayerEntity player) {
      this.trigger(player, conditions -> true);
   }

   public static record Conditions(Optional<LootContextPredicate> player) implements net.minecraft.advancement.criterion.AbstractCriterion.Conditions {
      public static final Codec<BreakUpCriterion.Conditions> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(BreakUpCriterion.Conditions::player))
               .apply(instance, BreakUpCriterion.Conditions::new)
      );

      public static AdvancementCriterion<BreakUpCriterion.Conditions> any() {
         return HeartboundCriteria.BREAK_UP.create(new BreakUpCriterion.Conditions(Optional.empty()));
      }
   }
}
