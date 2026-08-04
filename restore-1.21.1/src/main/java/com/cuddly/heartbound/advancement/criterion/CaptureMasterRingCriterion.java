package com.cuddly.heartbound.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CaptureMasterRingCriterion extends AbstractCriterion<CaptureMasterRingCriterion.Conditions> {
   public static final Identifier ID = Identifier.of("heartbound", "capture_master_ring");

   @Override
   public Codec<CaptureMasterRingCriterion.Conditions> getConditionsCodec() {
      return CaptureMasterRingCriterion.Conditions.CODEC;
   }

   public void trigger(ServerPlayerEntity player) {
      this.trigger(player, conditions -> true);
   }

   public static record Conditions(Optional<LootContextPredicate> player) implements net.minecraft.advancement.criterion.AbstractCriterion.Conditions {
      public static final Codec<CaptureMasterRingCriterion.Conditions> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(CaptureMasterRingCriterion.Conditions::player))
               .apply(instance, CaptureMasterRingCriterion.Conditions::new)
      );

      public static AdvancementCriterion<CaptureMasterRingCriterion.Conditions> any() {
         return HeartboundCriteria.CAPTURE_MASTER_RING.create(new CaptureMasterRingCriterion.Conditions(Optional.empty()));
      }
   }
}
