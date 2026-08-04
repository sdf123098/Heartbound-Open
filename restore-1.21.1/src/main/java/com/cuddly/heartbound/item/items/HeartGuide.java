package com.cuddly.heartbound.item.items;

import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import vazkii.patchouli.api.PatchouliAPI;

public class HeartGuide extends Item {
   public HeartGuide(Settings settings) {
      super(settings);
   }

   @Override
   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.getStackInHand(hand);
      if (world.isClient()) {
         PatchouliAPI.get().openBookGUI(Identifier.of("heartbound", "heart_guide"));
      }

      return TypedActionResult.success(stack);
   }

   @Override
   public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
      super.appendTooltip(stack, context, tooltip, type);
      tooltip.add(Text.translatable("item.heartbound.heart_guide.tooltip.edition").formatted(Formatting.GRAY));
   }
}
