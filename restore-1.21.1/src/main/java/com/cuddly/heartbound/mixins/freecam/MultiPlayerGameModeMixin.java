package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.freecam.Freecam;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientPlayerInteractionManager.class})
public class MultiPlayerGameModeMixin {
   @Inject(
      method = {"interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
      if (freecam$disableInteract() || heartbound$isRiderInScene()) {
         cir.setReturnValue(ActionResult.PASS);
      }
   }

   @Inject(
      method = {"interactEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onInteractEntity(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
      if (entity.equals(Freecam.MC.player) || freecam$disableInteract() || heartbound$isRiderInScene()) {
         cir.setReturnValue(ActionResult.PASS);
      }
   }

   @Inject(
      method = {"interactEntityAtLocation(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/hit/EntityHitResult;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onInteractEntityAtLocation(PlayerEntity player, Entity entity, EntityHitResult hitResult, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
      if (entity.equals(Freecam.MC.player) || freecam$disableInteract() || heartbound$isRiderInScene()) {
         cir.setReturnValue(ActionResult.PASS);
      }
   }

   @Inject(
      method = {"attackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
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
