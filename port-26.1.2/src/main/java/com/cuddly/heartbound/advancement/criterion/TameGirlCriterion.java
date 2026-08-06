package com.cuddly.heartbound.advancement.criterion;

import com.cuddly.heartbound.entity.base.GirlEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntityPredicate.Builder;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContextSource;

public class TameGirlCriterion extends SimpleCriterionTrigger<TameGirlCriterion.Conditions> {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("heartbound", "tame_girl");

   @Override
   public Codec<TameGirlCriterion.Conditions> codec() {
      return TameGirlCriterion.Conditions.CODEC;
   }

   public void trigger(ServerPlayer player, GirlEntity entity) {
      LootContext lootContext = EntityPredicate.createContext(player, entity);
      this.trigger(player, conditions -> conditions.matches(lootContext));
   }

   public static record Conditions(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> entity)
      implements net.minecraft.advancements.criterion.SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<TameGirlCriterion.Conditions> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
                  EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TameGirlCriterion.Conditions::player),
                  EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(TameGirlCriterion.Conditions::entity)
               )
               .apply(instance, TameGirlCriterion.Conditions::new)
      );

      public static Criterion<TameGirlCriterion.Conditions> any() {
         return HeartboundCriteria.TAME_GIRL.createCriterion(new TameGirlCriterion.Conditions(Optional.empty(), Optional.empty()));
      }

      public static Criterion<TameGirlCriterion.Conditions> create(Builder entity) {
         return HeartboundCriteria.TAME_GIRL
            .createCriterion(new TameGirlCriterion.Conditions(Optional.empty(), Optional.of(EntityPredicate.wrap(entity))));
      }

      public boolean matches(LootContext entityCtx) {
         return this.entity.isEmpty() || this.entity.get().matches(entityCtx);
      }

      @Override
      public void validate(ValidationContextSource validator) {
         net.minecraft.advancements.criterion.SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
         this.entity.ifPresent(entityPredicate -> entityPredicate.validate(validator.entityContext()));
      }
   }
}
