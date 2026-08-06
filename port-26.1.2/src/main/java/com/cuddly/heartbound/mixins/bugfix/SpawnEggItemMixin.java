package com.cuddly.heartbound.mixins.bugfix;

import com.cuddly.heartbound.Heartbound;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 诊断：vanilla SpawnEggItem 召唤链路日志（26.1 用 DataComponents.ENTITY_DATA 取类型，失败时无任何提示）。
 * useOn HEAD 看 getType 结果；spawnMob HEAD 看 spawn 是否进入。
 */
@Mixin(SpawnEggItem.class)
public abstract class SpawnEggItemMixin {
   @Inject(
      method = {"useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"},
      at = {@At("HEAD")}
   )
   private void heartbound$logSpawnEggUseOn(UseOnContext ctx, CallbackInfoReturnable<InteractionResult> cir) {
      ItemStack stack = ctx.getItemInHand();
      Heartbound.LOGGER.info(
         "[SpawnEgg] useOn {} item={} getType={} level={}",
         ctx.getLevel().isClientSide() ? "CLIENT" : "SERVER",
         stack.getItem(),
         SpawnEggItem.getType(stack),
         ctx.getLevel().getClass().getSimpleName()
      );
   }

   @Inject(
      method = {"spawnMob(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ZZ)Lnet/minecraft/world/InteractionResult;"},
      at = {@At("HEAD")}
   )
   private static void heartbound$logSpawnMob(
      LivingEntity user, ItemStack stack, Level level, BlockPos pos, boolean spawnEvenIfSameType, boolean spawnInAir,
      CallbackInfoReturnable<InteractionResult> cir
   ) {
      Heartbound.LOGGER.info(
         "[SpawnEgg] spawnMob getType={} pos={} server={} difficulty={}",
         SpawnEggItem.getType(stack),
         pos,
         level instanceof ServerLevel,
         level.getDifficulty()
      );
   }
}
