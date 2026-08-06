package com.cuddly.heartbound.advancement.criterion;

import com.cuddly.heartbound.Heartbound;
import com.mojang.serialization.Codec;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class HeartboundCriteria {
   public static final Codec<CriterionTrigger<?>> CODEC = BuiltInRegistries.TRIGGER_TYPES.byNameCodec();
   public static final TameGirlCriterion TAME_GIRL = register("tame_girl", new TameGirlCriterion());
   public static final FirstLoginCriterion FIRST_LOGIN = register("first_login", new FirstLoginCriterion());
   public static final MaxRelationshipCriterion MAX_RELATIONSHIP = register("max_relationship", new MaxRelationshipCriterion());
   public static final BreakUpCriterion BREAK_UP = register("break_up", new BreakUpCriterion());
   public static final KillGirlCriterion KILL_GIRL = register("kill_girl", new KillGirlCriterion());
   public static final TransformCriterion TRANSFORM = register("transform", new TransformCriterion());
   public static final CaptureLoveBallCriterion CAPTURE_LOVE_BALL = register("capture_love_ball", new CaptureLoveBallCriterion());
   public static final CaptureMasterRingCriterion CAPTURE_MASTER_RING = register("capture_master_ring", new CaptureMasterRingCriterion());

   public static <T extends CriterionTrigger<?>> T register(String id, T criterion) {
      return Registry.register(BuiltInRegistries.TRIGGER_TYPES, Identifier.fromNamespaceAndPath("heartbound", id), criterion);
   }

   public static CriterionTrigger<?> getDefault(Registry<CriterionTrigger<?>> registry) {
      return CriteriaTriggers.IMPOSSIBLE;
   }

   public static void registerAdvancementCriteria() {
      Heartbound.LOGGER.info("Registering Advancement Criteria for Heartbound");
   }
}
