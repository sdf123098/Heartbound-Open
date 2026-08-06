package com.cuddly.heartbound.item.items;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.PatchouliAPI;

public class HeartGuide extends Item {
   public HeartGuide(Properties settings) {
      super(settings);
   }

   @Override
   public InteractionResult use(Level world, Player user, InteractionHand hand) {
      ItemStack stack = user.getItemInHand(hand);
      if (world.isClientSide()) {
         PatchouliAPI.get().openBookGUI(Identifier.fromNamespaceAndPath("heartbound", "heart_guide"));
      }

      return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag type) {
      super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
      tooltip.accept(Component.translatable("item.heartbound.heart_guide.tooltip.edition").withStyle(ChatFormatting.GRAY));
   }
}
