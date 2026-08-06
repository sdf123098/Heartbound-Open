package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.freecam.Freecam;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({MultiPlayerGameMode.class})
public class MultiPlayerGameModeMixin {
   @Inject(
      method = {"useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onInteractBlock(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
      if (freecam$disableInteract() || heartbound$isRiderInScene()) {
         cir.setReturnValue(InteractionResult.PASS);
      }
   }

   @Inject(
      method = {"interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/EntityHitResult;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onInteractEntity(Player player, Entity entity, EntityHitResult hitResult, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
      if (entity.equals(Freecam.MC.player) || freecam$disableInteract() || heartbound$isRiderInScene()) {
         cir.setReturnValue(InteractionResult.PASS);
      }
   }

   @Inject(
      method = {"attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onAttackEntity(Player player, Entity target, CallbackInfo ci) {
      if (target.equals(Freecam.MC.player) || heartbound$isRiderInScene()) {
         ci.cancel();
      }
   }

   @Unique
   private static boolean heartbound$isRiderInScene() {
      return Freecam.MC.player != null && Freecam.MC.player.getVehicle() instanceof TransformablePlayer mount && mount.heartbound$isTransformSceneActive();
   }

   @Unique
   private static boolean freecam$disableInteract() {
      return Freecam.isEnabled() && !Freecam.isPlayerControlEnabled() && !freecam$allowInteract();
   }

   @Unique
   private static boolean freecam$allowInteract() {
      return ModConfig.INSTANCE.utility.allowInteract && ModConfig.INSTANCE.utility.interactionMode.equals(ModConfig.InteractionMode.PLAYER);
   }
}
