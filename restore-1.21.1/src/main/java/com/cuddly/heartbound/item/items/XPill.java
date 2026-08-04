package com.cuddly.heartbound.item.items;

import com.cuddly.heartbound.client.gui.screen.CategorySelectionScreen;
import com.cuddly.heartbound.networking.C2S.TransformRevertC2SPacket;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class XPill extends Item {
   public XPill(Settings settings) {
      super(settings);
   }

   @Override
   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.getStackInHand(hand);
      TransformablePlayer tp = (TransformablePlayer)user;
      if (tp.heartbound$isTransformed()) {
         if (world.isClient()) {
            this.sendRevertPacket();
         }

         if (!user.isCreative()) {
            stack.decrement(1);
         }

         return TypedActionResult.success(stack);
      } else {
         if (world.isClient()) {
            this.openSelectionScreen();
         }

         return TypedActionResult.success(stack);
      }
   }

   @Environment(EnvType.CLIENT)
   private void sendRevertPacket() {
      ClientPlayNetworking.send(new TransformRevertC2SPacket());
   }

   @Environment(EnvType.CLIENT)
   private void openSelectionScreen() {
      MinecraftClient.getInstance().setScreen(new CategorySelectionScreen());
   }
}
