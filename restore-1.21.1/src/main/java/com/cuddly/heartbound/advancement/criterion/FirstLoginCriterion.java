package com.cuddly.heartbound.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class FirstLoginCriterion extends AbstractCriterion<FirstLoginCriterion.Conditions> {
   public static final Identifier ID = Identifier.of("heartbound", "first_login");

   @Override
   public Codec<FirstLoginCriterion.Conditions> getConditionsCodec() {
      return FirstLoginCriterion.Conditions.CODEC;
   }

   public void trigger(ServerPlayerEntity player) {
      this.trigger(player, conditions -> true);
   }

   public static record Conditions(Optional<LootContextPredicate> player) implements net.minecraft.advancement.criterion.AbstractCriterion.Conditions {
      public static final Codec<FirstLoginCriterion.Conditions> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(FirstLoginCriterion.Conditions::player))
               .apply(instance, FirstLoginCriterion.Conditions::new)
      );

      public static AdvancementCriterion<FirstLoginCriterion.Conditions> any() {
         return HeartboundCriteria.FIRST_LOGIN.create(new FirstLoginCriterion.Conditions(Optional.empty()));
      }
   }
}
