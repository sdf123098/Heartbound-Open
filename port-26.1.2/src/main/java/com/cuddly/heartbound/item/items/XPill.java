package com.cuddly.heartbound.item.items;

import com.cuddly.heartbound.client.gui.screen.CategorySelectionScreen;
import com.cuddly.heartbound.networking.C2S.TransformRevertC2SPacket;
import com.cuddly.heartbound.transformation.TransformablePlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class XPill extends Item {
   public XPill(Properties settings) {
      super(settings);
   }

   @Override
   public InteractionResult use(Level world, Player user, InteractionHand hand) {
      ItemStack stack = user.getItemInHand(hand);
      TransformablePlayer tp = (TransformablePlayer)user;
      if (tp.heartbound$isTransformed()) {
         if (world.isClientSide()) {
            this.sendRevertPacket();
         }

         if (!user.isCreative()) {
            stack.shrink(1);
         }

         return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
      } else {
         if (world.isClientSide()) {
            this.openSelectionScreen();
         }

         return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
      }
   }

   @Environment(EnvType.CLIENT)
   private void sendRevertPacket() {
      ClientPlayNetworking.send(new TransformRevertC2SPacket());
   }

   @Environment(EnvType.CLIENT)
   private void openSelectionScreen() {
      Minecraft.getInstance().setScreen(new CategorySelectionScreen());
   }
}
