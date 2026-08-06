package com.cuddly.heartbound.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class KillGirlCriterion extends SimpleCriterionTrigger<KillGirlCriterion.Conditions> {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("heartbound", "kill_girl");

   @Override
   public Codec<KillGirlCriterion.Conditions> codec() {
      return KillGirlCriterion.Conditions.CODEC;
   }

   public void trigger(ServerPlayer player) {
      this.trigger(player, conditions -> true);
   }

   public static record Conditions(Optional<ContextAwarePredicate> player) implements net.minecraft.advancements.criterion.SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<KillGirlCriterion.Conditions> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(KillGirlCriterion.Conditions::player))
               .apply(instance, KillGirlCriterion.Conditions::new)
      );

      public static Criterion<KillGirlCriterion.Conditions> any() {
         return HeartboundCriteria.KILL_GIRL.createCriterion(new KillGirlCriterion.Conditions(Optional.empty()));
      }
   }
}
