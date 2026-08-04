package com.cuddly.heartbound.advancement.criterion;

import com.cuddly.heartbound.entity.base.GirlEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.predicate.entity.EntityPredicate.Builder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class TameGirlCriterion extends AbstractCriterion<TameGirlCriterion.Conditions> {
   public static final Identifier ID = Identifier.of("heartbound", "tame_girl");

   @Override
   public Codec<TameGirlCriterion.Conditions> getConditionsCodec() {
      return TameGirlCriterion.Conditions.CODEC;
   }

   public void trigger(ServerPlayerEntity player, GirlEntity entity) {
      LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(player, entity);
      this.trigger(player, conditions -> conditions.matches(lootContext));
   }

   public static record Conditions(Optional<LootContextPredicate> player, Optional<LootContextPredicate> entity)
      implements net.minecraft.advancement.criterion.AbstractCriterion.Conditions {
      public static final Codec<TameGirlCriterion.Conditions> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
                  EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(TameGirlCriterion.Conditions::player),
                  EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("entity").forGetter(TameGirlCriterion.Conditions::entity)
               )
               .apply(instance, TameGirlCriterion.Conditions::new)
      );

      public static AdvancementCriterion<TameGirlCriterion.Conditions> any() {
         return HeartboundCriteria.TAME_GIRL.create(new TameGirlCriterion.Conditions(Optional.empty(), Optional.empty()));
      }

      public static AdvancementCriterion<TameGirlCriterion.Conditions> create(Builder entity) {
         return HeartboundCriteria.TAME_GIRL
            .create(new TameGirlCriterion.Conditions(Optional.empty(), Optional.of(EntityPredicate.contextPredicateFromEntityPredicate(entity))));
      }

      public boolean matches(LootContext entityCtx) {
         return this.entity.isEmpty() || this.entity.get().test(entityCtx);
      }

      @Override
      public void validate(LootContextPredicateValidator validator) {
         net.minecraft.advancement.criterion.AbstractCriterion.Conditions.super.validate(validator);
         validator.validateEntityPredicate(this.entity, ".entity");
      }
   }
}
