package com.cuddly.heartbound.mixins.freecam;

import com.cuddly.heartbound.config.ModBindings;
import com.cuddly.heartbound.config.ModConfig;
import com.cuddly.heartbound.freecam.Freecam;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Minecraft.class})
public class MinecraftMixin {
   @Inject(
      method = {"startAttack()Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
      if (freecam$disableInteract() || heartbound$isInScene()) {
         cir.cancel();
      }
   }

   @Inject(
      method = {"startUseItem()V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDoItemUse(CallbackInfo ci) {
      if (heartbound$isInScene()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"pick(F)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDoItemPick(CallbackInfo ci) {
      if (freecam$disableInteract() || heartbound$isInScene()) {
         ci.cancel();
      }
   }

   @ModifyVariable(
      method = {"pick(F)V"},
      at = @At(
         value = "INVOKE_ASSIGN",
         target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"
      )
   )
   private Entity onUpdateTargetedEntity(Entity entity) {
      return (Entity)(!Freecam.isEnabled()
            || !Freecam.isPlayerControlEnabled() && !ModConfig.INSTANCE.utility.interactionMode.equals(ModConfig.InteractionMode.PLAYER)
         ? entity
         : Freecam.MC.player);
   }

   @Inject(
      method = {"continueAttack(Z)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onHandleBlockBreaking(CallbackInfo ci) {
      if (freecam$disableInteract() || heartbound$isInScene()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"handleKeybinds()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z",
         ordinal = 2
      )},
      cancellable = true
   )
   private void onHandleInputEvents(CallbackInfo ci) {
      if (ModBindings.KEY_TOGGLE.get().isDown() || ModBindings.KEY_TRIPOD_RESET.get().isDown()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"disconnectFromWorld(Lnet/minecraft/network/chat/Component;)V"},
      at = {@At("HEAD")}
   )
   private void onDisconnect(CallbackInfo ci) {
      Freecam.onDisconnect();
   }

   @Unique
   private static boolean heartbound$isInScene() {
      Minecraft client = Minecraft.getInstance();
      if (client.player instanceof TransformablePlayer tp && tp.heartbound$isTransformSceneActive()) {
         return true;
      }

      return client.player != null && client.player.getVehicle() instanceof TransformablePlayer mount && mount.heartbound$isTransformSceneActive();
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
