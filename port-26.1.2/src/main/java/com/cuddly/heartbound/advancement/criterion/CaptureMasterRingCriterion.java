package com.cuddly.heartbound.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class CaptureMasterRingCriterion extends SimpleCriterionTrigger<CaptureMasterRingCriterion.Conditions> {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("heartbound", "capture_master_ring");

   @Override
   public Codec<CaptureMasterRingCriterion.Conditions> codec() {
      return CaptureMasterRingCriterion.Conditions.CODEC;
   }

   public void trigger(ServerPlayer player) {
      this.trigger(player, conditions -> true);
   }

   public static record Conditions(Optional<ContextAwarePredicate> player) implements net.minecraft.advancements.criterion.SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<CaptureMasterRingCriterion.Conditions> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(CaptureMasterRingCriterion.Conditions::player))
               .apply(instance, CaptureMasterRingCriterion.Conditions::new)
      );

      public static Criterion<CaptureMasterRingCriterion.Conditions> any() {
         return HeartboundCriteria.CAPTURE_MASTER_RING.createCriterion(new CaptureMasterRingCriterion.Conditions(Optional.empty()));
      }
   }
}
