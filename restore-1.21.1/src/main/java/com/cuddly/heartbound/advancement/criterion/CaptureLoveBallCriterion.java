package com.cuddly.heartbound.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CaptureLoveBallCriterion extends AbstractCriterion<CaptureLoveBallCriterion.Conditions> {
   public static final Identifier ID = Identifier.of("heartbound", "capture_love_ball");

   @Override
   public Codec<CaptureLoveBallCriterion.Conditions> getConditionsCodec() {
      return CaptureLoveBallCriterion.Conditions.CODEC;
   }

   public void trigger(ServerPlayerEntity player) {
      this.trigger(player, conditions -> true);
   }

   public static record Conditions(Optional<LootContextPredicate> player) implements net.minecraft.advancement.criterion.AbstractCriterion.Conditions {
      public static final Codec<CaptureLoveBallCriterion.Conditions> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(CaptureLoveBallCriterion.Conditions::player))
               .apply(instance, CaptureLoveBallCriterion.Conditions::new)
      );

      public static AdvancementCriterion<CaptureLoveBallCriterion.Conditions> any() {
         return HeartboundCriteria.CAPTURE_LOVE_BALL.create(new CaptureLoveBallCriterion.Conditions(Optional.empty()));
      }
   }
}
