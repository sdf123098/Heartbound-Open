package com.cuddly.heartbound.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class FirstLoginCriterion extends SimpleCriterionTrigger<FirstLoginCriterion.Conditions> {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("heartbound", "first_login");

   @Override
   public Codec<FirstLoginCriterion.Conditions> codec() {
      return FirstLoginCriterion.Conditions.CODEC;
   }

   public void trigger(ServerPlayer player) {
      this.trigger(player, conditions -> true);
   }

   public static record Conditions(Optional<ContextAwarePredicate> player) implements net.minecraft.advancements.criterion.SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<FirstLoginCriterion.Conditions> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(FirstLoginCriterion.Conditions::player))
               .apply(instance, FirstLoginCriterion.Conditions::new)
      );

      public static Criterion<FirstLoginCriterion.Conditions> any() {
         return HeartboundCriteria.FIRST_LOGIN.createCriterion(new FirstLoginCriterion.Conditions(Optional.empty()));
      }
   }
}
