package com.cuddly.heartbound.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class KillGirlCriterion extends AbstractCriterion<KillGirlCriterion.Conditions> {
   public static final Identifier ID = Identifier.of("heartbound", "kill_girl");

   @Override
   public Codec<KillGirlCriterion.Conditions> getConditionsCodec() {
      return KillGirlCriterion.Conditions.CODEC;
   }

   public void trigger(ServerPlayerEntity player) {
      this.trigger(player, conditions -> true);
   }

   public static record Conditions(Optional<LootContextPredicate> player) implements net.minecraft.advancement.criterion.AbstractCriterion.Conditions {
      public static final Codec<KillGirlCriterion.Conditions> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(KillGirlCriterion.Conditions::player))
               .apply(instance, KillGirlCriterion.Conditions::new)
      );

      public static AdvancementCriterion<KillGirlCriterion.Conditions> any() {
         return HeartboundCriteria.KILL_GIRL.create(new KillGirlCriterion.Conditions(Optional.empty()));
      }
   }
}
